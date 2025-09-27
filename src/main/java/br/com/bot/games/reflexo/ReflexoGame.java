package br.com.bot.games.reflexo;

import br.com.bot.shared.Game;
import br.com.bot.util.VerificadorDePontos;
import br.com.bot.core.GameManager;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class ReflexoGame extends Game {
    private final String fraseCorreta;
    private final VerificadorDePontos verificador = new VerificadorDePontos(); // Pode ser injetado

    public ReflexoGame(String fraseCorreta, long tempoLimiteMs) {
        super(tempoLimiteMs);
        this.fraseCorreta = fraseCorreta;
    }

    public String getFraseCorreta() { return fraseCorreta; }

    @Override
    public void processarResposta(MessageReceivedEvent event, GameManager gameManager) {
        // A lógica de verificação da resposta do jogo de reflexos agora vive aqui!
        gameManager.finalizarJogo(event.getChannel().getId());
        String respostaDoUsuario = event.getMessage().getContentRaw();
        long tempoDeReacao = System.currentTimeMillis() - getTempoInicio();

        // ... Lógica de cálculo de pontos e formatação da mensagem ...
        int pontuacaoMaxima = getFraseCorreta().length() * 2;
        int pontosObtidos = verificador.calcularPontuacao(getFraseCorreta(), respostaDoUsuario);
        double porcentagem = (pontuacaoMaxima > 0) ? ((double) pontosObtidos / pontuacaoMaxima) * 100.0 : 0.0;
        String resultado = String.format(
                "🎉 %s respondeu em %.2f segundos!\n" +
                        "**Frase correta:** `%s`\n" +
                        "**Sua resposta:** `%s`\n" +
                        "**Pontuação:** %d de %d (%.2f%%)",
                event.getAuthor().getAsMention(), tempoDeReacao / 1000.0, getFraseCorreta(),
                respostaDoUsuario, pontosObtidos, pontuacaoMaxima, porcentagem
        );
        event.getChannel().sendMessage(resultado).queue();
    }
}