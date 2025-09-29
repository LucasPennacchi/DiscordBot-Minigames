package br.com.bot.core;

import br.com.bot.shared.Game;
import br.com.bot.shared.ICommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class GameCommands extends ListenerAdapter {
    private final GameManager gameManager;
    private final ConfigManager configManager;
    private final Map<String, ICommand> commands;

    public GameCommands(GameManager gameManager, ConfigManager configManager, Map<String, ICommand> commands) {
        this.gameManager = gameManager;
        this.configManager = configManager;
        this.commands = commands;
    }

    public Map<String, ICommand> getCommands() {
        return commands;
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        String commandName = event.getName();
        ICommand command = commands.get(commandName);

        if (command == null) {
            event.reply("Comando desconhecido.").setEphemeral(true).queue();
            return;
        }

        // --- VERIFICAÇÃO CENTRALIZADA ---
        // Se o comando for executado em um servidor (não em DM)
        if (event.getGuild() != null) {
            String channelId = event.getChannel().getId();
            String guildId = event.getGuild().getId();
            ServerConfig config = configManager.getConfig(guildId);

            // Verifica se o canal está bloqueado, mas permite comandos de configuração
            if (config.getBlockedChannelIds().contains(channelId) && !commandName.startsWith("config")) {
                event.reply("Este canal está bloqueado para o uso de comandos de jogo.").setEphemeral(true).queue();
                return;
            }
        }
        // --- FIM DA VERIFICAÇÃO ---

        // Se todas as verificações passarem, executa o comando.
        command.execute(event);
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;

        // Lógica de menção ao bot
        if (event.getMessage().getMentions().getUsers().contains(event.getJDA().getSelfUser())) {
            event.getChannel().sendMessage("Quack!").queue();
            return;
        }

        // Lógica de resposta para jogos ativos
        String channelId = event.getChannel().getId();
        Game jogo = gameManager.getJogo(channelId);

        if (jogo != null) {
            jogo.processarResposta(event, gameManager, configManager);
        }
    }
}