package com.eazygo.demo.Services;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class PollResponse {
    private LocalDateTime horario; //horario da resposta
    private String usuario;
    private DailyPoll enquete;
    private StatusResposta status;

    public PollResponse(LocalDateTime horario, String usuario, DailyPoll enquete, StatusResposta status, ) {
        this.horario = LocalDateTime.now();
        this.usuario = usuario;
        this.enquete = enquete;
        this.status = status;
    }
}

enum StatusResposta {CONFIRMADO, CANCELADO}
