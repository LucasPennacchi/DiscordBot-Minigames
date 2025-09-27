package br.com.bot.games.resposta;

import br.com.bot.shared.Game;
import br.com.bot.core.GameManager;
import br.com.bot.util.NormalizadorDeTexto;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class RespostaGame extends Game {
    private final String pergunta;
    private final String respostaCorreta;

    public RespostaGame(long tempoLimiteMs, String pergunta, String respostaCorreta) {
        super(tempoLimiteMs);
        this.pergunta = pergunta;
        this.respostaCorreta = respostaCorreta;
    }

    public String getPergunta() { return pergunta; }
    public String getRespostaCorreta() { return respostaCorreta; }

    @Override
    public void processarResposta(MessageReceivedEvent event, GameManager gameManager) {
        String respostaDoUsuario = event.getMessage().getContentRaw();

        // Normaliza ambas as respostas para uma comparação justa
        // Ignora maiúsculas/minúsculas, espaços extras e acentos.
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
        // Se a resposta estiver errada, o método simplesmente termina. O bot não faz nada,
        // permitindo que outros jogadores tentem responder.
    }
}