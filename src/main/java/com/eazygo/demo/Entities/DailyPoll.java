package com.eazygo.demo.Entities;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

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

    // CONSTRUTOR NOVO (vazio): o Spring usa esse para criar o objeto do formulário
    public DailyPoll() {
        LocalDate hoje = LocalDate.now();

        // antes das 4h ainda é a enquete de hoje; depois, é a de amanhã
        if (LocalTime.now().isBefore(LocalTime.of(4, 0))) {
            this.data = hoje;
        } else {
            this.data = hoje.plusDays(1);
        }

        this.horarioAbertura = this.data.minusDays(1).atTime(18, 30);
        this.horarioFechamento = this.data.atTime(4, 0);
    }

     public DailyPoll(LocalDate data) {
        this.data = data;
        this.horarioAbertura = data.minusDays(1).atTime(18,30);
        this.horarioFechamento = data.atTime(4,0);
    }

}
