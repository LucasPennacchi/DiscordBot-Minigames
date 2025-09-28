package br.com.bot.games.embaralhar;

import br.com.bot.core.GameManager;
import br.com.bot.shared.AbstractGameCommand;
import br.com.bot.shared.Game;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;

public class EmbaralharCommand extends AbstractGameCommand {
    public EmbaralharCommand(GameManager gameManager, ScheduledExecutorService scheduler) {
        super(gameManager, scheduler);
    }

    // Função auxiliar para embaralhar a palavra
    private String embaralharPalavra(String palavra) {
        List<Character> chars = new ArrayList<>();
        for (char c : palavra.toCharArray()) {
            chars.add(c);
        }
        // Garante que a palavra embaralhada não seja igual à original
        String embaralhada;
        do {
            Collections.shuffle(chars);
            StringBuilder sb = new StringBuilder();
            for (char c : chars) {
                sb.append(c);
            }
            embaralhada = sb.toString();
        } while (embaralhada.equals(palavra) && palavra.length() > 1);

        return embaralhada;
    }

    @Override
    protected Optional<Game> createGame(SlashCommandInteractionEvent event) {
        Optional<Long> tempoOpt = parseTimeOption(event, "tempo");
        if (tempoOpt.isEmpty()) {
            return Optional.empty();
        }
        long tempoLimiteMs = tempoOpt.get();

        String palavra = event.getOption("palavra").getAsString();
        String issuerId = event.getUser().getId();

        return Optional.of(new EmbaralharGame(tempoLimiteMs, palavra, issuerId));
    }

    @Override
    protected String getPrepareMessage() {
        return String.format("O jogo de embaralhar vai começar em %d segundos...", PREPARE_DELAY_SECONDS);
    }

    @Override
    protected String getStartMessage(Game game) {
        EmbaralharGame embaralharGame = (EmbaralharGame) game;
        String palavraOriginal = embaralharGame.getPalavraOriginal();
        String palavraEmbaralhada = embaralharPalavra(palavraOriginal);
        double tempoEmSegundos = embaralharGame.getTempoLimiteMs() / 1000.0;

        return String.format(
                "Tempo limite: **%.1f segundos**\n\nDesembaralhe a seguinte palavra: `%s`",
                tempoEmSegundos,
                palavraEmbaralhada
        );
    }

    @Override
    protected String getTimeoutMessage(Game game) {
        EmbaralharGame embaralharGame = (EmbaralharGame) game;
        return "O tempo esgotou! A palavra correta era: `" + embaralharGame.getPalavraOriginal() + "`";
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash("embaralhar", "Inicia um jogo de adivinhar a palavra embaralhada.")
                .addOption(OptionType.STRING, "tempo", "O tempo limite em segundos.", true)
                .addOption(OptionType.STRING, "palavra", "A palavra a ser embaralhada.", true);
    }
}