package br.com.bot.core;

import br.com.bot.shared.Game;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gerencia o estado em tempo real dos jogos ativos no bot.
 * <p>
 * Esta classe atua como um "gerenciador de tráfego" para as partidas, mantendo um registro
 * de qual jogo está acontecendo em qual canal. Ela utiliza um {@link ConcurrentHashMap}
 * para garantir a segurança em um ambiente com múltiplas threads (thread-safe).
 *
 * @author Lucas
 */
public class GameManager {

    /** Mapeia o ID de um canal (String) para a instância do jogo (Game) que está ativa nele. */
    private final Map<String, Game> jogosAtivos = new ConcurrentHashMap<>();

    /**
     * Inicia uma nova partida em um canal específico, registrando-a como ativa.
     *
     * @param channelId O ID do canal onde o jogo será iniciado.
     * @param game      A instância do jogo a ser iniciada.
     */
    public void iniciarJogo(String channelId, Game game) {
        jogosAtivos.put(channelId, game);
    }

    /**
     * Finaliza a partida ativa em um canal específico, removendo-a do registro.
     *
     * @param channelId O ID do canal cuja partida será finalizada.
     * @return O objeto {@link Game} que foi removido, ou {@code null} se não havia jogo ativo.
     */
    public Game finalizarJogo(String channelId) {
        return jogosAtivos.remove(channelId);
    }

    /**
     * Obtém a instância do jogo ativo em um canal, sem finalizá-lo.
     *
     * @param channelId O ID do canal a ser verificado.
     * @return O objeto {@link Game} ativo, ou {@code null} se não houver jogo naquele canal.
     */
    public Game getJogo(String channelId) {
        return jogosAtivos.get(channelId);
    }

    /**
     * Verifica se há um jogo em andamento em um canal específico.
     *
     * @param channelId O ID do canal a ser verificado.
     * @return {@code true} se houver um jogo ativo, {@code false} caso contrário.
     */
    public boolean isJogoAtivo(String channelId) {
        return jogosAtivos.containsKey(channelId);
    }
}