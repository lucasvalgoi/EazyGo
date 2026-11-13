package com.eazygo.demo.Entities;

import com.eazygo.demo.Entities.StatusResposta;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class PollResponse {
    private int id; //identificacao de cada pessoa
    private LocalDateTime horarioResposta;
    private String usuario;
    private DailyPoll enquete;
    private StatusResposta status;

    public PollResponse(LocalDateTime horarioResposta, String usuario, DailyPoll enquete, StatusResposta status) {
        this.horarioResposta = LocalDateTime.now();
        this.usuario = usuario;
        this.enquete = enquete;
        this.status = status;
    }

    public void confirmarParticipacao() {

    }
}

enum StatusResposta {CONFIRMADO, CANCELADO}
