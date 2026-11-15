package com.eazygo.demo.Entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data //gera métodos (getter/setter...) boilerplate para uso da aplicação
@AllArgsConstructor //gera automaticamente um construtor com parêmetro pra cada campo
@NoArgsConstructor //criar um construtor sem argumentos
public class Route {
    private Long id;//id usado na requisição da rota
    private String localNickname;//"apelido" de identificação do local ("Casa", "Trabalho" ...)
    private String street;//nome da rua do endereço
    private String number;//número do endereço
    private String neighborhood;//bairro do endereço
    private String city;//cidade do endereço
    private String zipCode;//CEP do endereço
    private boolean activeToday = false;//status de atividade para o dia atual
    private Long userId;//id de identificação do usuário
}
