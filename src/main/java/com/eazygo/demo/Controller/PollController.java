package com.eazygo.demo.Controller;
import com.eazygo.demo.Entities.DailyPoll;
import com.eazygo.demo.Services.PollService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@Controller
public class PollController {

    private final PollService pollService;
    private DailyPoll poll;

    public PollController(PollService pollService){
        this.pollService = pollService;
    }

    @PostMapping ("/api/poll/confirmar")
    public String confirmarPresenca(@ModelAttribute DailyPoll poll){
        return pollService.confirmarPresenca(poll);
    }

    @PostMapping ("/api/poll/cancelar")
    public String cancelarPresenca(@ModelAttribute DailyPoll poll){
        return pollService.cancelarPresenca(poll);
    }

}
