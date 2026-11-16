package com.eazygo.demo.Services;

import com.eazygo.demo.Entities.Route;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service//anotação da classe pertencente ao package de services
public class RouteService {
    private static final List<Route> mockedDB = new ArrayList<>();//lista de informações mockadas para as rotas
    private static Long idCounter = 1L;//contador de ids para iteração dentro da lista de dados das rotas

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

    public List<Route> getUserRoutes(Long userId){
        return mockedDB.stream()
                .filter(route -> route.getUserId().equals(userId))
                .collect(Collectors.toList());//captura todas as rotas que
        //passaram pelo filtro e joga em uma nova lista pronta para requisição
    }

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
}
