package com.eazygo.demo.Entities;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/* CLASSE DA ENQUETE */

@Getter
public class DailyPoll {

    @Getter
    @Setter
    private String usuario;

    @Getter
    @Setter
    private PollResponseStatus status;

    private LocalDate data; //data da enquete

    @Getter
    private LocalDateTime horarioAbertura;

    @Getter
    private LocalDateTime horarioFechamento;

     public DailyPoll(LocalDate data) {
        this.data = data;
        this.horarioAbertura = data.minusDays(1).atTime(18,30);
        this.horarioFechamento = data.atTime(4,0);
    }

}
