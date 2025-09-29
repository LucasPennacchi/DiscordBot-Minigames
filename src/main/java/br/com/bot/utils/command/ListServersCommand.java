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

public class ListServersCommand implements ICommand {

    public ListServersCommand() {}

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

            // Verifica se a mensagem não excederá o limite de 2000 caracteres do Discord
            if (responseBuilder.length() + serverInfo.length() > 2000) {
                // Se for exceder, envia o que já temos e começa uma nova mensagem.
                // Esta é uma forma de paginação simples.
                event.getChannel().sendMessage(responseBuilder.toString()).queue();
                responseBuilder = new StringBuilder();
            }
            responseBuilder.append(serverInfo);
        }

        // Responde ao comando com a mensagem final (ou a primeira, se a lista for pequena).
        // Usamos setEphemeral(true) para que só quem digitou o comando veja a lista.
        event.reply(responseBuilder.toString()).setEphemeral(true).queue();
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash("servidores", "Lista todos os servidores em que o bot está.")
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR));
    }
}