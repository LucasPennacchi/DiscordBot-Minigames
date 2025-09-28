package br.com.bot.core;

import br.com.bot.games.embaralhar.EmbaralharCommand;
import br.com.bot.games.memoria.MemoriaCommand;
import br.com.bot.games.reflexo.ReflexosCommand;
import br.com.bot.games.resposta.RespostaCommand;
import br.com.bot.shared.ICommand;
import br.com.bot.utils.CancelarCommand;
import br.com.bot.utils.ListServersCommand;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;


public class BotMain {

    private static JDA jda;
    private static TrayManager trayManager;

    public static void main(String[] args) {

        ConfigLoader config = new ConfigLoader();

        // Define a ação de desligamento que será usada pelo ícone da bandeja.
        Runnable shutdownHook = () -> {
            if (jda != null) {
                System.out.println("Desligando o bot...");
                jda.shutdown();
            }
        };
        trayManager = new TrayManager(shutdownHook);
        javax.swing.SwingUtilities.invokeLater(trayManager::init);

        // Inicia a lógica principal do bot em uma thread separada.
        new Thread(() -> conectarEConfigurarBot(config)).start();
    }

    private static void conectarEConfigurarBot(ConfigLoader config) {
        try {
            // --- INÍCIO DA INJEÇÃO DE DEPENDÊNCIA ---

            // 1. Crie os "serviços" compartilhados.
            GameManager gameManager = new GameManager();
            ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

            // 2. Monte o mapa de comandos, criando cada objeto de comando aqui.
            Map<String, ICommand> commandMap = new ConcurrentHashMap<>();
            // Comandos de Jogo
            commandMap.put("reflexos", new ReflexosCommand(gameManager, scheduler));
            commandMap.put("resposta", new RespostaCommand(gameManager, scheduler));
            commandMap.put("memoria", new MemoriaCommand(gameManager, scheduler));
            commandMap.put("embaralhar", new EmbaralharCommand(gameManager, scheduler));
            // Adicione futuros jogos aqui...

            // Comandos de Utilidade
            commandMap.put("cancelar", new CancelarCommand(gameManager));
            commandMap.put("servidores", new ListServersCommand());

            // 3. "Injete" as dependências no GameCommands.
            GameCommands gameCommandsManager = new GameCommands(gameManager, commandMap);

            // --- FIM DA INJEÇÃO DE DEPENDÊNCIA ---


            // 4. Construa a instância do JDA usando as dependências e configurações.
            jda = JDABuilder.createDefault(config.getToken())
                    .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                    .addEventListeners(gameCommandsManager)
                    .setActivity(Activity.customStatus("Gerenciando Jogos 🦆"))
                    .build();

            // Espera o bot ficar totalmente online.
            jda.awaitReady();

            // Atualiza a dica de texto do ícone da bandeja para "Online".
            trayManager.updateTooltip("Bot de Jogos (Online)");

            // --- LÓGICA DE REGISTRO DE COMANDOS ---

            List<ICommand> allCommands = new ArrayList<>(gameCommandsManager.getCommands().values());

            if (config.isDevMode()) {
                // MODO DE DESENVOLVIMENTO: Registra TODOS os comandos instantaneamente no servidor de teste.
                Guild guild = jda.getGuildById(config.getTestGuildId());
                if (guild != null) {
                    List<SlashCommandData> allCommandDataForGuild = allCommands.stream()
                            .map(ICommand::getCommandData)
                            .collect(Collectors.toList());
                    guild.updateCommands().addCommands(allCommandDataForGuild).queue();
                    System.out.println("Todos os comandos registrados em modo de DESENVOLVIMENTO no servidor: " + guild.getName());
                } else {
                    System.err.println("Servidor de teste não encontrado! Verifique o ID em config.properties.");
                }
            } else {
                // MODO DE PRODUÇÃO: Registra apenas os comandos PÚBLICOS globalmente.
                List<SlashCommandData> publicCommands = allCommands.stream()
                        .filter(c -> !(c instanceof ListServersCommand)) // Filtra para não incluir os comandos de admin
                        .map(ICommand::getCommandData)
                        .collect(Collectors.toList());
                jda.updateCommands().addCommands(publicCommands).queue();
                System.out.println("Apenas os comandos públicos foram registrados em modo de PRODUÇÃO (globalmente).");
            }

            System.out.println("Bot está online e pronto!");

        } catch (InterruptedException e) {
            System.err.println("A inicialização do bot foi interrompida.");
            // Garante que a aplicação feche se a inicialização falhar
            System.exit(1);
        }
    }
}