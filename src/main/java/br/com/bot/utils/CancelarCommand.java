package br.com.bot.utils;

import br.com.bot.core.GameManager;
import br.com.bot.shared.ICommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class CancelarCommand implements ICommand {
    private final GameManager gameManager;

    public CancelarCommand(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        String channelId = event.getChannel().getId();

        if (gameManager.isJogoAtivo(channelId)) {
            gameManager.finalizarJogo(channelId);
            event.reply("✅ O jogo ativo neste canal foi cancelado!").queue();
        } else {
            event.reply("ℹ️ Não há nenhum jogo ativo para cancelar neste canal.").setEphemeral(true).queue();
        }
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash("cancelar", "Cancela qualquer jogo ativo no canal.");
                // IMPORTANTE: Define a permissão padrão para este comando.
                // Apenas membros com a permissão de "Gerenciar Mensagens" (moderadores) poderão vê-lo e usá-lo.
                // .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MESSAGE_MANAGE));
    }
}