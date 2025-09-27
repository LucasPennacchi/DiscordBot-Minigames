package br.com.bot.core;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;

// Imports necessários para a interface gráfica (AWT)
import javax.swing.ImageIcon;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionListener;

public class BotMain {

    // Guarda a instância do JDA para que o botão de fechar possa acessá-la
    private static JDA jda;
    private static TrayIcon trayIcon;

    public static void main(String[] args) {
        // Inicia a configuração do bot numa nova thread para não travar a interface
        new Thread(() -> {
            try {
                String token = "MTQyMTQ5NDU2MTAyMDE4NjYyNA.GlF7zH.pEWmG7-daKyzKxr2CGc5zvdZXdfZvs8AaUH0wc";
                GameManager gameManager = new GameManager();

                jda = JDABuilder.createDefault(token)
                        .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                        .addEventListeners(new GameCommands(gameManager))
                        .setActivity(Activity.customStatus("Nadando na lagoa 🦆"))
                        .build();

                jda.awaitReady();

                // Registro de comandos (exemplo com comandos globais)
                jda.updateCommands().addCommands(
                        Commands.slash("reflexos", "Inicia um teste de reflexo no canal.")
                                .addOption(OptionType.STRING, "tempo", "O tempo limite em segundos (ex: 2.5 ou 2,5).", true)
                                .addOption(OptionType.STRING, "frase", "A frase a ser digitada.", true),
                        Commands.slash("resposta", "Inicia um jogo de pergunta e resposta.")
                                .addOption(OptionType.STRING, "tempo", "O tempo para responder em segundos (ex: 30).", true)
                                .addOption(OptionType.STRING, "pergunta", "A pergunta a ser exibida.", true)
                                .addOption(OptionType.STRING, "resposta", "A resposta correta esperada.", true),
                        Commands.slash("memoria", "Inicia um jogo de memória.")
                                .addOption(OptionType.STRING, "tempo_ocultar", "Tempo para memorizar a string (ex: 5).", true)
                                .addOption(OptionType.STRING, "tempo", "Tempo para responder após a string sumir (ex: 10).", true)
                                .addOption(OptionType.STRING, "frase", "A string a ser memorizada.", true)
                ).queue();

                System.out.println("Bot está online e pronto!");

                // Atualiza a dica do ícone quando o bot estiver online
                if (trayIcon != null) {
                    trayIcon.setToolTip("Bot de Jogos (Online)");
                }

            } catch (InterruptedException e) {
                System.err.println("A inicialização do bot foi interrompida.");
                System.exit(1); // Sai se houver erro
            }
        }).start();

        // A thread principal fica responsável por criar e manter o ícone na bandeja
        javax.swing.SwingUtilities.invokeLater(BotMain::createTrayIcon);
    }

    private static void createTrayIcon() {
        // Verifica se o sistema suporta a bandeja
        if (!SystemTray.isSupported()) {
            System.out.println("Bandeja do sistema (System Tray) não é suportada.");
            return;
        }

        // Cria o menu popup que aparecerá ao clicar com o botão direito
        PopupMenu popup = new PopupMenu();
        MenuItem exitItem = new MenuItem("Fechar Bot");

        // Define a ação do botão "Fechar"
        exitItem.addActionListener(e -> {
            // Desliga o bot de forma graciosa
            if (jda != null) {
                jda.shutdown();
            }
            // Remove o ícone da bandeja
            SystemTray.getSystemTray().remove(trayIcon);
            // Encerra a aplicação
            System.exit(0);
        });

        popup.add(exitItem);

        // Carrega a imagem do ícone da pasta 'resources'
        java.net.URL imageUrl = BotMain.class.getResource("/icon.png");
        if (imageUrl == null) {
            System.err.println("Arquivo de ícone 'icon.png' não encontrado na pasta resources!");
            // Se não encontrar o ícone, o bot ainda funcionará, mas sem o ícone na bandeja.
            return;
        }

        // Cria o ícone da bandeja
        trayIcon = new TrayIcon(new ImageIcon(imageUrl).getImage(), "Bot de Jogos (Iniciando...)");
        trayIcon.setPopupMenu(popup);
        trayIcon.setImageAutoSize(true);

        try {
            SystemTray.getSystemTray().add(trayIcon);
        } catch (Exception e) {
            System.err.println("Não foi possível adicionar o ícone à bandeja do sistema.");
        }
    }
}