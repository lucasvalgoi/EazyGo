package com.eazygo.demo.Controller;
import com.eazygo.demo.Services.PollService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PollController {

    private PollService pollService;

    public PollController(PollService pollService){
        this.pollService = pollService;
    }

    @PostMapping ("/api/poll/confirmar")
    public String confirmarPresenca(){
        return pollService.confirmarPresenca();
    }

    @PostMapping ("/api/poll/cancelar")
    public String cancelarPresenca(){
        return pollService.cancelarPresenca();
    }

}
