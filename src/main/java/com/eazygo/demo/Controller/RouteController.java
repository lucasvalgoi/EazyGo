package com.eazygo.demo.Controller;

import com.eazygo.demo.Entities.Route;
import com.eazygo.demo.Services.RouteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController//classe passa a ser um controller REST (API REST - respostas convertidas em JSON)
@RequestMapping("/api/route")//endereço base das requisições desse controller
public class RouteController {
    @Autowired//injeção de dependência para a classe routeService
    private RouteService routeService;

    //POST /api/routes?userId=1
    @PostMapping//usa da requisição para criar uma nova rota pelo usuário
    //@RequestParam - pega um parâmetro passado diretamente na URL
    //@RequestBody - pega o JSON retornado no corpo da requisição HTTP e transforma automaticamente em um objeto
    public ResponseEntity<Route> create(@RequestParam Long userId, @RequestBody Route route){
    //ResponseEntity<> objeto do Spring que representa a resposta HTTP completa que será enviada
    //de volta por quem chamou a API e permite o controle dos status das responses
        return ResponseEntity.ok(routeService.createRoute(userId, route));
    ////envia de volta o status HTTP + coloca dentro do corpo da resposta o objeto em formato JSON
    }

    //GET /api/routes?userId=1
    @GetMapping//usa da requisição para listar/buscar os dados das rotas do usuário
    public ResponseEntity<List<Route>> listRoutesByUser(@RequestParam Long userId){
        return ResponseEntity.ok(routeService.getUserRoutes(userId));
    }

    //PUT /api/routes/{id}
    @PutMapping("/{id}")//usa da requisição para atualizar o registro existente do usuário
    //@PathVariable - pega a variável que está no caminho da URL
    public ResponseEntity<Route> updateDataRoute(@PathVariable Long id, @RequestBody Route route){
        return ResponseEntity.ok(routeService.updateRoute(id, route));
    }

    //PATCH /api/routes/{id}/select-today?userId=1
    @PatchMapping("/{id}/select-today")//usa da requisição para alterar parcialmente um recurso (status de atividade do usuário no dia atual)
    public ResponseEntity<Route> selectRouteForToday(@PathVariable Long id, @RequestBody Long userId){
        return ResponseEntity.ok(routeService.selectTodaysRoute(id, userId));
    }

}
