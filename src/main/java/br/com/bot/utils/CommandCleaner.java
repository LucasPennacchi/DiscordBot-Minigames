package br.com.bot.utils;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;

import javax.security.auth.login.LoginException;

/**
 * Uma classe de utilidade para limpar/remover todos os comandos de barra do bot no Discord.
 * Execute o metodo main() desta classe para realizar a limpeza.
 */
public class CommandCleaner {

    // --- CONFIGURE AQUI ANTES DE EXECUTAR ---
    private static final String TOKEN = "MTQyMTQ5NDU2MTAyMDE4NjYyNA.GlF7zH.pEWmG7-daKyzKxr2CGc5zvdZXdfZvs8AaUH0wc";
    private static final String ID_SERVIDOR_TESTE = "1421864528223797382";
    // -----------------------------------------

    public static void main(String[] args) {
        System.out.println("### FERRAMENTA DE LIMPEZA DE COMANDOS ###");

        if (TOKEN == null || TOKEN.equals("SEU_TOKEN_AQUI")) {
            System.err.println("ERRO: Por favor, configure o TOKEN do bot na classe CommandCleaner.java antes de executar.");
            return;
        }

        try {
            System.out.println("Conectando ao Discord...");
            JDA jda = JDABuilder.createDefault(TOKEN).build();
            jda.awaitReady(); // Espera a conexão ser estabelecida

            // 1. Limpa os comandos GLOBAIS
            System.out.println("Limpando comandos globais...");
            jda.updateCommands().addCommands().queue();
            System.out.println("-> Comandos globais enviados para limpeza.");

            // 2. Limpa os comandos do SERVIDOR DE TESTE (GUILD)
            if (ID_SERVIDOR_TESTE != null && !ID_SERVIDOR_TESTE.equals("SEU_ID_DO_SERVIDOR_AQUI")) {
                Guild testGuild = jda.getGuildById(ID_SERVIDOR_TESTE);
                if (testGuild != null) {
                    System.out.println("Limpando comandos no servidor de teste: " + testGuild.getName());
                    testGuild.updateCommands().addCommands().queue();
                    System.out.println("-> Comandos do servidor de teste enviados para limpeza.");
                } else {
                    System.err.println("AVISO: Servidor de teste com o ID fornecido não foi encontrado.");
                }
            } else {
                System.out.println("Nenhum ID de servidor de teste configurado, pulando limpeza de guild.");
            }

            System.out.println("\nSolicitações de limpeza enviadas. Pode levar alguns minutos para o Discord atualizar.");
            System.out.println("Desligando o bot de limpeza...");

            // Desliga o JDA de forma graciosa
            jda.shutdown();

        } catch (InterruptedException e) {
            System.err.println("A conexão do bot foi interrompida.");
            Thread.currentThread().interrupt();
        }
    }
}