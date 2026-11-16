package com.eazygo.demo.Entities;

public class PassengersDTO {
    private int id;
    private String nome;
    private String telefone;
    private String foto;

    public PassengersDTO(int id, String nome, String foto, String telefone) {
        this.id = id;
        this.nome = nome;
        this.foto = foto;
        this.telefone = telefone;
    }

    // Lembre-se de adicionar os Getters e Setters abaixo
}
