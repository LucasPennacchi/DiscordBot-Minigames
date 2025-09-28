package br.com.bot.shared;

import br.com.bot.core.GameManager;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public abstract class Game {
    private final long tempoLimiteMs;
    private final long tempoInicio;
    private final String issuerId; // NOVO: Guarda o ID de quem iniciou o jogo

    public Game(long tempoLimiteMs, String issuerId) {
        this.tempoLimiteMs = tempoLimiteMs;
        this.tempoInicio = System.currentTimeMillis();
        this.issuerId = issuerId; // NOVO
    }

    public long getTempoLimiteMs() { return tempoLimiteMs; }
    public long getTempoInicio() { return tempoInicio; }
    public String getIssuerId() { return issuerId; } // NOVO

    // O método de processar resposta agora tem uma verificação inicial
    public void processarResposta(MessageReceivedEvent event, GameManager gameManager) {
        // VERIFICAÇÃO PRINCIPAL: Se o autor da mensagem é quem iniciou o jogo.
        if (event.getAuthor().getId().equals(getIssuerId())) {
            // Opcional: Enviar uma mensagem efêmera ou simplesmente ignorar.
            // Por enquanto, vamos apenas ignorar a resposta.
            return;
        }
        // Chama o método específico de cada jogo.
        processarRespostaDoJogo(event, gameManager);
    }

    // NOVO: Método abstrato para a lógica específica de cada jogo
    protected abstract void processarRespostaDoJogo(MessageReceivedEvent event, GameManager gameManager);
}