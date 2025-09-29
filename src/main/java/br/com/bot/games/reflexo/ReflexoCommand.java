package br.com.bot.games.reflexo;

import br.com.bot.core.ConfigManager;
import br.com.bot.core.GameManager;
import br.com.bot.shared.AbstractSimpleGameCommand;
import br.com.bot.shared.Game;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Comando que implementa o Jogo de Reflexo, onde os jogadores devem digitar uma frase rapidamente.
 * <p>
 * Esta classe herda do molde de jogos simples, {@link AbstractSimpleGameCommand},
 * e preenche os detalhes específicos para o fluxo do jogo de reflexo.
 *
 * @author Lucas
 */
public class ReflexoCommand extends AbstractSimpleGameCommand {

    /**
     * Constrói o comando de reflexo com suas dependências necessárias,
     * passando-as para a classe-mãe abstrata.
     *
     * @param gameManager O gerenciador de jogos ativos.
     * @param configManager O gerenciador de configurações de servidor.
     * @param scheduler O agendador de tarefas para os timers.
     */
    public ReflexoCommand(GameManager gameManager, ConfigManager configManager, ScheduledExecutorService scheduler) {
        super(gameManager, configManager, scheduler);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Valida as opções 'tempo' e 'frase' fornecidas pelo usuário
     * e cria uma instância de {@link ReflexoGame} com esses dados.
     */
    @Override
    protected Optional<Game> createGame(SlashCommandInteractionEvent event) {
        // Usa o método auxiliar herdado de AbstractGameCommand para validar o tempo.
        Optional<Long> tempoOpt = parseTimeOption(event, "tempo");
        if (tempoOpt.isEmpty()) {
            return Optional.empty(); // O erro já foi reportado ao usuário pelo método auxiliar.
        }

        String frase = event.getOption("frase").getAsString();
        String issuerId = event.getUser().getId();

        return Optional.of(new ReflexoGame(frase, tempoOpt.get(), issuerId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getPrepareMessage() {
        // Usa a constante herdada para garantir consistência.
        return String.format("O teste de reflexo vai começar em %d segundos... Prepare-se!", PREPARE_DELAY_SECONDS);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getStartMessage(Game game) {
        ReflexoGame reflexoGame = (ReflexoGame) game;
        double tempoEmSegundos = reflexoGame.getTempoLimiteMs() / 1000.0;

        return String.format(
                "Tempo limite: **%.1f segundos**\n\nVAI! Digite a frase: `%s`",
                tempoEmSegundos,
                reflexoGame.getFraseCorreta()
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getTimeoutMessage(Game game) {
        return "O tempo esgotou e ninguém respondeu!";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash("reflexo", "Inicia um teste de reflexo no canal.")
                .addOption(OptionType.STRING, "tempo", "O tempo limite em segundos (ex: 2.5 ou 2,5).", true)
                .addOption(OptionType.STRING, "frase", "A frase a ser digitada.", true);
    }
}