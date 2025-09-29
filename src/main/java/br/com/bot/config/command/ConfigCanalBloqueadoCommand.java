package br.com.bot.config.command;

import br.com.bot.core.ConfigManager;
import br.com.bot.shared.ICommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class ConfigCanalBloqueadoCommand implements ICommand {
    private final ConfigManager configManager;

    public ConfigCanalBloqueadoCommand(ConfigManager configManager) {
        this.configManager = configManager;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        if (event.getGuild() == null) {
            event.reply("Este comando só pode ser usado em um servidor.").setEphemeral(true).queue();
            return;
        }

        boolean bloquear = event.getOption("bloquear").getAsBoolean();
        String guildId = event.getGuild().getId();
        String channelId = event.getChannel().getId();

        if (bloquear) {
            configManager.blockChannel(guildId, channelId);
            event.reply("✅ Este canal foi **bloqueado**. Nenhum jogo poderá ser iniciado aqui.").setEphemeral(true).queue();
        } else {
            configManager.unblockChannel(guildId, channelId);
            event.reply("✅ Este canal foi **desbloqueado**. Os jogos estão permitidos novamente.").setEphemeral(true).queue();
        }
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash("config-canalbloqueado", "Bloqueia ou desbloqueia o canal atual para jogos.")
                .addOption(OptionType.BOOLEAN, "bloquear", "Defina como 'True' para bloquear ou 'False' para desbloquear.", true)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_CHANNEL));
    }
}