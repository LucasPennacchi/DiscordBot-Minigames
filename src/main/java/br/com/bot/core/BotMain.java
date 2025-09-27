package br.com.bot.core;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class BotMain {
    public static void main(String[] args) throws InterruptedException {
        // Cole o token do seu bot aqui
        String token = "MTQyMTQ5NDU2MTAyMDE4NjYyNA.GlF7zH.pEWmG7-daKyzKxr2CGc5zvdZXdfZvs8AaUH0wc";

        GameManager gameManager = new GameManager();

        JDA jda = JDABuilder.createDefault(token)
                .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                .addEventListeners(new GameCommands(gameManager))
                .setActivity(Activity.customStatus("Nadando na lagoa"))
                .build();

        jda.awaitReady();

        // Comando do jogo de reflexo
        jda.upsertCommand("reflexos", "Inicia um teste de reflexo no canal.")
                .addOption(OptionType.STRING, "timer", "O tempo limite em segundos (ex: 2.5 ou 2,5).", true)
                .addOption(OptionType.STRING, "frase", "A frase a ser digitada.", true)
                .queue();

        // Comando do jogo de resposta
        jda.upsertCommand("resposta", "Inicia um jogo de pergunta e resposta.")
                .addOption(OptionType.STRING, "timer", "O tempo para responder em segundos (ex: 30).", true)
                .addOption(OptionType.STRING, "pergunta", "A pergunta a ser exibida.", true)
                .addOption(OptionType.STRING, "resposta", "A resposta correta esperada.", true)
                .queue();

        // Comando do jogo de memória
        jda.upsertCommand("memoria", "Inicia um jogo de memória.")
                .addOption(OptionType.STRING, "tempo para ocultar", "Tempo para memorizar a string (ex: 5).", true)
                .addOption(OptionType.STRING, "timer", "Tempo para responder após a string sumir (ex: 2.5 ou 2,5).", true)
                .addOption(OptionType.STRING, "frase", "A frase a ser memorizada.", true)
                .queue();

        System.out.println("Bot está online e pronto!");
    }
}