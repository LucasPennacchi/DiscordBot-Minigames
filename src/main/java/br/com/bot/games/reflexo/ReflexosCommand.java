package br.com.bot.games.reflexo;

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
    public ReflexosCommand(GameManager gameManager, ScheduledExecutorService scheduler) {
        super(gameManager, scheduler);
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash("reflexos", "Inicia um teste de reflexo no canal.")
                .addOption(OptionType.STRING, "tempo", "O tempo limite em segundos (ex: 2.5 ou 2,5).", true)
                .addOption(OptionType.STRING, "frase", "A frase a ser digitada.", true);
    }

    @Override
    protected Optional<Game> createGame(SlashCommandInteractionEvent event) {
        // A validação específica fica aqui
        String tempoInput = event.getOption("tempo").getAsString();
        double tempoLimiteDouble;
        try {
            tempoLimiteDouble = Double.parseDouble(tempoInput.replace(',', '.'));
        } catch (NumberFormatException e) {
            event.reply("O tempo fornecido ('" + tempoInput + "') não é um número válido.").setEphemeral(true).queue();
            return Optional.empty();
        }

        String frase = event.getOption("frase").getAsString();
        long tempoLimiteMs = (long) (tempoLimiteDouble * 1000);
        String issuerId = event.getUser().getId();

        // A criação do objeto específico fica aqui
        return Optional.of(new ReflexoGame(frase, tempoLimiteMs, issuerId));
    }

    @Override
    protected String getPrepareMessage() {
        return "O teste de reflexo vai começar em 3 segundos... Prepare-se!";
    }

    @Override
    protected String getStartMessage(Game game) {
        ReflexoGame reflexoGame = (ReflexoGame) game;

        double tempoEmSegundos = reflexoGame.getTempoLimiteMs() / 1000.0;

        // Formata toda a mensagem de uma vez e usa Markdown do Discord (**...**) para destacar
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


}