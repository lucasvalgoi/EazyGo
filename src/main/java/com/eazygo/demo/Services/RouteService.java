package com.eazygo.demo.Services;

import com.eazygo.demo.Entities.Route;
import com.eazygo.demo.Entities.RotaOtimizada;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service//anotação da classe pertencente ao package de services
public class RouteService {

    @Autowired
    private GeocodingService geocodingService;

    @Autowired
    private OsrmService osrmService;

    // pontos fixos do trajeto, no formato "longitude,latitude" (definidos no application.properties)
    @Value("${eazygo.origem}")
    private String origem;

    @Value("${eazygo.destino}")
    private String destino;

    private static final List<Route> mockedDB = new ArrayList<>();//lista de informações mockadas para as rotas
    private static Long idCounter = 1L;//contador de ids para iteração dentro da lista de dados das rotas

    public RouteService() {
        // Exemplo de dado mockado para o usuário "1" para fins de teste do MVP
        Route r = new Route();
        r.setUserId(1L);
        r.setLocalNickname("usuario1");
        r.setStreet("Rua da Praia");
        r.setNumber("300");
        r.setNeighborhood("Centro Histórico");
        r.setCity("Porto Alegre");
        mockedDB.add(r);
    }

    //CRIAÇÃO DE ROTA + VERIFICAÇÃO MÁX. 3 ROTAS POR USER
    public Route createRoute(Long userId, Route newRoute){
        //verificação das rotas do usuário na lista
        //.stream() cria fluxo de elementos a partir da lista de dados
        long currentRoutsCount = mockedDB.stream()
                .filter(route -> route.getUserId().equals(userId))
                .count();//conta as rotas que passaram pelo filtro
        if (currentRoutsCount >= 3){
            //jogando uma nossa exceção durante tempo de execução
            throw new RuntimeException("Limite máximo de 3 rotas por usuário atingido");
        }
        newRoute.setId(idCounter++);//setando id pra rota incrementando +1
        newRoute.setUserId(userId);//setando userId à nova rota

        //
        if(currentRoutsCount == 0){
            newRoute.setActiveToday(true);
        }

        mockedDB.add(newRoute);//adicionando novos dados da rota a lista de dados mockados
        return newRoute;
    }

    //LISTAGEM DAS ROTAS DO USER
    public List<Route> getUserRoutes(Long userId){
        return mockedDB.stream()
                .filter(route -> route.getUserId().equals(userId))
                .collect(Collectors.toList());//captura todas as rotas que
        //passaram pelo filtro e joga em uma nova lista pronta para requisição
    }

    //ATUALIZAÇÃO DOS DADOS DA ROTA SELECIONADA PELO USER
    public Route updateRoute(Long routeId, Route updateData){
        Route existingRoute = mockedDB.stream()
                .filter(route -> route.getId().equals(routeId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Rota não encontrada"));

    //atualizando novos dados da rota já existente do usuário
        existingRoute.setLocalNickname(updateData.getLocalNickname());
        existingRoute.setStreet(updateData.getStreet());
        existingRoute.setNumber(updateData.getNumber());
        existingRoute.setNeighborhood(updateData.getNeighborhood());
        existingRoute.setCity(updateData.getCity());
        existingRoute.setZipCode(updateData.getZipCode());

        return existingRoute;
    }

    //SELEÇÃO DA ROTA PARA O DIA ATUAL
    public Route selectTodaysRoute(Long userId, Long routeId){
        List<Route> userRoutes = getUserRoutes(userId);
        Route selectedRoute = null;

        for(Route route : userRoutes){
            if(route.getId().equals(routeId)){
                route.setActiveToday(true);
                selectedRoute = route;
            } else {
                route.setActiveToday(false);
            }
        }
        if(selectedRoute == null){
            throw new RuntimeException("Rota não encontrada ou não pertencente ao usuário");
        }
        return selectedRoute;
    }

    public List<String> buscarEnderecos(String usuario) {
        return mockedDB.stream()
                .filter(r -> String.valueOf(r.getUserId()).equals(usuario) || r.getLocalNickname().equalsIgnoreCase(usuario))
                .map(r -> r.getStreet() + ", " + r.getNumber() + " - " + r.getNeighborhood() + ", " + r.getCity())
                .collect(Collectors.toList());
    }

    public void adicionarEnderecoSeNaoExistir(String usuario, String enderecoCompleto) {
        // Verifica se já existe rota para este usuário, se não, cria e adiciona ao mockDatabase
        boolean jaExiste = mockedDB.stream()
                .anyMatch(r -> String.valueOf(r.getUserId()).equals(usuario) || r.getLocalNickname().equalsIgnoreCase(usuario));

        if (!jaExiste) {
            Route novaRota = new Route();
            // Atribua os campos conforme os métodos da entidade Route de vocês
            novaRota.setLocalNickname(usuario);
            // Exemplo simples guardando a string ou quebrando em campos de endereço
            mockedDB.add(novaRota);
        }
    }

    /**
     * Recebe os endereços de TODOS os passageiros confirmados e devolve a rota otimizada:
     * endereço de texto -> coordenadas (Nominatim) -> melhor ordem, distância e tempo (OSRM Trip).
     */
    public Optional<RotaOtimizada> otimizarRotaDoDia(List<String> enderecosPassageiros) {
        List<String> nomes = new ArrayList<>();
        List<double[]> coordenadas = new ArrayList<>();

        nomes.add("ORIGEM");
        coordenadas.add(parseCoordenada(origem));

        // distinct(): dois passageiros no mesmo endereço viram uma única parada
        for (String endereco : enderecosPassageiros.stream().distinct().toList()) {
            Optional<double[]> coord = geocodingService.buscarCoordenadas(endereco);
            if (coord.isEmpty()) {
                System.err.println("[RouteService] Endereço ignorado (sem coordenadas): " + endereco);
                continue;
            }
            nomes.add(endereco);
            coordenadas.add(coord.get());
        }

        if (nomes.size() == 1) { // só sobrou a origem: nenhum passageiro com endereço válido
            return Optional.empty();
        }

        nomes.add("DESTINO");
        coordenadas.add(parseCoordenada(destino));

        return osrmService.otimizarRota(nomes, coordenadas);
    }

    // "-51.2286,-30.0331" -> {-51.2286, -30.0331}  (longitude primeiro, como o OSRM exige)
    private double[] parseCoordenada(String texto) {
        String[] partes = texto.split(",");
        return new double[]{Double.parseDouble(partes[0].trim()), Double.parseDouble(partes[1].trim())};
    }

}
