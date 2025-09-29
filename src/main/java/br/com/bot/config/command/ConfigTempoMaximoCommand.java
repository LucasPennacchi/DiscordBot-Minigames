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
 * Comando de configuração para definir um tempo máximo de duração para todos os jogos em um servidor.
 * <p>
 * Este comando é restrito a administradores e altera uma configuração persistente
 * para o servidor (guild) onde é executado, utilizando o {@link ConfigManager}.
 *
 * @author Lucas
 */
public class ConfigTempoMaximoCommand implements ICommand {

    /** Gerenciador de configurações de servidor. */
    private final ConfigManager configManager;

    /**
     * Constrói o comando de configuração com suas dependências.
     *
     * @param configManager O gerenciador de configurações, necessário para alterar e salvar a regra.
     */
    public ConfigTempoMaximoCommand(ConfigManager configManager) {
        this.configManager = configManager;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Executa a lógica de atualização da configuração. Lê a opção 'segundos',
     * tratando valores menores ou iguais a zero como uma forma de desativar o limite.
     * A alteração é persistida pelo {@link ConfigManager} e uma mensagem de confirmação
     * é enviada para o administrador.
     */
    @Override
    public void execute(SlashCommandInteractionEvent event) {
        long tempo = event.getOption("segundos").getAsLong();
        String guildId = event.getGuild().getId();

        if (tempo <= 0) {
            // Um valor negativo ou zero desativa o limite, salvando -1 na configuração.
            configManager.setMaxGameTime(guildId, -1);
            event.reply("✅ O limite máximo de tempo para jogos foi **desativado**.").queue();
        } else {
            configManager.setMaxGameTime(guildId, tempo);
            event.reply(String.format("✅ O tempo máximo para qualquer jogo foi definido para **%d segundos**.", tempo)).queue();
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Cria e retorna a definição do comando /config-tempomaximo para o Discord.
     * O comando é restrito a membros com a permissão de Administrador por padrão.
     *
     * @return A definição do comando de barra.
     */
    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash("config-tempomaximo", "Define o tempo máximo em segundos que um jogo pode durar.")
                .addOption(OptionType.INTEGER, "segundos", "O tempo máximo. Digite 0 ou um número negativo para desativar.", true)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR));
    }
}