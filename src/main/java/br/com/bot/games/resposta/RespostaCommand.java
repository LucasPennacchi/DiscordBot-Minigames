package br.com.bot.games.resposta;

import br.com.bot.core.GameManager;
import br.com.bot.shared.AbstractGameCommand;
import br.com.bot.shared.Game;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;

public class RespostaCommand extends AbstractGameCommand {
    public RespostaCommand(GameManager gameManager, ScheduledExecutorService scheduler) {
        super(gameManager, scheduler);
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash("resposta", "Inicia um jogo de pergunta e resposta.")
                .addOption(OptionType.STRING, "tempo", "O tempo para responder em segundos (ex: 30).", true)
                .addOption(OptionType.STRING, "pergunta", "A pergunta a ser exibida.", true)
                .addOption(OptionType.STRING, "resposta", "A resposta correta esperada.", true);
    }

    @Override
    protected Optional<Game> createGame(SlashCommandInteractionEvent event) {
        String tempoInput = event.getOption("tempo").getAsString();
        double tempoLimiteDouble;
        try {
            tempoLimiteDouble = Double.parseDouble(tempoInput.replace(',', '.'));
        } catch (NumberFormatException e) {
            event.reply("O tempo fornecido ('" + tempoInput + "') não é um número válido.").setEphemeral(true).queue();
            return Optional.empty();
        }

        String pergunta = event.getOption("pergunta").getAsString();
        String resposta = event.getOption("resposta").getAsString();
        long tempoLimiteMs = (long) (tempoLimiteDouble * 1000);
        String issuerId = event.getUser().getId();

        return Optional.of(new RespostaGame(tempoLimiteMs, pergunta, resposta, issuerId));
    }

    @Override
    protected String getPrepareMessage() {
        return "O jogo de perguntas vai começar em 3 segundos...";
    }

    @Override
    protected String getStartMessage(Game game) {
        RespostaGame respostaGame = (RespostaGame) game;
        double tempoEmSegundos = respostaGame.getTempoLimiteMs() / 1000.0;

        return String.format(
                "Tempo limite: **%.1f segundos**\n\nQual é a resposta para:\n>>> %s",
                tempoEmSegundos,
                respostaGame.getPergunta()
        );
    }

    @Override
    protected String getTimeoutMessage(Game game) {
        RespostaGame respostaGame = (RespostaGame) game;
        return "O tempo esgotou! A resposta correta era: `" + respostaGame.getRespostaCorreta() + "`";
    }
}