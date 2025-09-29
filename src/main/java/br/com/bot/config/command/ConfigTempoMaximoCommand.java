package br.com.bot.config.command;

import br.com.bot.core.ConfigManager;
import br.com.bot.shared.ICommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class ConfigTempoMaximoCommand implements ICommand {
    private final ConfigManager configManager;

    public ConfigTempoMaximoCommand(ConfigManager configManager) {
        this.configManager = configManager;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        long tempo = event.getOption("segundos").getAsLong();
        String guildId = event.getGuild().getId();

        if (tempo <= 0) {
            // Um valor negativo ou zero desativa o limite
            configManager.setMaxGameTime(guildId, -1);
            event.reply("✅ O limite máximo de tempo para jogos foi **desativado**.").queue();
        } else {
            configManager.setMaxGameTime(guildId, tempo);
            event.reply(String.format("✅ O tempo máximo para qualquer jogo foi definido para **%d segundos**.", tempo)).queue();
        }
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash("config-tempomaximo", "Define o tempo máximo em segundos que um jogo pode durar.")
                .addOption(OptionType.INTEGER, "segundos", "O tempo máximo. Digite 0 ou um número negativo para desativar.", true)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR));
    }
}