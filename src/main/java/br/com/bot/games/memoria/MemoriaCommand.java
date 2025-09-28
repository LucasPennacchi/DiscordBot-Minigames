package br.com.bot.games.memoria;

import br.com.bot.core.GameManager;
import br.com.bot.shared.Game;
import br.com.bot.shared.ICommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MemoriaCommand implements ICommand {
    private final GameManager gameManager;
    private final ScheduledExecutorService scheduler;

    // Constante para o tempo de preparo, para consistência
    private static final int PREPARE_DELAY_SECONDS = 3;

    public MemoriaCommand(GameManager gameManager, ScheduledExecutorService scheduler) {
        this.gameManager = gameManager;
        this.scheduler = scheduler;
    }

    private long parseTimeInput(String input) throws NumberFormatException {
        return (long) (Double.parseDouble(input.replace(',', '.')) * 1000);
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        String channelId = event.getChannel().getId();
        if (gameManager.isJogoAtivo(channelId)) {
            event.reply("Já existe um jogo ativo neste canal!").setEphemeral(true).queue();
            return;
        }

        long tempoOcultarMs;
        long tempoLimiteMs;
        try {
            tempoOcultarMs = parseTimeInput(event.getOption("tempo_ocultar").getAsString());
            tempoLimiteMs = parseTimeInput(event.getOption("tempo_limite").getAsString());
        } catch (NumberFormatException e) {
            event.reply("O tempo fornecido não é um número válido.").setEphemeral(true).queue();
            return;
        }

        String stringSecreta = event.getOption("string").getAsString();
        String issuerId = event.getUser().getId();

        // 1. Envia a nova mensagem de preparo
        event.reply(String.format("O jogo da memória vai começar em %d segundos... Prepare-se!", PREPARE_DELAY_SECONDS)).queue();

        // 2. Agenda a lógica principal do jogo para depois do tempo de preparo
        scheduler.schedule(() -> {
            event.getChannel().sendMessage("Memorize a seguinte string:\n`" + stringSecreta + "`").queue(message -> {
                String messageId = message.getId();

                // Agenda a edição da mensagem (ocultar)
                scheduler.schedule(() -> {
                    double tempoLimiteSeg = tempoLimiteMs / 1000.0;
                    event.getChannel().editMessageById(messageId, String.format("**Qual era a string?** Você tem %.1f segundos!", tempoLimiteSeg)).queue();

                    MemoriaGame novoJogo = new MemoriaGame(tempoLimiteMs, stringSecreta, issuerId);
                    gameManager.iniciarJogo(channelId, novoJogo);

                    // Agenda o timeout do jogo
                    scheduler.schedule(() -> {
                        Game jogoFinalizado = gameManager.finalizarJogo(channelId);
                        if (jogoFinalizado != null) {
                            event.getChannel().sendMessage("O tempo esgotou! A string correta era: `" + ((MemoriaGame) jogoFinalizado).getStringSecreta() + "`").queue();
                        }
                    }, tempoLimiteMs, TimeUnit.MILLISECONDS);

                }, tempoOcultarMs, TimeUnit.MILLISECONDS);
            });
        }, PREPARE_DELAY_SECONDS, TimeUnit.SECONDS);
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash("memoria", "Inicia um jogo de memória.")
                .addOption(OptionType.STRING, "tempo_ocultar", "Tempo para memorizar a string (ex: 5).", true)
                .addOption(OptionType.STRING, "tempo_limite", "Tempo para responder após a string sumir (ex: 10).", true)
                .addOption(OptionType.STRING, "string", "A string a ser memorizada.", true);
    }
}