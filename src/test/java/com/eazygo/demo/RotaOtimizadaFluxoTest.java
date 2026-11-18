package com.eazygo.demo;

import com.eazygo.demo.Entities.DailyPoll;
import com.eazygo.demo.Entities.PollResponseStatus;
import com.eazygo.demo.Entities.RotaOtimizada;
import com.eazygo.demo.Services.GeocodingService;
import com.eazygo.demo.Services.OsrmService;
import com.eazygo.demo.Services.PollService;
import com.eazygo.demo.Services.RouteService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Simula o fluxo completo: passageiros confirmam na enquete -> geocodificação -> OSRM Trip -> rota otimizada.
 *
 * NÃO usa internet: sobe dois servidores HTTP falsos na própria máquina (um imitando o Nominatim e
 * outro imitando o OSRM) e aponta os services reais para eles. Assim o teste é rápido e sempre igual.
 *
 * Também NÃO depende do horário da enquete (18:30 -> 04:00): as respostas são inseridas direto na lista
 * do PollService, então o teste passa em qualquer hora do dia.
 */
class RotaOtimizadaFluxoTest {

    private static final String ORIGEM = "-51.2286,-30.0331";
    private static final String DESTINO = "-51.1839,-29.9189";

    private HttpServer nominatimFalso;
    private HttpServer osrmFalso;

    // o que o OSRM falso recebeu (para conferirmos que o app montou a URL certa)
    private final AtomicReference<String> caminhoRecebido = new AtomicReference<>();
    private final AtomicReference<String> queryRecebida = new AtomicReference<>();
    private final AtomicInteger chamadasOsrm = new AtomicInteger();
    private final AtomicReference<String> userAgentRecebido = new AtomicReference<>();

    // resposta que o OSRM falso devolve (cada teste pode trocar)
    private volatile int statusOsrm = 200;
    private volatile String corpoOsrm;

    private PollService pollService;

    @BeforeEach
    void subirServidoresFalsos() throws Exception {
        // ---------- Nominatim falso: devolve lat/lon (como TEXTO, igual ao real) conforme o endereço ----------
        nominatimFalso = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        nominatimFalso.createContext("/search", ex -> {
            userAgentRecebido.set(ex.getRequestHeaders().getFirst("User-Agent"));
            String q = parametro(ex.getRequestURI().getRawQuery(), "q");
            String json;
            if (q.startsWith("Rua A")) json = "[{\"lat\":\"-30.0100\",\"lon\":\"-51.2000\"}]";
            else if (q.startsWith("Rua B")) json = "[{\"lat\":\"-30.0200\",\"lon\":\"-51.2100\"}]";
            else if (q.startsWith("Rua C")) json = "[{\"lat\":\"-30.0300\",\"lon\":\"-51.2200\"}]";
            else json = "[]"; // endereço desconhecido
            responder(ex, 200, json);
        });
        nominatimFalso.start();

        // ---------- OSRM falso: registra a requisição e devolve a resposta configurada ----------
        // Entrada do app: [ORIGEM, Rua A, Rua B, Rua C, DESTINO]
        // Resposta: A fica em 3º, B em 1º, C em 2º  ==> ordem esperada: ORIGEM, B, C, A, DESTINO
        corpoOsrm = "{\"code\":\"Ok\","
                + "\"trips\":[{\"distance\":12400.0,\"duration\":1860.0,\"legs\":[]}],"
                + "\"waypoints\":["
                + "{\"waypoint_index\":0},"   // ORIGEM
                + "{\"waypoint_index\":3},"   // Rua A
                + "{\"waypoint_index\":1},"   // Rua B
                + "{\"waypoint_index\":2},"   // Rua C
                + "{\"waypoint_index\":4}"    // DESTINO
                + "]}";

        osrmFalso = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        osrmFalso.createContext("/trip/v1/driving/", ex -> {
            chamadasOsrm.incrementAndGet();
            caminhoRecebido.set(ex.getRequestURI().getPath());   // já decodificado
            queryRecebida.set(ex.getRequestURI().getQuery());
            responder(ex, statusOsrm, corpoOsrm);
        });
        osrmFalso.start();

        // ---------- Monta os services REAIS apontando para os servidores falsos ----------
        GeocodingService geocoding = new GeocodingService(
                "http://127.0.0.1:" + nominatimFalso.getAddress().getPort(), "EazyGo-Teste/1.0");
        OsrmService osrm = new OsrmService("http://127.0.0.1:" + osrmFalso.getAddress().getPort());

        RouteService routeService = new RouteService();
        definir(routeService, "geocodingService", geocoding);
        definir(routeService, "osrmService", osrm);
        definir(routeService, "origem", ORIGEM);
        definir(routeService, "destino", DESTINO);

        pollService = new PollService();
        definir(pollService, "routeService", routeService);
    }

