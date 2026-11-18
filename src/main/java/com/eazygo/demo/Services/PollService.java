package com.eazygo.demo.Services;

import com.eazygo.demo.Entities.DailyPoll;
import lombok.Getter;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.eazygo.demo.Entities.PollResponseStatus.*;

@Service
public class PollService {

    @Getter
    private List<DailyPoll> respostas = new ArrayList<>(); //guardar as respostas da enquete
    private String Usuario;

    private boolean validarHorario(DailyPoll poll) {

        // variavel definida dentro do metodo para pegar o horario atual de quando ele for chamado
        LocalDateTime horarioAtual = LocalDateTime.now();

        if(horarioAtual.isAfter(poll.getHorarioFechamento())){
            return false;
        }
        else if(horarioAtual.isBefore(poll.getHorarioAbertura())){
            return false;
        }
        else if(horarioAtual.isEqual(poll.getHorarioFechamento())){
            return false;
        }
        else {
            return true;
        }
    }

    public String confirmarPresenca(DailyPoll poll){
        if(validarHorario(poll)){
            poll.setStatus(CONFIRMADO);
            respostas.add(poll);
            // Ver no terminal as respostas guardas
            System.out.println(respostas.size() + " resposta(s) guardada(s)");
            return "Presença Confirmada";
        }
        else{
            poll.setStatus(PENDENTE);
            System.out.println(respostas.size() + " resposta(s) guardada(s)");
            return "Presença Cancelada. Você está fora do horário programado!";
        }

    }

    public String cancelarPresenca(DailyPoll poll){
        poll.setStatus(CANCELADO);
        respostas.add(poll);
        System.out.println(respostas.size() + " resposta(s) guardada(s)");
        return "Presença Cancelada";
    }
}
