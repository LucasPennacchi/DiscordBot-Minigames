package br.com.bot.shared;

import br.com.bot.core.GameManager;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public abstract class Game {
    private final long tempoLimiteMs;
    private final long tempoInicio;

    public Game(long tempoLimiteMs) {
        this.tempoLimiteMs = tempoLimiteMs;
        this.tempoInicio = System.currentTimeMillis();
    }

    public long getTempoLimiteMs() { return tempoLimiteMs; }
    public long getTempoInicio() { return tempoInicio; }

    // NOVO: Metodo abstrato que define a "estratégia" de processamento de resposta.
    public abstract void processarResposta(MessageReceivedEvent event, GameManager gameManager);
}