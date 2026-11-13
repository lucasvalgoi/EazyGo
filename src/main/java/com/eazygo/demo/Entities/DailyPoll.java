package com.eazygo.demo.Entities;
import lombok.Getter;
import java.time.LocalDate;
import java.time.LocalDateTime;

/* CLASSE DA ENQUETE */

@Getter
public class DailyPoll {
    private int id; // identificar enquete do dia
    private LocalDate data; //data da enquete
    private LocalDateTime horarioAbertura;
    private LocalDateTime horarioFechamento;

     public DailyPoll(LocalDate data) {
        this.data = data;
        this.horarioAbertura = data.minusDays(1).atTime(18,30);
        this.horarioFechamento = data.atTime(4,0);
    }

    public LocalDateTime getHorarioAbertura() {
        return horarioAbertura;
    }

    public LocalDateTime getHorarioFechamento() {
        return horarioFechamento;
    }
}
