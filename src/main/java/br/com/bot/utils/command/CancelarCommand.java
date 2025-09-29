package br.com.bot.utils.command;

import br.com.bot.core.GameManager;
import br.com.bot.shared.ICommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

/**
 * Um comando de utilidade para cancelar qualquer jogo que esteja ativo em um canal.
 * <p>
 * Este comando verifica se há uma partida em andamento e, em caso afirmativo, a encerra,
 * notificando o canal. Pode ser configurado para ser restrito a moderadores.
 *
 * @author Lucas
 */
public class CancelarCommand implements ICommand {

    /** Gerenciador de estado para jogos ativos. */
    private final GameManager gameManager;

    /**
     * Constrói o comando de cancelamento com suas dependências.
     *
     * @param gameManager O gerenciador de jogos ativos, necessário para finalizar uma partida.
     */
    public CancelarCommand(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Executa a lógica de cancelamento. Verifica se um jogo está ativo no canal,
     * finaliza-o se estiver, e envia uma resposta de confirmação. Se não houver
     * jogo, envia uma mensagem de erro efêmera.
     */
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

    /**
     * {@inheritDoc}
     * <p>
     * Cria e retorna a definição do comando /cancelar para o Discord.
     * A linha de permissão está comentada, mas pode ser reativada para restringir
     * o uso do comando a membros com a permissão de "Gerenciar Canal".
     *
     * @return A definição do comando de barra.
     */
    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash("cancelar", "Cancela qualquer jogo ativo no canal.");
        // IMPORTANTE: A linha abaixo, se descomentada, restringe o comando a moderadores.
        // .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_CHANNEL));
    }
}