    @AfterEach
    void derrubarServidores() {
        nominatimFalso.stop(0);
        osrmFalso.stop(0);
    }

    @Test
    void tresPassageirosConfirmam_rotaVemOtimizadaComDistanciaETempo() {
        responder("ana", "Rua A, 10", PollResponseStatus.CONFIRMADO);
        responder("bruno", "Rua B, 20", PollResponseStatus.CONFIRMADO);
        responder("carla", "Rua C, 30", PollResponseStatus.CONFIRMADO);
        // Diego confirmou e depois cancelou: vale a ÚLTIMA resposta, então ele NÃO entra na rota
        responder("diego", "Rua D, 40", PollResponseStatus.CONFIRMADO);
        responder("diego", "Rua D, 40", PollResponseStatus.CANCELADO);

        assertEquals(List.of("Rua A, 10", "Rua B, 20", "Rua C, 30"), pollService.enderecosConfirmadosDoDia());

        Optional<RotaOtimizada> resultado = pollService.calcularRotaDoDia();

        assertTrue(resultado.isPresent(), "deveria ter calculado a rota");
        RotaOtimizada rota = resultado.get();

        // metros -> km e segundos -> minutos
        assertEquals(12.4, rota.distanciaKm(), 0.001);
        assertEquals(31.0, rota.duracaoMin(), 0.001);

        // a ordem veio do waypoint_index do OSRM, com origem no começo e destino no fim
        assertEquals(List.of("ORIGEM", "Rua B, 20", "Rua C, 30", "Rua A, 10", "DESTINO"), rota.paradas());

        // conferindo o que o app ENVIOU ao OSRM: lon,lat, origem primeiro, destino por último
        String caminho = caminhoRecebido.get();
        assertEquals("/trip/v1/driving/"
                + "-51.228600,-30.033100;"   // origem
                + "-51.200000,-30.010000;"   // Rua A
                + "-51.210000,-30.020000;"   // Rua B
                + "-51.220000,-30.030000;"   // Rua C
                + "-51.183900,-29.918900",   // destino
                caminho);
        assertTrue(queryRecebida.get().contains("roundtrip=false"));
        assertTrue(queryRecebida.get().contains("source=first"));
        assertTrue(queryRecebida.get().contains("destination=last"));

        // Nominatim exige User-Agent identificando o app
        assertEquals("EazyGo-Teste/1.0", userAgentRecebido.get());
        assertEquals(1, chamadasOsrm.get(), "uma única chamada ao OSRM para todos os passageiros");
    }

    @Test
    void ninguemConfirmou_naoChamaOsrm() {
        responder("ana", "Rua A, 10", PollResponseStatus.CANCELADO);

        assertTrue(pollService.calcularRotaDoDia().isEmpty());
        assertEquals(0, chamadasOsrm.get());
    }

    @Test
    void osrmRetornaErro400_devolveVazioSemLancarExcecao() {
        statusOsrm = 400;
        corpoOsrm = "{\"code\":\"NoSegment\",\"message\":\"Could not find a matching segment\"}";
        responder("ana", "Rua A, 10", PollResponseStatus.CONFIRMADO);

        Optional<RotaOtimizada> resultado = assertDoesNotThrow(() -> pollService.calcularRotaDoDia());

        assertTrue(resultado.isEmpty());
        assertEquals(1, chamadasOsrm.get());
    }

    // ===================== auxiliares =====================

    // simula uma resposta já gravada na enquete de hoje (sem depender do horário de abertura/fechamento)
    private void responder(String usuario, String endereco, PollResponseStatus status) {
        DailyPoll poll = new DailyPoll();
        poll.setUsuario(usuario);
        poll.setEndereco(endereco);
        poll.setStatus(status);
        pollService.getRespostas().add(poll);
    }

    private static void responder(HttpExchange ex, int status, String json) throws IOException {
        byte[] corpo = json.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().add("Content-Type", "application/json");
        ex.sendResponseHeaders(status, corpo.length);
        ex.getResponseBody().write(corpo);
        ex.close();
    }

    private static String parametro(String rawQuery, String nome) {
        for (String par : rawQuery.split("&")) {
            String[] kv = par.split("=", 2);
            if (kv[0].equals(nome)) {
                return URLDecoder.decode(kv[1], StandardCharsets.UTF_8);
            }
        }
        return "";
    }

    // injeta campos privados (@Autowired/@Value) sem precisar subir o Spring
    private static void definir(Object alvo, String campo, Object valor) throws Exception {
        Field f = alvo.getClass().getDeclaredField(campo);
        f.setAccessible(true);
        f.set(alvo, valor);
    }
}
