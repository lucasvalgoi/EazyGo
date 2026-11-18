package com.eazygo.demo.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.ui.Model;

import java.util.ArrayList;
import java.util.List;

@Controller // retorna o nome de uma pagina
public class PageController {

    // model leva os dados do controller para a pagina da enquete
    @GetMapping("/enquete")
    public String enquete(@RequestParam String usuario, Model model){
        model.addAttribute("usuario", usuario); // usar o nome usuario para pegar o nome da URL

        // trocar para o service larp do valgoi
        List<String> enderecos = List.of("Rua A, 10", "Rua B, 20");
        model.addAttribute("enderecos", enderecos);

        return "enquete"; // nome do arquivo enquete.html
    }
}
