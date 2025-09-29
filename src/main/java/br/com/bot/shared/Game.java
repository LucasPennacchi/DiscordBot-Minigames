package br.com.bot.shared;

import br.com.bot.core.ConfigManager;
import br.com.bot.core.GameManager;
import br.com.bot.core.ServerConfig;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

/**
 * Classe base abstrata para o estado de todos os jogos.
 * <p>
 * Contém a lógica e os campos comuns a qualquer partida, como tempo limite,
 * tempo de início e a regra de quem pode responder, que é lida
 * a partir das configurações do servidor.
 *
 * @author Lucas
 */
public abstract class Game {
    private final long tempoLimiteMs;
    private final long tempoInicio;
    private final String issuerId;

    /**
     * Construtor para um novo estado de jogo.
     * @param tempoLimiteMs O tempo limite para o jogo em milissegundos.
     * @param issuerId O ID do usuário que iniciou o jogo.
     */
    public Game(long tempoLimiteMs, String issuerId) {
        this.tempoLimiteMs = tempoLimiteMs;
        this.tempoInicio = System.currentTimeMillis();
        this.issuerId = issuerId;
    }

    public long getTempoLimiteMs() { return tempoLimiteMs; }
    public long getTempoInicio() { return tempoInicio; }
    public String getIssuerId() { return issuerId; }

    /**
     * Processa uma tentativa de resposta de um jogador.
     * Este método contém a lógica de verificação para bloquear o criador do jogo de responder,
     * baseando-se na configuração salva para o servidor específico.
     *
     * @param event O evento da mensagem recebida.
     * @param gameManager O gerenciador de jogos ativos.
     * @param configManager O gerenciador de configurações de servidor.
     */
    public void processarResposta(MessageReceivedEvent event, GameManager gameManager, ConfigManager configManager) {
        // Pega a configuração específica do servidor onde a mensagem foi enviada.
        ServerConfig config = configManager.getConfig(event.getGuild().getId());

        // A verificação é baseada SOMENTE na configuração daquele servidor.
        if (!config.isAllowCreatorToPlay()) {
            if (event.getAuthor().getId().equals(getIssuerId())) {
                return; // Bloqueia o criador se a configuração do servidor assim o exigir.
            }
        }

        // Se a regra for passada, delega para a lógica específica do jogo.
        processarRespostaDoJogo(event, gameManager, configManager);
    }

    /**
     * Método abstrato que as subclasses devem implementar com a lógica de validação
     * específica para cada tipo de jogo.
     *
     * @param event O evento da mensagem recebida.
     * @param gameManager O gerenciador de jogos ativos.
     * @param configManager O gerenciador de configurações de servidor.
     */
    protected abstract void processarRespostaDoJogo(MessageReceivedEvent event, GameManager gameManager, ConfigManager configManager);
}