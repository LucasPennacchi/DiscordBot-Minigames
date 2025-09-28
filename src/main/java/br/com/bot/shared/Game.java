package br.com.bot.shared;

import br.com.bot.core.BotMain; // Importa o BotMain para acessar a variável estática
import br.com.bot.core.GameManager;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public abstract class Game {
    private final long tempoLimiteMs;
    private final long tempoInicio;
    private final String issuerId;

    public Game(long tempoLimiteMs, String issuerId) {
        this.tempoLimiteMs = tempoLimiteMs;
        this.tempoInicio = System.currentTimeMillis();
        this.issuerId = issuerId;
    }

    public long getTempoLimiteMs() { return tempoLimiteMs; }
    public long getTempoInicio() { return tempoInicio; }
    public String getIssuerId() { return issuerId; }

    public void processarResposta(MessageReceivedEvent event, GameManager gameManager) {

        if (event.getAuthor().getId().equals(getIssuerId())) {
            // A condição para permitir que o criador jogue é ser DEV_MODE E estar no servidor de teste.
            boolean isCreatorAllowedToPlay = BotMain.IS_DEV_MODE && event.getGuild().getId().equals(BotMain.ID_SERVIDOR_TESTE);

            // Se a condição especial NÃO for atendida, bloqueia a resposta e encerra.
            if (!isCreatorAllowedToPlay) {
                return;
            }
        }

        processarRespostaDoJogo(event, gameManager);
    }

    protected abstract void processarRespostaDoJogo(MessageReceivedEvent event, GameManager gameManager);
}