package br.com.bot.core;

import java.util.HashSet;
import java.util.Set;

public class ServerConfig {
    private boolean allowCreatorToPlay = false;

    // Tempo máximo em segundos. Usamos -1 como padrão para "sem limite".
    private long maxGameTimeSeconds = -1;

    // Lista de IDs de canais que estão bloqueados.
    private Set<String> blockedChannelIds = new HashSet<>();

    // Getters e Setters
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
        return new HashSet<>(blockedChannelIds);
    }
    public void blockChannel(String channelId) {
        if (this.blockedChannelIds == null) {
            this.blockedChannelIds = new HashSet<>();
        }
        this.blockedChannelIds.add(channelId);
    }
    public void unblockChannel(String channelId) {
        if (this.blockedChannelIds != null) {
            this.blockedChannelIds.remove(channelId);
        }
    }
}