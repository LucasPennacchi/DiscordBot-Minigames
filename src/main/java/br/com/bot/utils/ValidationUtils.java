package br.com.bot.utils;

import br.com.bot.core.ConfigManager;
import br.com.bot.core.ServerConfig;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class ValidationUtils {

    /**
     * Verifica se o tempo de jogo solicitado excede o limite máximo configurado para o servidor.
     * Se exceder, envia uma mensagem de erro efêmera.
     * @param event O evento do comando.
     * @param configManager O gerenciador de configurações.
     * @param requestedTimeMs O tempo solicitado pelo usuário, em milissegundos.
     * @return {@code true} se o tempo for válido, {@code false} se for inválido.
     */
    public static boolean checkMaxGameTime(SlashCommandInteractionEvent event, ConfigManager configManager, long requestedTimeMs) {
        if (event.getGuild() == null) return true; // Não se aplica fora de servidores

        ServerConfig config = configManager.getConfig(event.getGuild().getId());
        long maxGameTimeSeconds = config.getMaxGameTimeSeconds();

        if (maxGameTimeSeconds > 0 && (requestedTimeMs / 1000) > maxGameTimeSeconds) {
            event.reply(String.format("O tempo solicitado excede o limite máximo de %d segundos definido para este servidor.", maxGameTimeSeconds))
                    .setEphemeral(true).queue();
            return false; // Validação falhou
        }
        return true; // Validação passou
    }
}