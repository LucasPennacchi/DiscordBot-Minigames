package br.com.bot.games.reflexo;

import br.com.bot.core.GameManager;
import br.com.bot.shared.Game;
import br.com.bot.utils.VerificadorDePontos;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class ReflexoGame extends Game {
    private final String fraseCorreta;
    private final VerificadorDePontos verificador = new VerificadorDePontos();

    public ReflexoGame(String fraseCorreta, long tempoLimiteMs, String issuerId) {
        super(tempoLimiteMs, issuerId);
        this.fraseCorreta = fraseCorreta;
    }

    public String getFraseCorreta() {
        return fraseCorreta;
    }

    @Override
    protected void processarRespostaDoJogo(MessageReceivedEvent event, GameManager gameManager) {
        // Finaliza o jogo na primeira resposta válida
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