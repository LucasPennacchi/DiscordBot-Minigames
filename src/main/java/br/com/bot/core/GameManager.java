package br.com.bot.core;

import br.com.bot.shared.Game;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GameManager {
    // Agora o Map armazena a classe base 'Game', aceitando qualquer tipo de jogo.
    private final Map<String, Game> jogosAtivos = new ConcurrentHashMap<>();

    public void iniciarJogo(String channelId, Game game) {
        jogosAtivos.put(channelId, game);
    }

    public Game finalizarJogo(String channelId) {
        return jogosAtivos.remove(channelId);
    }

    public Game getJogo(String channelId) {
        return jogosAtivos.get(channelId);
    }

    public boolean isJogoAtivo(String channelId) {
        return jogosAtivos.containsKey(channelId);
    }
}