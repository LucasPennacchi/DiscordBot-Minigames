package br.com.bot.games.reflexo;

import br.com.bot.core.ConfigManager;
import br.com.bot.core.GameManager;
import br.com.bot.shared.Game;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

/**
 * Representa o estado e a lógica de um Jogo de Reflexo.
 * <p>
 * Esta classe guarda a frase que deve ser digitada e, ao receber a primeira resposta,
 * finaliza o jogo e calcula a pontuação do jogador com base na precisão da digitação,
 * utilizando a classe {@link VerificadorDePontos}.
 *
 * @author Lucas
 */
public class ReflexoGame extends Game {

    /** A frase correta que o jogador deve digitar. */
    private final String fraseCorreta;

    /** Instância do verificador de pontos para calcular a pontuação. */
    private final VerificadorDePontos verificador = new VerificadorDePontos();

    /**
     * Constrói uma nova instância do Jogo de Reflexo.
     *
     * @param fraseCorreta   A frase que deve ser digitada pelo jogador.
     * @param tempoLimiteMs  O tempo limite para o jogo em milissegundos.
     * @param issuerId       O ID do usuário que iniciou o jogo.
     */
    public ReflexoGame(String fraseCorreta, long tempoLimiteMs, String issuerId) {
        super(tempoLimiteMs, issuerId);
        this.fraseCorreta = fraseCorreta;
    }

    /**
     * Retorna a frase correta do jogo.
     *
     * @return A frase correta.
     */
    public String getFraseCorreta() {
        return fraseCorreta;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Processa a primeira e única resposta do jogador. O jogo é imediatamente finalizado
     * e o método calcula o tempo de reação e a pontuação detalhada da digitação,
     * enviando um resumo completo para o canal.
     */
    @Override
    protected void processarRespostaDoJogo(MessageReceivedEvent event, GameManager gameManager, ConfigManager configManager) {
        gameManager.finalizarJogo(event.getChannel().getId());
        String respostaDoUsuario = event.getMessage().getContentRaw();
        long tempoDeReacao = System.currentTimeMillis() - getTempoInicio();

        int pontuacaoMaxima = getFraseCorreta().length() * 2;
        int pontosObtidos = verificador.calcularPontuacao(getFraseCorreta(), respostaDoUsuario);
        double porcentagem = (pontuacaoMaxima > 0) ? ((double) pontosObtidos / pontuacaoMaxima) * 100.0 : 0.0;
        String resultado = String.format(
                "🎉 %s respondeu em %.2f segundos!\n" +
                        "**Frase correta:** `%s`\n" +
                        "**Sua resposta:** `%s`\n" +
                        "**Pontuação:** %d de %d (%.2f%%)",
                event.getAuthor().getAsMention(),
                tempoDeReacao / 1000.0,
                getFraseCorreta(),
                respostaDoUsuario,
                pontosObtidos,
                pontuacaoMaxima,
                porcentagem
        );
        event.getChannel().sendMessage(resultado).queue();
    }
}