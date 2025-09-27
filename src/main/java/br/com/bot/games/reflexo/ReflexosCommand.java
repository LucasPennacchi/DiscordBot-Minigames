package br.com.bot.games.reflexo;

import br.com.bot.shared.ICommand;
import br.com.bot.core.GameManager;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ReflexosCommand implements ICommand {
    private final GameManager gameManager;
    private final ScheduledExecutorService scheduler;

    public ReflexosCommand(GameManager gameManager, ScheduledExecutorService scheduler) {
        this.gameManager = gameManager;
        this.scheduler = scheduler;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        // Toda a lógica de validação e início do jogo de reflexo vem para cá.
        String tempoInput = event.getOption("timer").getAsString();
        String tempoNormalizado = tempoInput.replace(',', '.');
        double tempoLimiteDouble;
        try {
            tempoLimiteDouble = Double.parseDouble(tempoNormalizado);
        } catch (NumberFormatException e) {
            event.reply("O tempo fornecido ('" + tempoInput + "') não é um número válido.").setEphemeral(true).queue();
            return;
        }

        String frase = event.getOption("frase").getAsString();
        long tempoLimiteMs = (long) (tempoLimiteDouble * 1000);

        event.reply("O teste de reflexo vai começar em 3 segundos... Prepare-se!").queue();

        scheduler.schedule(() -> {
            event.getChannel().sendMessage(String.format("Tempo limite: %.2f\n",(double)tempoLimiteMs/1000) + "VAI! Digite a frase: `" + frase + "`").queue();
            ReflexoGame novoJogo = new ReflexoGame(frase, tempoLimiteMs);
            gameManager.iniciarJogo(event.getChannel().getId(), novoJogo);

            scheduler.schedule(() -> {
                if (gameManager.finalizarJogo(event.getChannel().getId()) != null) {
                    event.getChannel().sendMessage("O tempo esgotou e ninguém respondeu!").queue();
                }
            }, tempoLimiteMs, TimeUnit.MILLISECONDS);
        }, 3, TimeUnit.SECONDS);
    }
}