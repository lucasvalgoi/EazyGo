package com.eazygo.demo.Services;

import com.eazygo.demo.Entities.DailyPoll;
import com.eazygo.demo.Entities.PollResponseStatus;
import com.eazygo.demo.Entities.PollStatusResponse;
import com.eazygo.demo.Entities.RotaOtimizada;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
            recalcularRota(); // alguém confirmou -> recalcula a rota com todos os confirmados
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
        recalcularRota(); // quem cancelou sai da rota -> recalcula
        return "Presença Cancelada";
    }

    // serve para saber se a enquete está aberta e ver o que o usuario respondeu
    public PollStatusResponse consultarStatus(String usuario) {
        DailyPoll enquete = new DailyPoll(); // usa o construtor vazio criado para o questionario
        boolean aberta = validarHorario(enquete);

        PollResponseStatus status = null; // null = usuario ainda não respondeu a enquete

        // vê as respostas guardadas dos usuarios do dia atual, verifica se a pessoa respondeu mais de uma vez
        for (DailyPoll resposta : respostas) {
            if (resposta.getUsuario().equals(usuario)) {
                status = resposta.getStatus();
            }
        }

        return new PollStatusResponse(aberta, status);
    }

    @Autowired
    private RouteService routeService;

    @Autowired
    private OsrmService osrmService;

    // ... (os métodos existentes de enquete continuam aqui) ...

    /**
     * Exemplo de método que processa a resposta da enquete do usuário.
     * Se a resposta for de confirmação, cadastra o endereço e recalcula a rota.
     */
    public void registrarRespostaEnquete(String usuario, String enderecoUsuario, boolean confirmado) {
        // Agora reaproveita o fluxo oficial da enquete (mesmo do formulário), sem coordenadas fixas
        DailyPoll poll = new DailyPoll();
        poll.setUsuario(usuario);
        poll.setEndereco(enderecoUsuario);

        if (confirmado) {
            confirmarPresenca(poll);  // guarda a resposta e já recalcula a rota
        } else {
            cancelarPresenca(poll);
        }
    }

    /**
     * Lista os endereços dos passageiros CONFIRMADOS na enquete de hoje.
     * Se a mesma pessoa respondeu mais de uma vez, vale só a ÚLTIMA resposta dela.
     */
    public List<String> enderecosConfirmadosDoDia() {
        LocalDate dataEnquete = new DailyPoll().getData();
        Map<String, DailyPoll> ultimaRespostaPorUsuario = new LinkedHashMap<>();

        for (DailyPoll resposta : respostas) {
            if (dataEnquete.equals(resposta.getData())) {
                ultimaRespostaPorUsuario.put(resposta.getUsuario(), resposta); // sobrescreve respostas antigas
            }
        }

        return ultimaRespostaPorUsuario.values().stream()
                .filter(r -> r.getStatus() == CONFIRMADO)
                .map(DailyPoll::getEndereco)
                .filter(e -> e != null && !e.isBlank())
                .toList();
    }

    /** Calcula a rota otimizada com todos os confirmados (vazio se ninguém confirmou ou se o OSRM falhar). */
    public Optional<RotaOtimizada> calcularRotaDoDia() {
        List<String> enderecos = enderecosConfirmadosDoDia();
        if (enderecos.isEmpty()) {
            return Optional.empty();
        }
        return routeService.otimizarRotaDoDia(enderecos);
    }

    // chamado a cada confirmação/cancelamento; um erro aqui NUNCA pode derrubar o registro da resposta
    private void recalcularRota() {
        try {
            calcularRotaDoDia().ifPresentOrElse(
                    rota -> System.out.printf("[PollService] Rota recalculada: %.2f km | %.0f min | ordem: %s%n",
                            rota.distanciaKm(), rota.duracaoMin(), rota.paradas()),
                    () -> System.out.println("[PollService] Sem rota para calcular (nenhum confirmado ou serviço indisponível)."));
        } catch (Exception e) {
            System.err.println("[PollService] Erro ao recalcular rota: " + e.getMessage());
        }
    }

}
