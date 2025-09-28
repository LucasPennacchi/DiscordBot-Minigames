package br.com.bot.core;

import javax.swing.ImageIcon;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;

public class TrayManager {

    private TrayIcon trayIcon;
    private final Runnable shutdownHook;

    /**
     * O construtor recebe a ação que deve ser executada ao fechar.
     * @param shutdownHook Uma ação (Runnable) para desligar o bot.
     */
    public TrayManager(Runnable shutdownHook) {
        this.shutdownHook = shutdownHook;
    }

    public void init() {
        if (!SystemTray.isSupported()) {
            System.out.println("Bandeja do sistema (System Tray) não é suportada.");
            return;
        }

        PopupMenu popup = new PopupMenu();
        MenuItem exitItem = new MenuItem("Fechar Bot");

        // Define a ação do botão "Fechar" para executar o shutdownHook que recebemos
        exitItem.addActionListener(e -> {
            shutdownHook.run(); // Executa a lógica de desligamento
            SystemTray.getSystemTray().remove(trayIcon); // Remove o ícone
            System.exit(0); // Encerra a aplicação
        });

        popup.add(exitItem);

        java.net.URL imageUrl = getClass().getResource("/icon.png");
        if (imageUrl == null) {
            System.err.println("Arquivo de ícone 'icon.png' não encontrado na pasta resources!");
            return;
        }

        trayIcon = new TrayIcon(new ImageIcon(imageUrl).getImage(), "Bot de Jogos (Iniciando...)");
        trayIcon.setPopupMenu(popup);
        trayIcon.setImageAutoSize(true);

        try {
            SystemTray.getSystemTray().add(trayIcon);
            System.out.println("Ícone da bandeja adicionado. O bot está iniciando em segundo plano...");
        } catch (Exception e) {
            System.err.println("Não foi possível adicionar o ícone à bandeja do sistema.");
        }
    }

    public void updateTooltip(String tooltip) {
        if (trayIcon != null) {
            javax.swing.SwingUtilities.invokeLater(() -> trayIcon.setToolTip(tooltip));
        }
    }
}