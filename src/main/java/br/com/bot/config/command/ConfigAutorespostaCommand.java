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
 * Comando de configuração para permitir ou proibir que o criador de um jogo possa respondê-lo.
 * <p>
 * Este comando é restrito a administradores e altera uma configuração persistente
 * para o servidor (guild) onde é executado, utilizando o {@link ConfigManager}.
 *
 * @author Lucas
 */
public class ConfigAutorespostaCommand implements ICommand {

    /** Gerenciador de configurações de servidor. */
    private final ConfigManager configManager;

    /**
     * Constrói o comando de configuração com suas dependências.
     *
     * @param configManager O gerenciador de configurações, necessário para alterar e salvar a regra.
     */
    public ConfigAutorespostaCommand(ConfigManager configManager) {
        this.configManager = configManager;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Executa a lógica de atualização da configuração. Lê a opção booleana 'permitir',
     * chama o {@link ConfigManager} para persistir a alteração e envia uma mensagem
     * de confirmação para o administrador.
     */
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

        // A resposta não é efêmera para que outros admins possam ver a mudança.
        event.reply(resposta).queue();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Cria e retorna a definição do comando /config-autoresposta para o Discord.
     * O comando é restrito a membros com a permissão de Administrador por padrão.
     *
     * @return A definição do comando de barra.
     */
    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash("config-autoresposta", "Configura se o criador de um jogo pode respondê-lo.")
                .addOption(OptionType.BOOLEAN, "permitir", "Defina como 'True' para permitir ou 'False' para proibir.", true)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR));
    }
}