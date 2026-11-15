package com.eazygo.demo.Controller;
import com.eazygo.demo.Entities.DailyPoll;
import com.eazygo.demo.Services.PollService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PollController {

    private PollService pollService;
    public DailyPoll poll;

    public PollController(PollService pollService){
        this.pollService = pollService;
    }

    @PostMapping ("/api/poll/confirmar")
    public String confirmarPresenca(){
        return pollService.confirmarPresenca(poll);
    }

    @PostMapping ("/api/poll/cancelar")
    public String cancelarPresenca(){
        return pollService.cancelarPresenca();
    }

}
