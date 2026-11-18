package com.eazygo.demo.Services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * O OSRM só entende COORDENADAS (longitude,latitude), não endereços de texto.
 * Este serviço converte "Rua da Praia, 300 - Centro Histórico, Porto Alegre" em {longitude, latitude}
 * usando o Nominatim (geocodificador gratuito do OpenStreetMap).
 *
 * Regras do Nominatim público: máx. 1 requisição por segundo e User-Agent identificando o app.
 */
@Service
public class GeocodingService {

    private final RestClient restClient;

    // cache: o mesmo endereço não precisa ser consultado duas vezes
    private final Map<String, double[]> cache = new ConcurrentHashMap<>();
    private long ultimaChamadaMs = 0;

    public GeocodingService(@Value("${nominatim.base-url}") String baseUrl,
                            @Value("${nominatim.user-agent}") String userAgent) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5));
        factory.setReadTimeout(Duration.ofSeconds(10));

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(factory)
                .defaultHeader("User-Agent", userAgent)
                .build();
    }

    /** @return {longitude, latitude} (ordem que o OSRM exige) ou vazio se não achou o endereço */
    public Optional<double[]> buscarCoordenadas(String endereco) {
        if (endereco == null || endereco.isBlank()) {
            return Optional.empty();
        }

        double[] emCache = cache.get(endereco);
        if (emCache != null) {
            return Optional.of(emCache);
        }

        Optional<double[]> resultado = consultarNominatim(endereco);
        resultado.ifPresent(c -> cache.put(endereco, c));
        return resultado;
    }

    // synchronized + pausa = garante no máximo 1 consulta por segundo ao Nominatim
    private synchronized Optional<double[]> consultarNominatim(String endereco) {
        long espera = 1100 - (System.currentTimeMillis() - ultimaChamadaMs);
        if (espera > 0) {
            try {
                Thread.sleep(espera);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return Optional.empty();
            }
        }
        ultimaChamadaMs = System.currentTimeMillis();

        try {
            List<Map<String, Object>> resposta = restClient.get()
                    .uri("/search?q={q}&format=jsonv2&limit=1&countrycodes=br", endereco + ", Brasil")
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<Map<String, Object>>>() {});

            if (resposta == null || resposta.isEmpty()) {
                System.err.println("[Geocoding] Endereço não encontrado: " + endereco);
                return Optional.empty();
            }

            // o Nominatim devolve lat/lon como TEXTO
            double lat = Double.parseDouble(String.valueOf(resposta.get(0).get("lat")));
            double lon = Double.parseDouble(String.valueOf(resposta.get(0).get("lon")));
            return Optional.of(new double[]{lon, lat});

        } catch (RestClientException | NumberFormatException e) {
            System.err.println("[Geocoding] Falha ao consultar '" + endereco + "': " + e.getMessage());
            return Optional.empty();
        }
    }
}
