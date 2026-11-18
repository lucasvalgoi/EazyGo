package com.eazygo.demo.Entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor

public class PassengersDTO 
{
    private int id;
    private String nome;
    private String foto;
    private String telefone;
}