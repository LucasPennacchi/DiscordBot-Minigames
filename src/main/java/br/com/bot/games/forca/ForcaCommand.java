package br.com.bot.games.forca;

import br.com.bot.core.GameManager;
import br.com.bot.shared.AbstractGameCommand; // Importa para usar a constante
import br.com.bot.shared.Game;
import br.com.bot.shared.ICommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

// Implementa ICommand diretamente, pois é um caso especial
public class ForcaCommand implements ICommand {
    private final GameManager gameManager;
    private final ScheduledExecutorService scheduler;

    public ForcaCommand(GameManager gameManager, ScheduledExecutorService scheduler) {
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

        long tempoLimiteMs;
        try {
            tempoLimiteMs = parseTimeInput(event.getOption("tempo").getAsString());
        } catch (NumberFormatException e) {
            event.reply("O tempo fornecido não é um número válido.").setEphemeral(true).queue();
            return;
        }

        String palavra = event.getOption("palavra").getAsString();
        int erros = event.getOption("erros", 6, OptionMapping::getAsInt);

        if (erros < 1) {
            event.reply("O número de erros deve ser no mínimo 1.").setEphemeral(true).queue();
            return;
        }

        String issuerId = event.getUser().getId();

        // Usa a constante PÚBLICA de AbstractGameCommand, evitando duplicação
        event.reply(String.format("O jogo da forca vai começar em %d segundos...", AbstractGameCommand.PREPARE_DELAY_SECONDS)).queue();

        scheduler.schedule(() -> {
            ForcaGame novoJogo = new ForcaGame(tempoLimiteMs, palavra, erros, issuerId);

            event.getChannel().sendMessageEmbeds(novoJogo.buildGameEmbed()).queue(message -> {
                novoJogo.setMessageId(message.getId());
                gameManager.iniciarJogo(channelId, novoJogo);

                scheduler.schedule(() -> {
                    Game jogoFinalizado = gameManager.finalizarJogo(channelId);
                    if (jogoFinalizado != null) {
                        event.getChannel().sendMessage("O tempo para o jogo da forca esgotou! A palavra era `" + ((ForcaGame) jogoFinalizado).getPalavraSecreta() + "`.").queue();
                    }
                }, tempoLimiteMs, TimeUnit.MILLISECONDS);
            });

        }, AbstractGameCommand.PREPARE_DELAY_SECONDS, TimeUnit.SECONDS);
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash("forca", "Inicia um jogo da forca.")
                .addOption(OptionType.STRING, "palavra", "A palavra secreta a ser adivinhada.", true)
                .addOption(OptionType.INTEGER, "erros", "O número máximo de erros permitidos (mínimo 1).", true)
                .addOption(OptionType.STRING, "tempo", "O tempo limite em segundos (ex: 120).", true);
    }
}