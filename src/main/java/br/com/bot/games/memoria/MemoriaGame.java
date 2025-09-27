package br.com.bot.games.memoria;

import br.com.bot.shared.Game;
import br.com.bot.util.NormalizadorDeTexto;
import br.com.bot.core.GameManager;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class MemoriaGame extends Game {
    private final String stringSecreta;

    public MemoriaGame(long tempoLimiteMs, String stringSecreta) {
        super(tempoLimiteMs);
        this.stringSecreta = stringSecreta;
    }

    public String getStringSecreta() {
        return stringSecreta;
    }

    @Override
    public void processarResposta(MessageReceivedEvent event, GameManager gameManager) {
        String respostaDoUsuario = event.getMessage().getContentRaw();

        // Normaliza a resposta para uma comparação justa (ignora acentos, maiúsculas/minúsculas, etc.)
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
        // Se a resposta estiver errada, o bot ignora, esperando outras tentativas.
    }
}