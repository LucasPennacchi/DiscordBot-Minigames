package br.com.bot.core;

import br.com.bot.games.memoria.MemoriaCommand;
import br.com.bot.shared.Game;
import br.com.bot.shared.ICommand;
import br.com.bot.games.reflexo.ReflexosCommand;
import br.com.bot.games.resposta.RespostaCommand;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class GameCommands extends ListenerAdapter {
    private final GameManager gameManager;
    private final Map<String, ICommand> commands = new ConcurrentHashMap<>();

    public GameCommands(GameManager gameManager) {
        this.gameManager = gameManager;
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        // Mapeia o nome do comando para a classe que o implementa
        commands.put("reflexos", new ReflexosCommand(gameManager, scheduler));
        commands.put("resposta", new RespostaCommand(gameManager, scheduler));
        commands.put("memoria", new MemoriaCommand(gameManager, scheduler));
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        String commandName = event.getName();
        ICommand command = commands.get(commandName);

        if (command != null) {
            // Delega a execução para o objeto de comando correto.
            command.execute(event);
        }
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;

        Game jogo = gameManager.getJogo(event.getChannel().getId());

        if (jogo != null) {
            // Delega o processamento da resposta para o próprio objeto do jogo.
            jogo.processarResposta(event, gameManager);
        }
    }

}