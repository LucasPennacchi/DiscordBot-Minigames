package br.com.bot.config.command;

import br.com.bot.core.ConfigManager;
import br.com.bot.shared.ICommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class ConfigAutorespostaCommand implements ICommand {
    private final ConfigManager configManager;

    public ConfigAutorespostaCommand(ConfigManager configManager) {
        this.configManager = configManager;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        if (event.getGuild() == null) {
            event.reply("Este comando só pode ser usado em um servidor.").setEphemeral(true).queue();
            return;
        }

        boolean permitir = event.getOption("permitir").getAsBoolean();
        String guildId = event.getGuild().getId();

        configManager.setAllowCreatorToPlay(guildId, permitir);

        String resposta = permitir
                ? "✅ Configuração atualizada: O criador do jogo **agora PODE** responder."
                : "❌ Configuração atualizada: O criador do jogo **NÃO PODE mais** responder.";

        event.reply(resposta).queue();
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash("config_autoresposta", "Configura se o criador de um jogo pode respondê-lo.")
                .addOption(OptionType.BOOLEAN, "permitir", "Defina como 'True' para permitir ou 'False' para proibir.", true)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR));
    }
}