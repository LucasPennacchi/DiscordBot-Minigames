package br.com.bot.core;

import br.com.bot.shared.ICommand;
import br.com.bot.utils.command.ListServersCommand;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

/**
 * A classe principal e ponto de entrada da aplicação do bot.
 * <p>
 * Orquestra o ciclo de vida completo do bot:
 * <ol>
 * <li>Carrega as configurações do arquivo {@code config.properties}.</li>
 * <li>Inicializa o ícone da bandeja do sistema (System Tray).</li>
 * <li>Inicia a lógica de conexão e configuração do bot em uma thread separada.</li>
 * <li>Atua como o "Injetor de Dependências" manual da aplicação.</li>
 * <li>Gerencia o registro de comandos de barra, alternando entre modo de desenvolvimento e produção.</li>
 * </ol>
 *
 * @author Lucas
 */
public class BotMain {

    // --- VARIÁVEIS DE CONFIGURAÇÃO GLOBAIS ---
    /** Indica se o bot está rodando em modo de desenvolvimento. Lido de config.properties. */
    public static boolean IS_DEV_MODE;
    /** O token secreto do bot. Lido de config.properties. */
    private static String TOKEN;
    /** O ID do servidor de teste. Lido de config.properties. */
    public static String ID_SERVIDOR_TESTE;
    // ------------------------------------------

    /** A instância principal do JDA, para ser acessível globalmente. */
    private static JDA jda;
    /** A instância do gerenciador do ícone da bandeja do sistema. */
    private static TrayManager trayManager;

    /**
     * O método principal que inicia a aplicação.
     * <p>
     * Carrega as configurações, prepara e exibe o ícone da bandeja do sistema e
     * dispara a thread de conexão do bot.
     *
     * @param args Argumentos de linha de comando (não utilizados).
     */
    public static void main(String[] args) {
        // Carrega as configurações do arquivo .properties
        ConfigLoader config = new ConfigLoader();

        // Preenche nossas variáveis globais com os valores do arquivo
        TOKEN = config.getToken();
        ID_SERVIDOR_TESTE = config.getTestGuildId();
        IS_DEV_MODE = config.isDevMode();

        // Define a ação de desligamento que será usada pelo ícone da bandeja.
        Runnable shutdownHook = () -> {
            if (jda != null) {
                System.out.println("Desligando o bot...");
                jda.shutdown();
            }
        };
        trayManager = new TrayManager(shutdownHook);
        javax.swing.SwingUtilities.invokeLater(trayManager::init);

        // Inicia a lógica principal do bot em uma thread separada para não travar a UI.
        new Thread(BotMain::conectarEConfigurarBot).start();
    }

    /**
     * Contém a lógica principal de conexão, montagem de dependências e registro de comandos.
     * Este método é executado em uma thread de segundo plano para não congelar a aplicação.
     */
    private static void conectarEConfigurarBot() {
        try {
            // --- INÍCIO DA INJEÇÃO DE DEPENDÊNCIA ---
            GameManager gameManager = new GameManager();
            ConfigManager configManager = new ConfigManager();
            ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

            Map<String, ICommand> commandMap = CommandRegistry.createCommands(gameManager, configManager, scheduler);
            GameCommands gameCommandsManager = new GameCommands(gameManager, configManager, commandMap);
            // --- FIM DA INJEÇÃO DE DEPENDÊNCIA ---


            // Constrói a instância do JDA usando as dependências e configurações.
            jda = JDABuilder.createDefault(TOKEN)
                    .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                    .addEventListeners(gameCommandsManager)
                    .setActivity(Activity.customStatus("Gerenciando Jogos 🦆"))
                    .build();

            jda.awaitReady();
            trayManager.updateTooltip("Bot de Jogos (Online)");

            // --- LÓGICA DE REGISTRO E LIMPEZA AUTOMÁTICA DE COMANDOS ---
            List<ICommand> allCommands = new ArrayList<>(gameCommandsManager.getCommands().values());

            if (IS_DEV_MODE) {
                Guild guild = jda.getGuildById(ID_SERVIDOR_TESTE);
                if (guild != null) {
                    System.out.println("Modo de Desenvolvimento Ativo.");
                    System.out.println("Limpando comandos globais...");
                    jda.updateCommands().addCommands().queue();

                    List<SlashCommandData> allCommandDataForGuild = allCommands.stream()
                            .map(ICommand::getCommandData)
                            .collect(Collectors.toList());
                    guild.updateCommands().addCommands(allCommandDataForGuild).queue();
                    System.out.println("Todos os comandos registrados no servidor de teste: " + guild.getName());
                } else {
                    System.err.println("Servidor de teste não encontrado! Verifique o ID em config.properties.");
                }
            } else {
                System.out.println("Modo de Produção Ativo.");
                Guild guild = jda.getGuildById(ID_SERVIDOR_TESTE);
                if (guild != null) {
                    System.out.println("Limpando comandos do servidor de teste...");
                    guild.updateCommands().addCommands().queue();
                }

                List<SlashCommandData> publicCommands = allCommands.stream()
                        .filter(c -> !(c instanceof ListServersCommand))
                        .map(ICommand::getCommandData)
                        .collect(Collectors.toList());
                jda.updateCommands().addCommands(publicCommands).queue();
                System.out.println("Apenas os comandos públicos foram registrados globalmente.");
            }

            System.out.println("Bot está online e pronto!");

        } catch (InterruptedException e) {
            System.err.println("A inicialização do bot foi interrompida.");
            System.exit(1);
        }
    }
}