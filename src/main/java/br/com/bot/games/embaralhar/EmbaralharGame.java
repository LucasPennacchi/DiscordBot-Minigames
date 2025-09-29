package br.com.bot.games.embaralhar;

import br.com.bot.core.ConfigManager;
import br.com.bot.core.GameManager;
import br.com.bot.shared.Game;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

/**
 * Representa o estado e a lógica de um Jogo de Embaralhar Palavras.
 * <p>
 * Esta classe guarda a palavra original e implementa a lógica para verificar se a
 * tentativa de um jogador corresponde à palavra correta, finalizando o jogo em caso de acerto.
 *
 * @author Lucas
 */
public class EmbaralharGame extends Game {

    /** A palavra original, não embaralhada, que é a resposta correta. */
    private final String palavraOriginal;

    /**
     * Constrói uma nova instância do Jogo de Embaralhar.
     *
     * @param tempoLimiteMs   O tempo limite para o jogo em milissegundos.
     * @param palavraOriginal A palavra correta que os jogadores devem adivinhar.
     * @param issuerId        O ID do usuário que iniciou o jogo.
     */
    public EmbaralharGame(long tempoLimiteMs, String palavraOriginal, String issuerId) {
        super(tempoLimiteMs, issuerId);
        this.palavraOriginal = palavraOriginal;
    }

    /**
     * Retorna a palavra original e correta do jogo.
     *
     * @return A palavra original.
     */
    public String getPalavraOriginal() {
        return palavraOriginal;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Processa a mensagem de um jogador. Se a mensagem for igual à palavra original
     * (ignorando maiúsculas/minúsculas), o jogo é finalizado e o vencedor é anunciado.
     * Respostas incorretas são simplesmente ignoradas.
     */
    @Override
    protected void processarRespostaDoJogo(MessageReceivedEvent event, GameManager gameManager, ConfigManager configManager) {
        String respostaDoUsuario = event.getMessage().getContentRaw();
        if (respostaDoUsuario.equalsIgnoreCase(palavraOriginal)) {
            gameManager.finalizarJogo(event.getChannel().getId());
            long tempoDeReacao = System.currentTimeMillis() - getTempoInicio();
            String resultado = String.format(
                    "✅ **Correto!** %s desembaraçou a palavra em %.2f segundos!",
                    event.getAuthor().getAsMention(),
                    tempoDeReacao / 1000.0
            );
            event.getChannel().sendMessage(resultado).queue();
        }
    }
}