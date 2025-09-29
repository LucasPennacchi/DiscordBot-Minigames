package br.com.bot.games.resposta;

import br.com.bot.core.ConfigManager;
import br.com.bot.core.GameManager;
import br.com.bot.shared.Game;
import br.com.bot.utils.NormalizadorDeTexto;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

/**
 * Representa o estado e a lógica de um Jogo de Pergunta e Resposta.
 * <p>
 * Esta classe guarda a pergunta a ser feita e a resposta correta esperada.
 * Implementa a lógica para verificar se a tentativa de um jogador corresponde à resposta.
 *
 * @author Lucas
 */
public class RespostaGame extends Game {

    /** A pergunta que será exibida para os jogadores. */
    private final String pergunta;

    /** A resposta correta para a pergunta. */
    private final String respostaCorreta;

    /**
     * Constrói uma nova instância do Jogo de Pergunta e Resposta.
     *
     * @param tempoLimiteMs   O tempo limite para o jogo em milissegundos.
     * @param pergunta        A pergunta a ser exibida no canal.
     * @param respostaCorreta A resposta exata que os jogadores devem digitar.
     * @param issuerId        O ID do usuário que iniciou o jogo.
     */
    public RespostaGame(long tempoLimiteMs, String pergunta, String respostaCorreta, String issuerId) {
        super(tempoLimiteMs, issuerId);
        this.pergunta = pergunta;
        this.respostaCorreta = respostaCorreta;
    }

    /**
     * Retorna a pergunta do jogo.
     *
     * @return A pergunta.
     */
    public String getPergunta() {
        return pergunta;
    }

    /**
     * Retorna a resposta correta do jogo.
     *
     * @return A resposta correta.
     */
    public String getRespostaCorreta() {
        return respostaCorreta;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Processa a mensagem de um jogador. A resposta é normalizada (ignorando acentos,
     * espaços e maiúsculas/minúsculas) e comparada com a resposta correta. Se corresponder,
     * o jogo é finalizado e o vencedor é anunciado. Respostas incorretas são ignoradas.
     */
    @Override
    protected void processarRespostaDoJogo(MessageReceivedEvent event, GameManager gameManager, ConfigManager configManager) {
        String respostaDoUsuario = event.getMessage().getContentRaw();

        // Normaliza as respostas para uma comparação justa (ignora maiúsculas/minúsculas, espaços e acentos)
        String respostaNormalizadaUsuario = NormalizadorDeTexto.removerAcentos(respostaDoUsuario.trim().toLowerCase());
        String respostaNormalizadaCorreta = NormalizadorDeTexto.removerAcentos(getRespostaCorreta().trim().toLowerCase());

        if (respostaNormalizadaUsuario.equals(respostaNormalizadaCorreta)) {
            // Se a resposta estiver correta, finaliza o jogo e anuncia o vencedor
            gameManager.finalizarJogo(event.getChannel().getId());
            long tempoDeReacao = System.currentTimeMillis() - getTempoInicio();

            String resultado = String.format(
                    "🏆 **Correto!** %s acertou a resposta em %.2f segundos!",
                    event.getAuthor().getAsMention(),
                    tempoDeReacao / 1000.0
            );
            event.getChannel().sendMessage(resultado).queue();
        }
        // Se a resposta estiver errada, o metodo simplesmente termina, permitindo outras tentativas.
    }
}