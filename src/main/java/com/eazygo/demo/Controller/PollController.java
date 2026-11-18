package com.eazygo.demo.Controller;
import com.eazygo.demo.Entities.DailyPoll;
import com.eazygo.demo.Entities.PollStatusResponse;
import com.eazygo.demo.Services.PollService;
import org.springframework.web.bind.annotation.*;

@RestController
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

    @GetMapping("/api/poll/status")
    //@RequestParam: usado para filtrar a url (/users?nome=maria&idade=25)
    public PollStatusResponse status(@RequestParam String usuario) {
        return pollService.consultarStatus(usuario);
    }
}
