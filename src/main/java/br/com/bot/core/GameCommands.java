package br.com.bot.core;

import br.com.bot.shared.Game;
import br.com.bot.shared.ICommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * A classe central de listeners de eventos do Discord.
 * <p>
 * Atua como um "dispatcher" (despachante) ou "gatekeeper" (porteiro). Ela recebe todos
 * os eventos de comando e mensagem, realiza verificações de pré-execução universais
 * (como a de canais bloqueados) e delega a ação para a classe apropriada.
 *
 * @author Lucas
 */
public class GameCommands extends ListenerAdapter {

    /** Gerenciador de estado para jogos ativos. */
    private final GameManager gameManager;
    /** Gerenciador de configurações persistentes de servidor. */
    private final ConfigManager configManager;
    /** Mapa de todos os comandos registrados, associando nome ao objeto de comando. */
    private final Map<String, ICommand> commands;

    /**
     * Constrói o listener de comandos com suas dependências injetadas.
     *
     * @param gameManager O gerenciador de jogos ativos.
     * @param configManager O gerenciador de configurações de servidor.
     * @param commands O mapa de todos os comandos disponíveis.
     */
    public GameCommands(GameManager gameManager, ConfigManager configManager, Map<String, ICommand> commands) {
        this.gameManager = gameManager;
        this.configManager = configManager;
        this.commands = commands;
    }

    /**
     * Retorna o mapa de comandos registrados.
     * Usado pelo {@link BotMain} para o processo de registro de comandos no Discord.
     *
     * @return O mapa de comandos.
     */
    public Map<String, ICommand> getCommands() {
        return commands;
    }

    /**
     * Chamado pelo JDA sempre que uma interação de comando de barra é recebida.
     * <p>
     * Este método localiza o comando correspondente, executa verificações de permissão
     * universais (como canais bloqueados) e, se tudo estiver correto, delega a
     * execução para o objeto {@link ICommand} apropriado.
     *
     * @param event O evento da interação de comando.
     */
    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        String commandName = event.getName();
        ICommand command = commands.get(commandName);

        if (command == null) {
            event.reply("Comando desconhecido.").setEphemeral(true).queue();
            return;
        }

        // --- VERIFICAÇÃO CENTRALIZADA ---
        if (event.getGuild() != null) {
            String channelId = event.getChannel().getId();
            String guildId = event.getGuild().getId();
            ServerConfig config = configManager.getConfig(guildId);

            // Bloqueia comandos de jogo, mas permite comandos de configuração
            if (config.getBlockedChannelIds().contains(channelId) && !commandName.startsWith("config")) {
                event.reply("Este canal está bloqueado para o uso de comandos de jogo.").setEphemeral(true).queue();
                return;
            }
        }
        // --- FIM DA VERIFICAÇÃO ---

        command.execute(event);
    }

    /**
     * Chamado pelo JDA sempre que uma mensagem é recebida em um canal visível para o bot.
     * <p>
     * Este método tem duas responsabilidades principais:
     * <ol>
     * <li>Responder com "Quack!" se o bot for mencionado.</li>
     * <li>Verificar se há um jogo ativo no canal e, em caso afirmativo, passar a mensagem
     * para o objeto do jogo para que ela seja processada como uma resposta.</li>
     * </ol>
     *
     * @param event O evento da mensagem recebida.
     */
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