package com.eazygo.demo.Entities;
import com.eazygo.demo.Entities.PollResponseStatus;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class PollResponse {
    private int id; //identificacao de cada pessoa
    private LocalDateTime horarioResposta = LocalDateTime.now();
    private String usuario;
    private DailyPoll enquete;
    private PollResponseStatus status;

    public PollResponse(String usuario, DailyPoll enquete, PollResponseStatus status) {
        this.usuario = usuario;
        this.enquete = enquete;
        this.status = status;
    }
}
