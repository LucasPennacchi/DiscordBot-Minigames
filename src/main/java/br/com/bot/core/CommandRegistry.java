package br.com.bot.core;

import br.com.bot.games.forca.ForcaCommand;
import br.com.bot.games.memoria.MemoriaCommand;
import br.com.bot.games.reflexo.ReflexosCommand;
import br.com.bot.games.resposta.RespostaCommand;
import br.com.bot.shared.ICommand;
import br.com.bot.utils.CancelarCommand;
import br.com.bot.utils.ListServersCommand;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Classe responsável por criar e registrar todos os comandos da aplicação.
 * Segue o padrão Factory, centralizando a lógica de criação de comandos.
 */
public class CommandRegistry {

    public static Map<String, ICommand> createCommands(GameManager gameManager, ScheduledExecutorService scheduler) {
        Map<String, ICommand> commandMap = new ConcurrentHashMap<>();

        // Comandos de Jogo
        commandMap.put("reflexos", new ReflexosCommand(gameManager, scheduler));
        commandMap.put("resposta", new RespostaCommand(gameManager, scheduler));
        commandMap.put("memoria", new MemoriaCommand(gameManager, scheduler));
        commandMap.put("forca", new ForcaCommand(gameManager, scheduler));
        // Adicione futuros jogos aqui...

        // Comandos de Utilidade
        commandMap.put("cancelar", new CancelarCommand(gameManager));
        commandMap.put("servidores", new ListServersCommand());

        return commandMap;
    }
}