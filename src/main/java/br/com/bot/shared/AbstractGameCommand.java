package br.com.bot.shared;

import br.com.bot.core.ConfigManager;
import br.com.bot.core.GameManager;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public abstract class AbstractGameCommand implements ICommand {

    public static final int PREPARE_DELAY_SECONDS = 3;

    protected final GameManager gameManager;
    protected final ConfigManager configManager;
    protected final ScheduledExecutorService scheduler;

    public AbstractGameCommand(GameManager gameManager, ConfigManager configManager, ScheduledExecutorService scheduler) {
        this.gameManager = gameManager;
        this.configManager = configManager;
        this.scheduler = scheduler;
    }

    @Override
    public final void execute(final SlashCommandInteractionEvent event) {
        String channelId = event.getChannel().getId();

        // A verificação de canal bloqueado foi REMOVIDA daqui, pois agora está em GameCommands.

        if (gameManager.isJogoAtivo(channelId)) {
            event.reply("Já existe um jogo ativo neste canal!").setEphemeral(true).queue();
            return;
        }

        Optional<Game> gameOptional = createGame(event);
        if (gameOptional.isEmpty()) {
            return;
        }
        Game novoJogo = gameOptional.get();

        event.reply(getPrepareMessage()).queue();

        scheduler.schedule(() -> {
            event.getChannel().sendMessage(getStartMessage(novoJogo)).queue();
            gameManager.iniciarJogo(channelId, novoJogo);

            scheduler.schedule(() -> {
                Game jogoFinalizado = gameManager.finalizarJogo(channelId);
                if (jogoFinalizado != null) {
                    event.getChannel().sendMessage(getTimeoutMessage(jogoFinalizado)).queue();
                }
            }, novoJogo.getTempoLimiteMs(), TimeUnit.MILLISECONDS);

        }, PREPARE_DELAY_SECONDS, TimeUnit.SECONDS);
    }

    protected Optional<Long> parseTimeOption(SlashCommandInteractionEvent event, String optionName) {
        OptionMapping option = event.getOption(optionName);
        if (option == null) {
            event.reply("A opção obrigatória '" + optionName + "' não foi encontrada.").setEphemeral(true).queue();
            return Optional.empty();
        }

        String tempoInput = option.getAsString();
        String tempoNormalizado = tempoInput.replace(',', '.');
        try {
            double tempoDouble = Double.parseDouble(tempoNormalizado);
            return Optional.of((long) (tempoDouble * 1000));
        } catch (NumberFormatException e) {
            event.reply("O tempo fornecido ('" + tempoInput + "') para '" + optionName + "' não é um número válido.").setEphemeral(true).queue();
            return Optional.empty();
        }
    }

    protected abstract Optional<Game> createGame(SlashCommandInteractionEvent event);
    protected abstract String getPrepareMessage();
    protected abstract String getStartMessage(Game game);
    protected abstract String getTimeoutMessage(Game game);

    @Override
    public abstract SlashCommandData getCommandData();
}