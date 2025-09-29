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


public class BotMain {

    // --- VARIÁVEIS DE CONFIGURAÇÃO GLOBAIS ---
    // ALTERAÇÃO: Estas variáveis agora guardarão a configuração para todo o projeto.
    public static boolean IS_DEV_MODE;
    private static String TOKEN;
    public static String ID_SERVIDOR_TESTE;
    // ------------------------------------------

    private static JDA jda;
    private static TrayManager trayManager;

    public static void main(String[] args) {
        // Carrega as configurações do arquivo .properties
        ConfigLoader config = new ConfigLoader();

        // ALTERAÇÃO: Preenche nossas variáveis globais com os valores do arquivo
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

        // Inicia a lógica principal do bot em uma thread separada.
        // ALTERAÇÃO: Não precisa mais passar 'config' como parâmetro.
        new Thread(BotMain::conectarEConfigurarBot).start();
    }

    // ALTERAÇÃO: O método agora usa as variáveis estáticas da classe.
    private static void conectarEConfigurarBot() {
        try {
            // --- INÍCIO DA INJEÇÃO DE DEPENDÊNCIA ---
            ConfigLoader config = new ConfigLoader(); // Mantemos este para o token, etc.
            GameManager gameManager = new GameManager();
            ConfigManager configManager = new ConfigManager(); // NOVO
            ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

            Map<String, ICommand> commandMap = CommandRegistry.createCommands(gameManager, configManager, scheduler); // NOVO
            GameCommands gameCommandsManager = new GameCommands(gameManager, configManager, commandMap); // NOVO

            // --- FIM DA INJEÇÃO DE DEPENDÊNCIA ---


            // 4. Construa a instância do JDA usando as dependências e configurações.
            jda = JDABuilder.createDefault(TOKEN) // Usa a variável estática
                    .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                    .addEventListeners(gameCommandsManager)
                    .setActivity(Activity.customStatus("Gerenciando Jogos 🦆"))
                    .build();

            jda.awaitReady();
            trayManager.updateTooltip("Bot de Jogos (Online)");

            // --- LÓGICA DE REGISTRO E LIMPEZA AUTOMÁTICA DE COMANDOS ---

            List<ICommand> allCommands = new ArrayList<>(gameCommandsManager.getCommands().values());

            if (IS_DEV_MODE) {
                // ESTAMOS EM MODO DE DESENVOLVIMENTO
                Guild guild = jda.getGuildById(ID_SERVIDOR_TESTE);
                if (guild != null) {
                    System.out.println("Modo de Desenvolvimento Ativo.");

                    // 1. LIMPA o escopo global para evitar duplicatas de comandos antigos de produção.
                    System.out.println("Limpando comandos globais...");
                    jda.updateCommands().addCommands().queue();

                    // 2. REGISTRA todos os comandos apenas no servidor de teste.
                    List<SlashCommandData> allCommandDataForGuild = allCommands.stream()
                            .map(ICommand::getCommandData)
                            .collect(Collectors.toList());
                    guild.updateCommands().addCommands(allCommandDataForGuild).queue();
                    System.out.println("Todos os comandos registrados no servidor de teste: " + guild.getName());
                } else {
                    System.err.println("Servidor de teste não encontrado! Verifique o ID em config.properties.");
                }
            } else {
                // ESTAMOS EM MODO DE PRODUÇÃO
                System.out.println("Modo de Produção Ativo.");

                // 1. LIMPA os comandos do servidor de teste para evitar duplicatas.
                Guild guild = jda.getGuildById(ID_SERVIDOR_TESTE);
                if (guild != null) {
                    System.out.println("Limpando comandos do servidor de teste...");
                    guild.updateCommands().addCommands().queue();
                }

                // 2. REGISTRA apenas os comandos públicos globalmente.
                List<SlashCommandData> publicCommands = allCommands.stream()
                        .filter(c -> !(c instanceof ListServersCommand)) // Filtra para não incluir os comandos de admin
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