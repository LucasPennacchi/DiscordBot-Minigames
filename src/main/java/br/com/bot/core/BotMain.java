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

    private static final boolean MODO_DESENVOLVIMENTO = false;
    private static final String ID_SERVIDOR_TESTE = "1030911167952064633";

    public static void main(String[] args) throws InterruptedException {
        String token = "MTQyMTQ5NDU2MTAyMDE4NjYyNA.GlF7zH.pEWmG7-daKyzKxr2CGc5zvdZXdfZvs8AaUH0wc";

        GameManager gameManager = new GameManager();
        // Instancia o GameCommands antes para podermos pegar a lista de comandos
        GameCommands gameCommands = new GameCommands(gameManager);

        JDA jda = JDABuilder.createDefault(token)
                .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                .addEventListeners(gameCommands) // Passa a instância já criada
                .setActivity(Activity.customStatus("Nadando na lagoa 🦆"))
                .build();

        jda.awaitReady();

        // --- LÓGICA DE REGISTRO DE COMANDOS CENTRALIZADA ---

        // 1. Pega as definições de todos os comandos registrados em GameCommands
        List<SlashCommandData> commandDataList = new ArrayList<>();
        for (ICommand command : gameCommands.getCommands().values()) {
            commandDataList.add(command.getCommandData());
        }

        // 2. Registra os comandos (em modo de teste ou produção)
        if (MODO_DESENVOLVIMENTO) {
            Guild guild = jda.getGuildById(ID_SERVIDOR_TESTE);
            if (guild != null) {
                guild.updateCommands().addCommands(commandDataList).queue();
                System.out.println("Comandos registrados em modo de DESENVOLVIMENTO!");
            }
        } else {
            jda.updateCommands().addCommands(commandDataList).queue();
            System.out.println("Comandos registrados em modo de PRODUÇÃO (globalmente).");
        }

        System.out.println("Bot está online e pronto!");
    }
}