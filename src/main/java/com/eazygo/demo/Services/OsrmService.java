package com.eazygo.demo.Services;

import com.eazygo.demo.Entities.RotaOtimizada;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OsrmService {

    private final RestTemplate restTemplate = new RestTemplate();

    // cliente HTTP usado pelo novo método otimizarRota (URL base vem do application.properties)
    private final RestClient restClient;

    public OsrmService(@Value("${osrm.base-url}") String baseUrl) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5));
        factory.setReadTimeout(Duration.ofSeconds(15));
        this.restClient = RestClient.builder().baseUrl(baseUrl).requestFactory(factory).build();
    }

    /**
     * Calcula a rota utilizando a API pública e gratuita do OSRM.
     * @param coordenadasString String com as coordenadas no formato "longitude,latitude;longitude,latitude..."
     */
    public void calcularRotaPorCoordenadas(String coordenadasString) {
        // Endpoint público do OSRM para veículos (Driving profile)
        String url = "http://router.project-osrm.org/route/v1/driving/" + coordenadasString + "?overview=false";

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response != null && "Ok".equals(response.get("code"))) {
                // Usando o curinga '?' para evitar qualquer erro de cópia de caracteres < >
                List<?> routes = (List<?>) response.get("routes");

                if (routes != null && !routes.isEmpty()) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> route = (Map<String, Object>) routes.getFirst();

                    double distanciaMetros = ((Number) route.get("distance")).doubleValue();
                    double duracaoSegundos = ((Number) route.get("duration")).doubleValue();

                    double distanciaKm = distanciaMetros / 1000.0;
                    double tempoMinutos = duracaoSegundos / 60.0;

                    // Exibição limpa no console
                    System.out.println("\n==================================================");
                    System.out.println(" 🚗 RELATÓRIO DE ROTA OSRM (100% GRATUITO) 🚗 ");
                    System.out.println("==================================================");
                    System.out.println("👉 Distância Total do Percurso: " + String.format("%.2f km", distanciaKm));
                    System.out.println("⏱️ Tempo Estimado de Trânsito (ETA): " + String.format("%.0f minutos", tempoMinutos));
                    System.out.println("==================================================\n");
                }
            } else {
                System.out.println("[OSRM] Erro ao processar rota na API.");
            }
        } catch (Exception e) {
            System.err.println("[OSRM] Exceção na requisição: " + e.getMessage());
        }
    }

    /**
     * OTIMIZA a ordem das paradas usando o serviço TRIP do OSRM (resolve o "caixeiro viajante").
     *
     * Contrato da lista de entrada:
     *  - posição 0        = ORIGEM  (onde o motorista começa)  -> fica sempre em primeiro
     *  - posição final    = DESTINO (para onde todos vão)      -> fica sempre por último
     *  - posições do meio = passageiros, que o OSRM reordena para achar o melhor caminho
     *
     * @param nomes        rótulos de cada ponto (ex.: o endereço), na MESMA ordem das coordenadas
     * @param coordenadas  {longitude, latitude} de cada ponto (OSRM usa lon antes de lat!)
     * @return distância total, tempo total e as paradas já na ordem otimizada; vazio se o OSRM falhar
     */
    public Optional<RotaOtimizada> otimizarRota(List<String> nomes, List<double[]> coordenadas) {
        if (coordenadas.size() < 2 || nomes.size() != coordenadas.size()) {
            return Optional.empty();
        }

        // "lon,lat;lon,lat;..."  (Locale.US garante ponto como separador decimal)
        String coords = coordenadas.stream()
                .map(c -> String.format(Locale.US, "%.6f,%.6f", c[0], c[1]))
                .collect(Collectors.joining(";"));

        // roundtrip=false&source=first&destination=last -> começa no primeiro ponto e termina no último
        // overview=false -> não precisamos do desenho da rota agora, só de distância/tempo/ordem
        String url = "/trip/v1/driving/" + coords
                + "?roundtrip=false&source=first&destination=last&overview=false";

        try {
            Map<String, Object> resposta = restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {});

            if (resposta == null || !"Ok".equals(resposta.get("code"))) {
                System.err.println("[OSRM] Trip não retornou Ok: " + (resposta == null ? "sem resposta" : resposta.get("code")));
                return Optional.empty();
            }

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> trips = (List<Map<String, Object>>) resposta.get("trips");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> waypoints = (List<Map<String, Object>>) resposta.get("waypoints");

            if (trips == null || trips.isEmpty() || waypoints == null) {
                return Optional.empty();
            }

            double distanciaKm = ((Number) trips.getFirst().get("distance")).doubleValue() / 1000.0; // metros -> km
            double duracaoMin = ((Number) trips.getFirst().get("duration")).doubleValue() / 60.0;    // segundos -> min

            // "waypoints" vem na MESMA ordem que enviamos; cada um traz "waypoint_index" = posição dele na rota otimizada.
            // Aqui invertemos isso: ordemFinal[posição na rota] = nome do ponto
            String[] ordemFinal = new String[waypoints.size()];
            for (int i = 0; i < waypoints.size(); i++) {
                int posicao = ((Number) waypoints.get(i).get("waypoint_index")).intValue();
                ordemFinal[posicao] = nomes.get(i);
            }

            return Optional.of(new RotaOtimizada(distanciaKm, duracaoMin, List.of(ordemFinal)));

        } catch (RestClientException e) {
            // inclui HTTP 400 do OSRM (NoSegment, NoTrips, InvalidQuery...) e falhas de rede/timeout
            System.err.println("[OSRM] Falha ao chamar o Trip: " + e.getMessage());
            return Optional.empty();
        }
    }
}