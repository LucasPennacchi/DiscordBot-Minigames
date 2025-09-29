package br.com.bot.shared;

import br.com.bot.core.ConfigManager;
import br.com.bot.core.GameManager;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Um "molde" especializado para jogos com um fluxo de início simples ("dispare e esqueça").
 * Herda de {@link AbstractGameCommand} e implementa o método {@code startGameFlow} de forma
 * genérica para jogos como Reflexos e Resposta.
 *
 * @author Lucas
 */
public abstract class AbstractSimpleGameCommand extends AbstractGameCommand {
    public AbstractSimpleGameCommand(GameManager gameManager, ConfigManager configManager, ScheduledExecutorService scheduler) {
        super(gameManager, configManager, scheduler);
    }

    /**
     * {@inheritDoc}
     * Implementa o fluxo de início padrão: envia uma mensagem de preparo, espera,
     * envia a mensagem de início e agenda o timeout.
     */
    @Override
    protected void startGameFlow(SlashCommandInteractionEvent event, Game game) {
        event.reply(getPrepareMessage()).queue();
        scheduler.schedule(() -> {
            event.getChannel().sendMessage(getStartMessage(game)).queue();
            gameManager.iniciarJogo(event.getChannel().getId(), game);
            scheduler.schedule(() -> {
                Game jogoFinalizado = gameManager.finalizarJogo(event.getChannel().getId());
                if (jogoFinalizado != null) {
                    event.getChannel().sendMessage(getTimeoutMessage(jogoFinalizado)).queue();
                }
            }, game.getTempoLimiteMs(), TimeUnit.MILLISECONDS);
        }, PREPARE_DELAY_SECONDS, TimeUnit.SECONDS);
    }

    // Novos métodos abstratos para as subclasses (Reflexos, etc.) preencherem
    protected abstract String getPrepareMessage();
    protected abstract String getStartMessage(Game game);
    protected abstract String getTimeoutMessage(Game game);
}