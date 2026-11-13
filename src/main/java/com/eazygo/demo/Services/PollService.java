package com.eazygo.demo.Services;

import com.eazygo.demo.Entities.DailyPoll;
import com.eazygo.demo.Entities.PollResponse;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class PollService {

    private List<PollResponse> respostas = new ArrayList<>(); //guardar as respostas da enquete
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
            return "Presença confirmada!";
        }
        else{
            return "Presença não confirmada! Fora do horário estabelecido.";
        }
    }

    public String cancelarPresenca(){
        return"Presença Cancelada!";
    }
}
