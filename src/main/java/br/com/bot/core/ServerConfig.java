package br.com.bot.core;

import java.util.HashSet;
import java.util.Set;

public class ServerConfig {
    private boolean allowCreatorToPlay = false;

    // NOVO: Tempo máximo em segundos. Usamos -1 como padrão para "sem limite".
    private long maxGameTimeSeconds = -1;

    // NOVO: Lista de IDs de canais que estão bloqueados.
    private Set<String> blockedChannelIds = new HashSet<>();

    // Getters e Setters para as novas configurações
    public boolean isAllowCreatorToPlay() {
        return allowCreatorToPlay;
    }

    public void setAllowCreatorToPlay(boolean allowCreatorToPlay) {
        this.allowCreatorToPlay = allowCreatorToPlay;
    }

    public long getMaxGameTimeSeconds() {
        return maxGameTimeSeconds;
    }

    public void setMaxGameTimeSeconds(long maxGameTimeSeconds) {
        this.maxGameTimeSeconds = maxGameTimeSeconds;
    }

    public Set<String> getBlockedChannelIds() {
        return blockedChannelIds;
    }

    // Métodos para adicionar e remover canais da lista de bloqueio
    public void blockChannel(String channelId) {
        blockedChannelIds.add(channelId);
    }

    public void unblockChannel(String channelId) {
        blockedChannelIds.remove(channelId);
    }
}