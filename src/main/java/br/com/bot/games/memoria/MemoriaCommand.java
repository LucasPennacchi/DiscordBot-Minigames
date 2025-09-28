package br.com.bot.games.memoria;

import br.com.bot.core.GameManager;
import br.com.bot.shared.Game;
import br.com.bot.shared.ICommand;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MemoriaCommand implements ICommand {
    private final GameManager gameManager;
    private final ScheduledExecutorService scheduler;

    public MemoriaCommand(GameManager gameManager, ScheduledExecutorService scheduler) {
        this.gameManager = gameManager;
        this.scheduler = scheduler;
    }

    // Metodo auxiliar para validar e converter o tempo
    private long parseTimeInput(String input) throws NumberFormatException {
        String normalized = input.replace(',', '.');
        double seconds = Double.parseDouble(normalized);
        return (long) (seconds * 1000);
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        long tempoOcultarMs;
        long tempoLimiteMs;

        try {
            tempoOcultarMs = parseTimeInput(event.getOption("tempo_ocultar").getAsString());
            tempoLimiteMs = parseTimeInput(event.getOption("tempo").getAsString());
        } catch (NumberFormatException e) {
            event.reply("O tempo fornecido não é um número válido.").setEphemeral(true).queue();
            return;
        }

        String stringSecreta = event.getOption("frase").getAsString();

        // 1. Envia a mensagem inicial com a string e guarda o ID da mensagem
        event.reply("Memorize a seguinte string...").queue();
        event.getChannel().sendMessage("`" + stringSecreta + "`").queue(message -> {
            String messageId = message.getId();

            // 2. Agenda a edição da mensagem (ocultar a string)
            scheduler.schedule(() -> {
                // Edita a mensagem para esconder a string e iniciar o desafio
                event.getChannel().editMessageById(messageId, "**Qual era a string?** Você tem " + (tempoLimiteMs / 1000.0) + " segundos!").queue();

                // 3. AGORA SIM o jogo começa. Cria a instância e a registra.
                String issuerId = event.getUser().getId(); // Pega o ID do usuário
                MemoriaGame novoJogo = new MemoriaGame(tempoLimiteMs, stringSecreta, issuerId); // Passa o ID para o construtor
                gameManager.iniciarJogo(event.getChannel().getId(), novoJogo);

                // 4. Agenda o fim do jogo se o tempo limite esgotar
                scheduler.schedule(() -> {
                    Game jogoFinalizado = gameManager.finalizarJogo(event.getChannel().getId());
                    if (jogoFinalizado != null) {
                        event.getChannel().sendMessage("O tempo esgotou! A string correta era: `" + ((MemoriaGame) jogoFinalizado).getStringSecreta() + "`").queue();
                    }
                }, tempoLimiteMs, TimeUnit.MILLISECONDS);

            }, tempoOcultarMs, TimeUnit.MILLISECONDS);
        });
    }
}