package br.com.bot.core;

import net.dv8tion.jda.api.JDA;

import javax.swing.ImageIcon;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;

public class TrayManager {

    private TrayIcon trayIcon;

    public void init(JDA jda) {
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
                // O shutdown() pode demorar, então fazemos em uma nova thread para não travar a UI
                new Thread(jda::shutdown).start();
            }
            // Remove o ícone da bandeja
            SystemTray.getSystemTray().remove(trayIcon);
            // Encerra a aplicação
            System.exit(0);
        });

        popup.add(exitItem);

        // Carrega a imagem do ícone da pasta 'resources'
        java.net.URL imageUrl = getClass().getResource("/icon.png");
        if (imageUrl == null) {
            System.err.println("Arquivo de ícone 'icon.png' não encontrado na pasta resources!");
            // Se não encontrar o ícone, o bot ainda funcionará, mas sem o ícone na bandeja.
            return;
        }

        // Cria o ícone da bandeja com a mensagem inicial
        trayIcon = new TrayIcon(new ImageIcon(imageUrl).getImage(), "Bot de Jogos (Iniciando...)");
        trayIcon.setPopupMenu(popup);
        trayIcon.setImageAutoSize(true);

        try {
            SystemTray.getSystemTray().add(trayIcon);
            System.out.println("Ícone da bandeja adicionado com sucesso.");
        } catch (Exception e) {
            System.err.println("Não foi possível adicionar o ícone à bandeja do sistema.");
        }
    }

    /**
     * Atualiza a dica de texto que aparece ao passar o mouse sobre o ícone.
     * @param tooltip O novo texto a ser exibido.
     */
    public void updateTooltip(String tooltip) {
        if (trayIcon != null) {
            // Garante que a atualização da UI aconteça na thread de eventos gráficos
            javax.swing.SwingUtilities.invokeLater(() -> trayIcon.setToolTip(tooltip));
        }
    }
}