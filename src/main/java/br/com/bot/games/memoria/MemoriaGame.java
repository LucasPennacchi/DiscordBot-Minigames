package br.com.bot.games.memoria;

import br.com.bot.core.ConfigManager;
import br.com.bot.core.GameManager;
import br.com.bot.shared.Game;
import br.com.bot.utils.NormalizadorDeTexto;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class MemoriaGame extends Game {
    private final String stringSecreta;

    public MemoriaGame(long tempoLimiteMs, String stringSecreta, String issuerId) {
        super(tempoLimiteMs, issuerId);
        this.stringSecreta = stringSecreta;
    }

    public String getStringSecreta() {
        return stringSecreta;
    }

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
        // Se a resposta estiver errada, o metodo termina, permitindo que outros jogadores tentem.
    }
}