package br.com.bot.config.command;

import br.com.bot.core.ConfigManager;
import br.com.bot.shared.ICommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

/**
 * Comando de configuração para bloquear ou desbloquear o canal atual para o uso de jogos.
 * <p>
 * Este comando é restrito a moderadores e altera uma configuração persistente
 * para o servidor (guild) onde é executado, adicionando ou removendo o ID do canal
 * da lista de canais bloqueados.
 *
 * @author Lucas
 */
public class ConfigCanalBloqueadoCommand implements ICommand {

    /** Gerenciador de configurações de servidor. */
    private final ConfigManager configManager;

    /**
     * Constrói o comando de configuração com suas dependências.
     *
     * @param configManager O gerenciador de configurações, necessário para alterar e salvar a regra.
     */
    public ConfigCanalBloqueadoCommand(ConfigManager configManager) {
        this.configManager = configManager;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Executa a lógica de atualização da configuração. Lê a opção booleana 'bloquear' e
     * o ID do canal atual, chama o {@link ConfigManager} para persistir a alteração
     * e envia uma mensagem de confirmação efêmera para o moderador.
     */
    @Override
    public void execute(SlashCommandInteractionEvent event) {
        if (event.getGuild() == null) {
            event.reply("Este comando só pode ser usado em um servidor.").setEphemeral(true).queue();
            return;
        }

        boolean bloquear = event.getOption("bloquear").getAsBoolean();
        String guildId = event.getGuild().getId();
        String channelId = event.getChannel().getId(); // Pega o canal onde o comando foi usado

        if (bloquear) {
            configManager.blockChannel(guildId, channelId);
            event.reply("✅ Este canal foi **bloqueado**. Nenhum jogo poderá ser iniciado aqui.").setEphemeral(true).queue();
        } else {
            configManager.unblockChannel(guildId, channelId);
            event.reply("✅ Este canal foi **desbloqueado**. Os jogos estão permitidos novamente.").setEphemeral(true).queue();
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Cria e retorna a definição do comando /config-canalbloqueado para o Discord.
     * O comando é restrito a membros com a permissão de "Gerenciar Canais" por padrão.
     *
     * @return A definição do comando de barra.
     */
    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash("config-canalbloqueado", "Bloqueia ou desbloqueia o canal atual para jogos.")
                .addOption(OptionType.BOOLEAN, "bloquear", "Defina como 'True' para bloquear ou 'False' para desbloquear.", true)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_CHANNEL));
    }
}