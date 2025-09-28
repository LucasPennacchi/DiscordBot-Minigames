package br.com.bot.shared;

import br.com.bot.core.GameManager;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public abstract class AbstractGameCommand implements ICommand {
    protected final GameManager gameManager;
    protected final ScheduledExecutorService scheduler;
    protected static final int PREPARE_DELAY_SECONDS = 3;

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
        }, PREPARE_DELAY_SECONDS, TimeUnit.SECONDS);
    }

    /**
     * NOVO METODO PROTEGIDO:
     * Tenta converter uma opção de comando em um valor de tempo em milissegundos.
     * Envia uma mensagem de erro e retorna Optional.empty() se a conversão falhar.
     * @param event O evento do comando.
     * @param optionName O nome da opção a ser lida (ex: "tempo").
     * @return Um Optional contendo o tempo em milissegundos, ou vazio se houver erro.
     */
    protected Optional<Long> parseTimeOption(SlashCommandInteractionEvent event, String optionName) {
        OptionMapping option = event.getOption(optionName);
        if (option == null) {
            // Este caso não deve acontecer se a opção for obrigatória, mas é uma boa prática.
            event.reply("A opção '" + optionName + "' é obrigatória.").setEphemeral(true).queue();
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
    // Métodos abstratos que as subclasses DEVEM implementar
    protected abstract Optional<Game> createGame(SlashCommandInteractionEvent event);
    protected abstract String getPrepareMessage();
    protected abstract String getStartMessage(Game game);
    protected abstract String getTimeoutMessage(Game game);
}