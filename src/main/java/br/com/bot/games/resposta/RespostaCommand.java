package br.com.bot.games.resposta;

import br.com.bot.shared.Game;
import br.com.bot.shared.ICommand;
import br.com.bot.core.GameManager;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class RespostaCommand implements ICommand {
    private final GameManager gameManager;
    private final ScheduledExecutorService scheduler;

    public RespostaCommand(GameManager gameManager, ScheduledExecutorService scheduler) {
        this.gameManager = gameManager;
        this.scheduler = scheduler;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        // Validação e lógica para o comando /resposta
        String tempoInput = event.getOption("tempo").getAsString();
        String tempoNormalizado = tempoInput.replace(',', '.');
        double tempoLimiteDouble;
        try {
            tempoLimiteDouble = Double.parseDouble(tempoNormalizado);
        } catch (NumberFormatException e) {
            event.reply("O tempo fornecido ('" + tempoInput + "') não é um número válido.").setEphemeral(true).queue();
            return;
        }

        String pergunta = event.getOption("pergunta").getAsString();
        String resposta = event.getOption("resposta").getAsString();
        long tempoLimiteMs = (long) (tempoLimiteDouble * 1000);

        event.reply("O jogo de perguntas vai começar em 3 segundos...").queue();

        scheduler.schedule(() -> {
            event.getChannel().sendMessage(String.format("Tempo limite: %.2f\n",(double)tempoLimiteMs/1000) + "Qual é a resposta para:\n>>> " + pergunta).queue();
            String issuerId = event.getUser().getId(); // Pega o ID do usuário
            RespostaGame novoJogo = new RespostaGame(tempoLimiteMs, pergunta, resposta, issuerId); // Passa o ID para o construtor
            gameManager.iniciarJogo(event.getChannel().getId(), novoJogo);

            scheduler.schedule(() -> {
                Game jogoFinalizado = gameManager.finalizarJogo(event.getChannel().getId());
                if (jogoFinalizado != null) {
                    // Garante que o jogo finalizado é do tipo correto para pegar a resposta
                    if (jogoFinalizado instanceof RespostaGame) {
                        event.getChannel().sendMessage("O tempo esgotou! A resposta correta era: `" + ((RespostaGame) jogoFinalizado).getRespostaCorreta() + "`").queue();
                    }
                }
            }, tempoLimiteMs, TimeUnit.MILLISECONDS);
        }, 3, TimeUnit.SECONDS);
    }
}