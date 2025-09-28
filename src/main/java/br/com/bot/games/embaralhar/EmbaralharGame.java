package br.com.bot.games.embaralhar;

import br.com.bot.core.GameManager;
import br.com.bot.shared.Game;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class EmbaralharGame extends Game {
    private final String palavraOriginal;

    public EmbaralharGame(long tempoLimiteMs, String palavraOriginal, String issuerId) {
        super(tempoLimiteMs, issuerId);
        this.palavraOriginal = palavraOriginal;
    }

    public String getPalavraOriginal() {
        return palavraOriginal;
    }

    @Override
    protected void processarRespostaDoJogo(MessageReceivedEvent event, GameManager gameManager) {
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