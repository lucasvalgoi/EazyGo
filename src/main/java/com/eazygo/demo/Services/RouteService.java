package com.eazygo.demo.Services;

import com.eazygo.demo.Entities.Route;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

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
}
