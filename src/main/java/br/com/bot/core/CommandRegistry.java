package br.com.bot.core;

import br.com.bot.config.command.ConfigCanalBloqueadoCommand;
import br.com.bot.config.command.ConfigTempoMaximoCommand;
import br.com.bot.games.forca.ForcaCommand;
import br.com.bot.games.memoria.MemoriaCommand;
import br.com.bot.games.reflexo.ReflexoCommand;
import br.com.bot.games.resposta.RespostaCommand;
import br.com.bot.shared.ICommand;
import br.com.bot.utils.command.CancelarCommand;
import br.com.bot.config.command.ConfigAutorespostaCommand;
import br.com.bot.utils.command.ListServersCommand;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Classe responsável por criar e registrar todos os comandos da aplicação.
 * Segue o padrão Factory, centralizando a lógica de criação de comandos.
 */
public class CommandRegistry {

    public static Map<String, ICommand> createCommands(GameManager gameManager, ConfigManager configManager, ScheduledExecutorService scheduler) {
        Map<String, ICommand> commandMap = new ConcurrentHashMap<>();

        // --- Comandos de Jogo ---
        commandMap.put("reflexos", new ReflexoCommand(gameManager, configManager, scheduler));
        commandMap.put("resposta", new RespostaCommand(gameManager, configManager, scheduler));
        commandMap.put("memoria", new MemoriaCommand(gameManager, configManager, scheduler));
        commandMap.put("forca", new ForcaCommand(gameManager, configManager, scheduler));
        // Adicione futuros jogos aqui...

        // --- Comandos de Configuração ---
        commandMap.put("config-autoresposta", new ConfigAutorespostaCommand(configManager));
        commandMap.put("config-tempomaximo", new ConfigTempoMaximoCommand(configManager));
        commandMap.put("config-canalbloqueado", new ConfigCanalBloqueadoCommand(configManager));
        // Adicione futuros comandos de config aqui...

        // --- Comandos de Utilidade/Admin ---
        commandMap.put("cancelar", new CancelarCommand(gameManager));
        commandMap.put("servidores", new ListServersCommand());

        return commandMap;
    }
}