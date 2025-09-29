package br.com.bot.shared;

import br.com.bot.core.ConfigManager;
import br.com.bot.core.GameManager;
import br.com.bot.core.ServerConfig;
import br.com.bot.utils.ValidationUtils;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;

/**
 * A classe base para TODOS os comandos de jogo.
 * <p>
 * Atua como um "portão" (Gateway), implementando o padrão Template Method. Ela executa todas as
 * verificações comuns (canal bloqueado, jogo ativo, tempo máximo) antes de delegar o fluxo
 * de início específico do jogo para uma subclasse.
 *
 * @author Lucas
 */
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

    /**
     * O "Portão": Este método final executa uma série de verificações universais para todos os jogos.
     * Ele não pode ser sobrescrito.
     * @param event O evento do comando a ser processado.
     */
    @Override
    public final void execute(final SlashCommandInteractionEvent event) {
        ServerConfig config = configManager.getConfig(event.getGuild().getId());
        if (config.getBlockedChannelIds().contains(event.getChannel().getId())) {
            event.reply("Este canal está bloqueado para jogos.").setEphemeral(true).queue();
            return;
        }
        if (gameManager.isJogoAtivo(event.getChannel().getId())) {
            event.reply("Já existe um jogo ativo neste canal!").setEphemeral(true).queue();
            return;
        }

        Optional<Game> gameOptional = createGame(event);
        if (gameOptional.isEmpty()) return;
        Game novoJogo = gameOptional.get();

        if (!ValidationUtils.checkMaxGameTime(event, configManager, novoJogo.getTempoLimiteMs())) {
            return;
        }

        // Após todas as verificações, delega o fluxo de início para a subclasse.
        startGameFlow(event, novoJogo);
    }

    /**
     * Método de ajuda para converter a opção de tempo.
     * É 'protected', então as classes filhas podem usá-lo.
     */
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

    /**
     * As subclasses devem implementar este método para validar suas opções e criar a instância do jogo.
     * @param event O evento do comando para extrair as opções.
     * @return Um Optional contendo o Jogo, ou vazio se a validação falhar.
     */
    protected abstract Optional<Game> createGame(SlashCommandInteractionEvent event);

    /**
     * As subclasses devem implementar este método para definir o fluxo de início do jogo,
     * que pode ser diferente para cada tipo de jogo.
     * @param event O evento do comando original.
     * @param game O objeto de estado do jogo já criado e validado.
     */
    protected abstract void startGameFlow(SlashCommandInteractionEvent event, Game game);
}