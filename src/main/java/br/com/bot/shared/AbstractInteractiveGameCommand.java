package br.com.bot.shared;

import br.com.bot.core.ConfigManager;
import br.com.bot.core.GameManager;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Um "molde" especializado para jogos interativos que precisam editar sua mensagem de estado.
 * Herda de {@link AbstractGameCommand} e implementa o método {@code startGameFlow}
 * de uma forma que captura o ID da mensagem inicial.
 *
 * @author Lucas
 */
public abstract class AbstractInteractiveGameCommand extends AbstractGameCommand {
    public AbstractInteractiveGameCommand(GameManager gameManager, ConfigManager configManager, ScheduledExecutorService scheduler) {
        super(gameManager, configManager, scheduler);
    }

    /**
     * {@inheritDoc}
     * Implementa o fluxo de início interativo: envia uma mensagem de preparo, espera,
     * envia a mensagem inicial do jogo, captura seu ID, e então agenda o timeout.
     */
    @Override
    protected void startGameFlow(SlashCommandInteractionEvent event, Game game) {
        event.reply(String.format("O jogo vai começar em %d segundos...", PREPARE_DELAY_SECONDS)).queue();
        scheduler.schedule(() -> {
            MessageCreateData initialMessage = getInitialMessage(game);
            event.getChannel().sendMessage(initialMessage).queue(message -> {
                setGameMessageId(game, message.getId()); // Ponto crucial!
                gameManager.iniciarJogo(event.getChannel().getId(), game);
                scheduler.schedule(() -> {
                    Game jogoFinalizado = gameManager.finalizarJogo(event.getChannel().getId());
                    if (jogoFinalizado != null) {
                        event.getChannel().sendMessage(getTimeoutMessage(jogoFinalizado)).queue();
                    }
                }, game.getTempoLimiteMs(), TimeUnit.MILLISECONDS);
            });
        }, PREPARE_DELAY_SECONDS, TimeUnit.SECONDS);
    }

    // Novos métodos abstratos para as subclasses (Forca, etc.) preencherem
    protected abstract MessageCreateData getInitialMessage(Game game);
    protected abstract void setGameMessageId(Game game, String messageId);
    protected abstract String getTimeoutMessage(Game game);
}