package com.eazygo.demo.Entities;

// só serve para devolver se a enquete está aberta e qual o status do usuário.
// record só guarda dados
public record PollStatusResponse(boolean aberta, PollResponseStatus statusUsuario){}
