package br.com.bot.utils;

import br.com.bot.core.ConfigLoader;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;

/**
 * Uma classe de utilidade para limpar e remover todos os comandos de barra do bot no Discord.
 * <p>
 * Esta é uma aplicação standalone que deve ser executada manualmente quando necessário
 * para resolver problemas de comandos duplicados ou dessincronizados. Ela lê as
 * configurações (token e ID do servidor de teste) do arquivo {@code config.properties},
 * conecta-se ao Discord, envia requisições para apagar os comandos e se desconecta.
 *
 * @author Lucas
 */
public class CommandCleaner {

    /**
     * O ponto de entrada para a ferramenta de limpeza de comandos.
     * <p>
     * O método executa as seguintes ações:
     * <ol>
     * <li>Carrega as configurações do bot a partir de {@code config.properties}.</li>
     * <li>Valida se o token foi preenchido.</li>
     * <li>Conecta-se ao Discord usando o token fornecido.</li>
     * <li>Envia uma requisição para apagar todos os comandos globais.</li>
     * <li>Envia uma requisição para apagar todos os comandos do servidor de teste (se configurado).</li>
     * <li>Desliga a conexão de forma graciosa.</li>
     * </ol>
     *
     * @param args Argumentos de linha de comando (não utilizados por esta aplicação).
     */
    public static void main(String[] args) {
        System.out.println("### FERRAMENTA DE LIMPEZA DE COMANDOS ###");

        // 1. Carrega a configuração do arquivo, assim como o BotMain
        ConfigLoader config = new ConfigLoader();
        String token = config.getToken();
        String testGuildId = config.getTestGuildId();

        // 2. Valida a configuração carregada
        if (token == null || token.isEmpty() || token.equals("SEU_TOKEN_SECRETO_AQUI")) {
            System.err.println("ERRO: O token do bot não está configurado corretamente em config.properties.");
            return;
        }

        try {
            System.out.println("Conectando ao Discord...");
            JDA jda = JDABuilder.createDefault(token).build();
            jda.awaitReady(); // Espera a conexão ser estabelecida

            // 3. Limpa os comandos GLOBAIS
            System.out.println("Limpando comandos globais...");
            jda.updateCommands().addCommands().queue();
            System.out.println("-> Comandos globais enviados para limpeza.");

            // 4. Limpa os comandos do SERVIDOR DE TESTE (GUILD)
            if (testGuildId != null && !testGuildId.isEmpty() && !testGuildId.equals("SEU_ID_DO_SERVIDOR_AQUI")) {
                Guild testGuild = jda.getGuildById(testGuildId);
                if (testGuild != null) {
                    System.out.println("Limpando comandos no servidor de teste: " + testGuild.getName());
                    testGuild.updateCommands().addCommands().queue();
                    System.out.println("-> Comandos do servidor de teste enviados para limpeza.");
                } else {
                    System.err.println("AVISO: Servidor de teste com o ID fornecido em config.properties não foi encontrado.");
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
        } catch (Exception e) {
            // Captura outros erros, como token inválido
            System.err.println("Ocorreu um erro ao tentar conectar: " + e.getMessage());
        }
    }
}