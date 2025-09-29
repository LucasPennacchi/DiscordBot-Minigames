package br.com.bot.games.memoria;

import br.com.bot.core.ConfigManager;
import br.com.bot.core.GameManager;
import br.com.bot.shared.AbstractInteractiveGameCommand;
import br.com.bot.shared.Game;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Comando que implementa o Jogo da Memória.
 * <p>
 * Esta classe herda do molde de jogos interativos, {@link AbstractInteractiveGameCommand},
 * mas com uma lógica customizada em seu fluxo para acomodar os dois timers distintos
 * (tempo para memorizar e tempo para responder).
 *
 * @author Lucas
 */
public class MemoriaCommand extends AbstractInteractiveGameCommand {

    /**
     * Constrói o comando da Memória com suas dependências necessárias,
     * passando-as para a classe-mãe abstrata.
     *
     * @param gameManager O gerenciador de jogos ativos.
     * @param configManager O gerenciador de configurações de servidor.
     * @param scheduler O agendador de tarefas para os timers.
     */
    public MemoriaCommand(GameManager gameManager, ConfigManager configManager, ScheduledExecutorService scheduler) {
        super(gameManager, configManager, scheduler);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Para o Jogo da Memória, o fluxo de início é customizado para lidar com o
     * tempo de memorização antes de iniciar o timer do jogo.
     */
    @Override
    protected void startGameFlow(SlashCommandInteractionEvent event, Game game) {
        long tempoOcultarMs = parseTimeOption(event, "tempo_ocultar").orElse(5000L); // Padrão de 5s

        event.reply(String.format("O jogo da memória vai começar em %d segundos...", PREPARE_DELAY_SECONDS)).queue();

        scheduler.schedule(() -> {
            MessageCreateData initialMessage = getInitialMessage(game);
            event.getChannel().sendMessage(initialMessage).queue(message -> {
                // Agenda a edição da mensagem (para ocultar a string)
                scheduler.schedule(() -> {
                    // Guarda o ID da mensagem no objeto do jogo
                    setGameMessageId(game, message.getId());

                    double tempoLimiteSeg = game.getTempoLimiteMs() / 1000.0;
                    message.editMessage(String.format("**Qual era a string?** Você tem %.1f segundos!", tempoLimiteSeg)).queue();

                    // AGORA o jogo efetivamente começa
                    gameManager.iniciarJogo(event.getChannel().getId(), game);

                    // Agenda o timeout final do jogo
                    scheduler.schedule(() -> {
                        Game jogoFinalizado = gameManager.finalizarJogo(event.getChannel().getId());
                        if (jogoFinalizado != null) {
                            event.getChannel().sendMessage(getTimeoutMessage(jogoFinalizado)).queue();
                        }
                    }, game.getTempoLimiteMs(), TimeUnit.MILLISECONDS);
                }, tempoOcultarMs, TimeUnit.MILLISECONDS);
            });
        }, PREPARE_DELAY_SECONDS, TimeUnit.SECONDS);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Valida as opções e cria uma instância de {@link MemoriaGame}.
     */
    @Override
    protected Optional<Game> createGame(SlashCommandInteractionEvent event) {
        Optional<Long> tempoLimiteOpt = parseTimeOption(event, "tempo_limite");
        if (tempoLimiteOpt.isEmpty()) {
            return Optional.empty();
        }

        // Validação extra para o tempo_ocultar
        Optional<Long> tempoOcultarOpt = parseTimeOption(event, "tempo_ocultar");
        if(tempoOcultarOpt.isEmpty()) {
            return Optional.empty();
        }

        String stringSecreta = event.getOption("string").getAsString();
        String issuerId = event.getUser().getId();

        return Optional.of(new MemoriaGame(tempoLimiteOpt.get(), stringSecreta, issuerId));
    }

    /**
     * {@inheritDoc}
     * <p>
     * Cria a mensagem inicial que o jogador deve memorizar.
     */
    @Override
    protected MessageCreateData getInitialMessage(Game game) {
        MemoriaGame memoriaGame = (MemoriaGame) game;
        return MessageCreateData.fromContent("Memorize a seguinte string:\n`" + memoriaGame.getStringSecreta() + "`");
    }

    /**
     * {@inheritDoc}
     * <p>
     * Define o ID da mensagem no objeto do jogo para referência futura.
     */
    @Override
    protected void setGameMessageId(Game game, String messageId) {
        ((MemoriaGame) game).setMessageId(messageId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getTimeoutMessage(Game game) {
        return "O tempo esgotou! A string correta era: `" + ((MemoriaGame) game).getStringSecreta() + "`.";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash("memoria", "Inicia um jogo de memória.")
                .addOption(OptionType.STRING, "tempo_ocultar", "Tempo para memorizar a string (ex: 5).", true)
                .addOption(OptionType.STRING, "tempo_limite", "Tempo para responder após a string sumir (ex: 10).", true)
                .addOption(OptionType.STRING, "string", "A string a ser memorizada.", true);
    }
}