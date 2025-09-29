package br.com.bot.utils.command;

import br.com.bot.shared.ICommand;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.util.List;

/**
 * Um comando de diagnóstico para o dono do bot.
 * <p>
 * Este comando lista todos os servidores (Guilds) em que o bot está atualmente,
 * incluindo nome, ID e contagem de membros de cada um. O uso é restrito a administradores
 * para evitar a exposição de informações sobre outros servidores.
 *
 * @author Lucas
 */
public class ListServersCommand implements ICommand {

    /**
     * Constrói uma nova instância do comando ListServers.
     * Este comando não possui dependências.
     */
    public ListServersCommand() {}

    /**
     * {@inheritDoc}
     * <p>
     * Executa a lógica para buscar e formatar a lista de servidores. A resposta é enviada
     * de forma efêmera (visível apenas para quem executou o comando) e possui uma
     * paginação simples para evitar exceder o limite de 2000 caracteres do Discord.
     */
    @Override
    public void execute(SlashCommandInteractionEvent event) {
        JDA jda = event.getJDA();
        List<Guild> servers = jda.getGuilds();

        // Usa um StringBuilder para construir uma única mensagem grande de forma eficiente
        StringBuilder responseBuilder = new StringBuilder();
        responseBuilder.append("O bot está em **").append(servers.size()).append("** servidores:\n\n");

        for (Guild server : servers) {
            String serverInfo = String.format(
                    "- **%s** (ID: %s) - %d membros\n",
                    server.getName(),
                    server.getId(),
                    server.getMemberCount()
            );

            // Verifica se a mensagem não excederá o limite de caracteres do Discord
            if (responseBuilder.length() + serverInfo.length() > 2000) {
                // Se for exceder, envia o que já temos e começa uma nova mensagem.
                event.getChannel().sendMessage(responseBuilder.toString()).queue();
                responseBuilder = new StringBuilder();
            }
            responseBuilder.append(serverInfo);
        }

        // Responde ao comando com a mensagem final (ou a primeira, se a lista for pequena).
        event.reply(responseBuilder.toString()).setEphemeral(true).queue();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Cria e retorna a definição do comando /servidores para o Discord,
     * restringindo seu uso padrão a membros com a permissão de Administrador.
     */
    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash("servidores", "Lista todos os servidores em que o bot está.")
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR));
    }
}