package br.com.bot.games.memoria;

import br.com.bot.core.ConfigManager;
import br.com.bot.core.GameManager;
import br.com.bot.shared.Game;
import br.com.bot.utils.NormalizadorDeTexto;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

/**
 * Representa o estado e a lógica de um Jogo da Memória.
 * <p>
 * Esta classe guarda a string que deve ser memorizada e implementa a lógica
 * para verificar se a tentativa de um jogador corresponde à string correta.
 *
 * @author Lucas
 */
public class MemoriaGame extends Game {

    /** A string original que os jogadores devem memorizar e digitar. */
    private final String stringSecreta;

    /** O ID da mensagem do Discord que exibe o desafio, para que ela possa ser editada. */
    private String messageId;

    /**
     * Constrói uma nova instância do Jogo da Memória.
     *
     * @param tempoLimiteMs O tempo limite para o jogo em milissegundos.
     * @param stringSecreta A string correta que os jogadores devem adivinhar.
     * @param issuerId      O ID do usuário que iniciou o jogo.
     */
    public MemoriaGame(long tempoLimiteMs, String stringSecreta, String issuerId) {
        super(tempoLimiteMs, issuerId);
        this.stringSecreta = stringSecreta;
    }

    /**
     * Define o ID da mensagem do Discord que contém o desafio do jogo.
     * Necessário para que o bot possa editar a mensagem de "memorize" para "qual era a string?".
     *
     * @param messageId O ID da mensagem a ser guardada.
     */
    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    /**
     * Retorna a string secreta e correta do jogo.
     *
     * @return A string secreta.
     */
    public String getStringSecreta() {
        return stringSecreta;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Processa a mensagem de um jogador. A resposta é normalizada (ignorando acentos e
     * maiúsculas/minúsculas) e comparada com a string secreta. Se corresponder,
     * o jogo é finalizado e o vencedor é anunciado. Respostas incorretas são ignoradas.
     */
    @Override
    protected void processarRespostaDoJogo(MessageReceivedEvent event, GameManager gameManager, ConfigManager configManager) {
        String respostaDoUsuario = event.getMessage().getContentRaw();

        // Normaliza a resposta para uma comparação justa
        String respostaNormalizadaUsuario = NormalizadorDeTexto.removerAcentos(respostaDoUsuario.trim().toLowerCase());
        String respostaNormalizadaCorreta = NormalizadorDeTexto.removerAcentos(getStringSecreta().trim().toLowerCase());

        if (respostaNormalizadaUsuario.equals(respostaNormalizadaCorreta)) {
            // Se a resposta estiver correta, finaliza o jogo e anuncia o vencedor
            gameManager.finalizarJogo(event.getChannel().getId());
            long tempoDeReacao = System.currentTimeMillis() - getTempoInicio();

            String resultado = String.format(
                    "🧠 **Memória Incrível!** %s acertou em %.2f segundos!",
                    event.getAuthor().getAsMention(),
                    tempoDeReacao / 1000.0
            );
            event.getChannel().sendMessage(resultado).queue();
        }
        // Se a resposta estiver errada, o método termina, permitindo que outros jogadores tentem.
    }
}