package br.com.bot.games.reflexo;

import br.com.bot.core.ConfigManager;
import br.com.bot.core.GameManager;
import br.com.bot.shared.AbstractGameCommand;
import br.com.bot.shared.Game;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;

public class ReflexosCommand extends AbstractGameCommand {

    public ReflexosCommand(GameManager gameManager, ConfigManager configManager, ScheduledExecutorService scheduler) {
        super(gameManager, configManager, scheduler);
    }

    @Override
    protected Optional<Game> createGame(SlashCommandInteractionEvent event) {
        // Usa o metodo auxiliar herdado para validar o tempo
        Optional<Long> tempoOpt = parseTimeOption(event, "tempo");
        if (tempoOpt.isEmpty()) {
            return Optional.empty(); // O erro já foi reportado
        }
        long tempoLimiteMs = tempoOpt.get();

        String frase = event.getOption("frase").getAsString();
        String issuerId = event.getUser().getId();

        // Cria e retorna a instância do jogo
        return Optional.of(new ReflexoGame(frase, tempoLimiteMs, issuerId));
    }

    @Override
    protected String getPrepareMessage() {
        // Usa a constante herdada para garantir consistência
        return String.format("O teste de reflexo vai começar em %d segundos... Prepare-se!", PREPARE_DELAY_SECONDS);
    }

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

    @Override
    protected String getTimeoutMessage(Game game) {
        return "O tempo esgotou e ninguém respondeu!";
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash("reflexos", "Inicia um teste de reflexo no canal.")
                .addOption(OptionType.STRING, "tempo", "O tempo limite em segundos (ex: 2.5 ou 2,5).", true)
                .addOption(OptionType.STRING, "frase", "A frase a ser digitada.", true);
    }
}