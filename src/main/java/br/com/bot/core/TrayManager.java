package br.com.bot.core;

import javax.swing.ImageIcon;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;

/**
 * Gerencia a presença do bot na bandeja do sistema (System Tray).
 * <p>
 * Esta classe encapsula toda a lógica AWT/Swing necessária para criar um ícone
 * na bandeja do sistema, com um menu de contexto para fechar a aplicação de forma
 * graciosa. Ela é desacoplada da lógica principal do bot, recebendo a ação de
 * desligamento através de um {@link Runnable}.
 *
 * @author Lucas
 */
public class TrayManager {

    /** A instância do ícone da bandeja, para que possa ser atualizado ou removido. */
    private TrayIcon trayIcon;

    /** A ação a ser executada quando o usuário clica em "Fechar Bot". */
    private final Runnable shutdownHook;

    /**
     * Constrói o gerenciador da bandeja do sistema.
     *
     * @param shutdownHook Uma ação {@link Runnable} que contém a lógica para desligar
     * o bot de forma segura (ex: chamar jda.shutdown()).
     */
    public TrayManager(Runnable shutdownHook) {
        this.shutdownHook = shutdownHook;
    }

    /**
     * Inicializa e adiciona o ícone à bandeja do sistema.
     * <p>
     * Este método verifica se a bandeja é suportada, cria o menu de popup com a opção
     * de fechar, carrega o ícone de {@code /icon.png} dos recursos e o exibe na bandeja
     * com uma mensagem de status inicial.
     */
    public void init() {
        if (!SystemTray.isSupported()) {
            System.out.println("Bandeja do sistema (System Tray) não é suportada.");
            return;
        }

        PopupMenu popup = new PopupMenu();
        MenuItem exitItem = new MenuItem("Fechar Bot");

        // Define a ação do botão "Fechar" para executar o shutdownHook que recebemos
        exitItem.addActionListener(e -> {
            shutdownHook.run(); // Executa a lógica de desligamento (ex: jda.shutdown())
            SystemTray.getSystemTray().remove(trayIcon); // Remove o ícone da bandeja
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

    /**
     * Atualiza a dica de texto (tooltip) que aparece ao passar o mouse sobre o ícone na bandeja.
     * <p>
     * Este método é seguro para ser chamado de qualquer thread (thread-safe), pois ele agenda a atualização
     * da interface gráfica para a Event Dispatch Thread (EDT) do Swing, evitando problemas de concorrência.
     *
     * @param tooltip O novo texto a ser exibido.
     */
    public void updateTooltip(String tooltip) {
        if (trayIcon != null) {
            javax.swing.SwingUtilities.invokeLater(() -> trayIcon.setToolTip(tooltip));
        }
    }
}