package br.com.bot.shared;

import br.com.bot.core.ConfigManager;
import br.com.bot.core.GameManager;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Uma classe abstrata que implementa o padrão "Template Method" para comandos de JOGOS INTERATIVOS.
 * <p>
 * Diferente de {@link AbstractSimpleGameCommand}, este template é projetado para jogos que
 * precisam enviar uma mensagem inicial (como um tabuleiro) e depois editá-la durante o jogo.
 * Ele gerencia o fluxo de capturar o ID da mensagem inicial antes de criar o estado do jogo.
 *
 * @author Lucas
 */
public abstract class AbstractInteractiveGameCommand implements ICommand {

    /** O tempo de espera em segundos antes de cada jogo começar. */
    public static final int PREPARE_DELAY_SECONDS = 3;

    protected final GameManager gameManager;
    protected final ConfigManager configManager;
    protected final ScheduledExecutorService scheduler;

    /**
     * Construtor para comandos de jogos interativos.
     * @param gameManager O gerenciador de jogos ativos.
     * @param configManager O gerenciador de configurações de servidor.
     * @param scheduler O agendador de tarefas para os timers.
     */
    public AbstractInteractiveGameCommand(GameManager gameManager, ConfigManager configManager, ScheduledExecutorService scheduler) {
        this.gameManager = gameManager;
        this.configManager = configManager;
        this.scheduler = scheduler;
    }

    /**
     * {@inheritDoc}
     * Este é o "Template Method" que define a estrutura de execução de um jogo interativo.
     * O fluxo é: preparar, enviar uma mensagem inicial, capturar o ID da mensagem,
     * criar o jogo com o ID e agendar o timeout.
     */
    @Override
    public final void execute(SlashCommandInteractionEvent event) {
        // ... (Aqui entrariam as verificações de canal bloqueado e jogo ativo)

        event.reply(String.format("O jogo vai começar em %d segundos...", PREPARE_DELAY_SECONDS)).queue();

        scheduler.schedule(() -> {
            // A subclasse define qual é a mensagem inicial do jogo.
            MessageCreateData initialMessage = getInitialMessage(event);

            // Envia a mensagem e espera o callback do Discord para obter o objeto Message.
            event.getChannel().sendMessage(initialMessage).queue(message -> {
                // Com a mensagem enviada, o Discord nos dá o ID dela.
                // AGORA podemos criar o jogo, passando o ID da mensagem.
                Optional<Game> gameOptional = createGame(event, message.getId());

                if (gameOptional.isEmpty()) return;
                Game novoJogo = gameOptional.get();

                gameManager.iniciarJogo(event.getChannel().getId(), novoJogo);

                // A lógica de timeout também é compartilhada.
                scheduler.schedule(() -> {
                    Game jogoFinalizado = gameManager.finalizarJogo(event.getChannel().getId());
                    if (jogoFinalizado != null) {
                        event.getChannel().sendMessage(getTimeoutMessage(jogoFinalizado)).queue();
                    }
                }, novoJogo.getTempoLimiteMs(), TimeUnit.MILLISECONDS);
            });
        }, PREPARE_DELAY_SECONDS, TimeUnit.SECONDS);
    }

    // --- MÉTODOS ABSTRATOS (Hooks para as subclasses preencherem) ---

    /**
     * As subclasses devem implementar este método para fornecer a mensagem inicial do jogo.
     * Ex: O tabuleiro da forca, a string a ser memorizada, etc.
     * @param event O evento do comando original, caso precise de alguma opção.
     * @return Um {@link MessageCreateData} para ser enviado ao canal.
     */
    protected abstract MessageCreateData getInitialMessage(SlashCommandInteractionEvent event);

    /**
     * As subclasses devem implementar este método para validar as opções do comando e criar a instância do jogo.
     * Este método é chamado DEPOIS que a mensagem inicial é enviada.
     * @param event O evento do comando original para extrair as opções.
     * @param messageId O ID da mensagem inicial do jogo, para ser guardado no estado do jogo.
     * @return Um Optional contendo o Jogo, ou vazio se a validação falhar.
     */
    protected abstract Optional<Game> createGame(SlashCommandInteractionEvent event, String messageId);

    /**
     * As subclasses devem implementar este método para fornecer a mensagem de timeout.
     * @param game A instância do jogo que foi finalizado por timeout.
     * @return A mensagem a ser exibida quando o tempo esgota.
     */
    protected abstract String getTimeoutMessage(Game game);
}