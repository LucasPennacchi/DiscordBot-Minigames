package br.com.bot.core;

import br.com.bot.shared.ICommand;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.util.ArrayList;
import java.util.List;

public class BotMain {

    private static final boolean MODO_DESENVOLVIMENTO = true;
    private static final String ID_SERVIDOR_TESTE = "1030911167952064633";

    public static void main(String[] args) {
        // Instancia o nosso novo gerenciador de bandeja
        TrayManager trayManager = new TrayManager();

        try {
            String token = "MTQyMTQ5NDU2MTAyMDE4NjYyNA.GlF7zH.pEWmG7-daKyzKxr2CGc5zvdZXdfZvs8AaUH0wc";
            GameManager gameManager = new GameManager();
            GameCommands gameCommands = new GameCommands(gameManager);

            JDA jda = JDABuilder.createDefault(token)
                    .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                    .addEventListeners(gameCommands)
                    .setActivity(Activity.customStatus("Nadando na lagoa 🦆"))
                    .build();

            // Inicia a criação do ícone da bandeja, passando a instância do JDA para o controle de fechar
            javax.swing.SwingUtilities.invokeLater(() -> trayManager.init(jda));

            jda.awaitReady();

            // Após o bot estar pronto, atualizamos a dica do ícone
            trayManager.updateTooltip("Bot de Jogos (Online)");

            // Lógica de registro de comandos
            List<SlashCommandData> commandDataList = new ArrayList<>();
            for (ICommand command : gameCommands.getCommands().values()) {
                commandDataList.add(command.getCommandData());
            }

            if (MODO_DESENVOLVIMENTO) {
                Guild guild = jda.getGuildById(ID_SERVIDOR_TESTE);
                if (guild != null) {
                    guild.updateCommands().addCommands(commandDataList).queue();
                    System.out.println("Comandos registrados em modo de DESENVOLVIMENTO!");
                }
            } else {
                jda.updateCommands().addCommands(commandDataList).queue();
                System.out.println("Comandos registrados em modo de PRODUÇÃO.");
            }

            System.out.println("Bot está online e pronto!");

        } catch (InterruptedException e) {
            System.err.println("A inicialização do bot foi interrompida.");
            System.exit(1);
        }
    }
}