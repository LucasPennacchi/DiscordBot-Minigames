package br.com.bot.shared;

import br.com.bot.core.BotMain; // Importa o BotMain para acessar a variável estática
import br.com.bot.core.ConfigManager;
import br.com.bot.core.GameManager;
import br.com.bot.core.ServerConfig;
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

    protected abstract void processarRespostaDoJogo(MessageReceivedEvent event, GameManager gameManager, ConfigManager configManager);

    public void processarResposta(MessageReceivedEvent event, GameManager gameManager, ConfigManager configManager) {
        String guildId = event.getGuild().getId();
        ServerConfig config = configManager.getConfig(guildId);

        // A regra agora é baseada na configuração do servidor, não mais no DEV_MODE
        if (!config.isAllowCreatorToPlay()) {
            if (event.getAuthor().getId().equals(getIssuerId())) {
                return; // Bloqueia o criador
            }
        }
        processarRespostaDoJogo(event, gameManager, configManager);
    }
}