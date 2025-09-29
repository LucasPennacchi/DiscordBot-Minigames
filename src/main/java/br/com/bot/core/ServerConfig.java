package br.com.bot.core;

import java.util.HashSet;
import java.util.Set;

/**
 * Representa um objeto de dados (POJO) que armazena todas as configurações personalizáveis
 * para um único servidor (Guild) do Discord.
 * <p>
 * Uma instância desta classe é criada para cada servidor que possui configurações customizadas
 * e é serializada para e de um arquivo JSON pelo {@link ConfigManager}.
 *
 * @author Lucas
 */
public class ServerConfig {

    /** Define se o criador de um jogo pode participar de sua própria partida. Padrão: {@code false}. */
    private boolean allowCreatorToPlay = false;

    /** Define o tempo máximo em segundos que qualquer jogo pode durar. Padrão: {@code -1} (desativado). */
    private long maxGameTimeSeconds = -1;

    /** Armazena um conjunto de IDs de canais onde os jogos são proibidos. */
    private Set<String> blockedChannelIds = new HashSet<>();

    /**
     * Verifica se o criador do jogo tem permissão para jogar neste servidor.
     *
     * @return {@code true} se o criador pode jogar, {@code false} caso contrário.
     */
    public boolean isAllowCreatorToPlay() {
        return allowCreatorToPlay;
    }

    /**
     * Define a permissão para o criador do jogo poder jogar.
     *
     * @param allowCreatorToPlay O novo valor da permissão.
     */
    public void setAllowCreatorToPlay(boolean allowCreatorToPlay) {
        this.allowCreatorToPlay = allowCreatorToPlay;
    }

    /**
     * Obtém o tempo máximo em segundos que um jogo pode durar neste servidor.
     *
     * @return O tempo máximo em segundos, ou um valor <= 0 se não houver limite.
     */
    public long getMaxGameTimeSeconds() {
        return maxGameTimeSeconds;
    }

    /**
     * Define o tempo máximo em segundos para os jogos.
     *
     * @param maxGameTimeSeconds O novo limite de tempo.
     */
    public void setMaxGameTimeSeconds(long maxGameTimeSeconds) {
        this.maxGameTimeSeconds = maxGameTimeSeconds;
    }

    /**
     * Retorna uma cópia do conjunto de IDs de canais bloqueados.
     *
     * @return Um {@link Set} contendo os IDs dos canais bloqueados.
     */
    public Set<String> getBlockedChannelIds() {
        // Retorna uma cópia para proteger a lista interna de modificações externas (encapsulamento).
        if (blockedChannelIds == null) {
            return new HashSet<>();
        }
        return new HashSet<>(blockedChannelIds);
    }

    /**
     * Adiciona um ID de canal à lista de canais bloqueados.
     * Inclui uma verificação de nulidade para segurança ao desserializar de um JSON antigo.
     *
     * @param channelId O ID do canal a ser bloqueado.
     */
    public void blockChannel(String channelId) {
        if (this.blockedChannelIds == null) {
            this.blockedChannelIds = new HashSet<>();
        }
        this.blockedChannelIds.add(channelId);
    }

    /**
     * Remove um ID de canal da lista de canais bloqueados.
     *
     * @param channelId O ID do canal a ser desbloqueado.
     */
    public void unblockChannel(String channelId) {
        if (this.blockedChannelIds != null) {
            this.blockedChannelIds.remove(channelId);
        }
    }
}