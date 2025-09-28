package br.com.bot.shared;

import br.com.bot.core.GameManager;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public abstract class AbstractGameCommand implements ICommand {
    protected final GameManager gameManager;
    protected final ScheduledExecutorService scheduler;

    public AbstractGameCommand(GameManager gameManager, ScheduledExecutorService scheduler) {
        this.gameManager = gameManager;
        this.scheduler = scheduler;
    }

    // Este é o "Template Method". Ele define a estrutura e não pode ser sobrescrito.
    @Override
    public final void execute(SlashCommandInteractionEvent event) {
        String channelId = event.getChannel().getId();
        if (gameManager.isJogoAtivo(channelId)) {
            event.reply("Já existe um jogo ativo neste canal!").setEphemeral(true).queue();
            return;
        }

        // Delega a criação e validação para a subclasse
        Optional<Game> gameOptional = createGame(event);
        if (gameOptional.isEmpty()) {
            // Se a validação falhou, a subclasse já enviou a mensagem de erro.
            return;
        }
        Game novoJogo = gameOptional.get();

        event.reply(getPrepareMessage()).queue();

        // Lógica de agendamento centralizada
        scheduler.schedule(() -> {
            event.getChannel().sendMessage(getStartMessage(novoJogo)).queue();
            gameManager.iniciarJogo(channelId, novoJogo);

            scheduler.schedule(() -> {
                Game jogoFinalizado = gameManager.finalizarJogo(channelId);
                if (jogoFinalizado != null) {
                    event.getChannel().sendMessage(getTimeoutMessage(jogoFinalizado)).queue();
                }
            }, novoJogo.getTempoLimiteMs(), TimeUnit.MILLISECONDS);
        }, 3, TimeUnit.SECONDS);
    }

    // Métodos abstratos que as subclasses DEVEM implementar
    protected abstract Optional<Game> createGame(SlashCommandInteractionEvent event);
    protected abstract String getPrepareMessage();
    protected abstract String getStartMessage(Game game);
    protected abstract String getTimeoutMessage(Game game);
}