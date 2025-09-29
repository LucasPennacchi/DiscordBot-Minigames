package br.com.bot.shared;

import br.com.bot.core.ConfigManager;
import br.com.bot.core.GameManager;
import br.com.bot.utils.ValidationUtils;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Uma classe abstrata que implementa o padrão "Template Method" para comandos de jogo com fluxo padrão.
 * Ela define a estrutura principal de execução de um jogo (validar, preparar, iniciar, timeout),
 * centralizando a lógica comum e delegando as partes específicas para as subclasses.
 *
 * @author Lucas
 */
public abstract class AbstractGameCommand implements ICommand {

    /** O tempo de espera em segundos antes de cada jogo começar. */
    public static final int PREPARE_DELAY_SECONDS = 3;

    protected final GameManager gameManager;
    protected final ConfigManager configManager;
    protected final ScheduledExecutorService scheduler;

    /**
     * Construtor para comandos de jogo padrão.
     * @param gameManager O gerenciador de jogos ativos.
     * @param configManager O gerenciador de configurações de servidor.
     * @param scheduler O agendador de tarefas para os timers.
     */
    public AbstractGameCommand(GameManager gameManager, ConfigManager configManager, ScheduledExecutorService scheduler) {
        this.gameManager = gameManager;
        this.configManager = configManager;
        this.scheduler = scheduler;
    }

    /**
     * Este é o "Template Method". Ele define o esqueleto da execução de um comando
     * e não pode ser sobrescrito pelas subclasses.
     */
    @Override
    public final void execute(final SlashCommandInteractionEvent event) {
        String channelId = event.getChannel().getId();

        // A verificação de canal bloqueado é feita no dispatcher (GameCommands).

        if (gameManager.isJogoAtivo(channelId)) {
            event.reply("Já existe um jogo ativo neste canal!").setEphemeral(true).queue();
            return;
        }

        // Delega a validação de opções e criação do jogo para a subclasse.
        Optional<Game> gameOptional = createGame(event);
        if (gameOptional.isEmpty()) {
            return; // A subclasse já enviou a mensagem de erro.
        }
        Game novoJogo = gameOptional.get();

        // Validação centralizada de tempo máximo.
        if (!ValidationUtils.checkMaxGameTime(event, configManager, novoJogo.getTempoLimiteMs())) {
            return;
        }

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

    /**
     * Método auxiliar centralizado para validar e converter uma opção de tempo de um comando.
     * @param event O evento do comando.
     * @param optionName O nome da opção a ser lida (ex: "tempo").
     * @return Um Optional contendo o tempo em milissegundos, ou vazio se houver erro.
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

    // --- MÉTODOS ABSTRATOS (Hooks para as subclasses preencherem) ---

    protected abstract Optional<Game> createGame(SlashCommandInteractionEvent event);
    protected abstract String getPrepareMessage();
    protected abstract String getStartMessage(Game game);
    protected abstract String getTimeoutMessage(Game game);
}