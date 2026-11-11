package com.eazygo.demo.Services;
import com.eazygo.demo.DailyPoll;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

/* CLASSE DE RESPOSTAS ENQUETE */

@Getter
@Setter
@Service
public class PollResponse {
    private int id; //identificacao de cada pessoa
    private LocalDateTime horarioResposta;
    private String usuario;
    private DailyPoll enquete;
    private StatusResposta status;

    public PollResponse(LocalDateTime horarioResposta, String usuario, DailyPoll enquete) {
        this.horarioResposta = LocalDateTime.now();
        this.usuario = usuario;
        this.enquete = enquete;
        this.status = status;

    }
}

enum StatusResposta {CONFIRMADO, CANCELADO}
