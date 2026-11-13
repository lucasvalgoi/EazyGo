package com.eazygo.demo.Services;

import com.eazygo.demo.Entities.PollResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PollService {

    //guardar as respostas da enquete
    private List<PollResponse> respostas = new ArrayList<>();

    public String confirmarPresenca(){
        return"Presença Confirmada!";
    }
    public String cancelarPresenca(){
        return"Presença Cancelada!";
    }
}
