package com.eazygo.demo.Entities;

import java.util.List;

// Resultado da otimização de rota (mesmo estilo do PollStatusResponse: record só guarda dados).
// paradas: endereços na ORDEM em que o motorista deve passar (inclui ORIGEM no começo e DESTINO no fim)
public record RotaOtimizada(double distanciaKm, double duracaoMin, List<String> paradas) {}
