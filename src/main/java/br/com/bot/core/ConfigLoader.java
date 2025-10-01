package br.com.bot.core;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Classe responsável por carregar as configurações do arquivo {@code config.properties}.
 * <p>
 * Esta classe abstrai o acesso ao arquivo de propriedades, carregando os valores na memória
 * durante sua inicialização e fornecendo métodos de acesso convenientes para o resto da aplicação.
 * A aplicação será encerrada se o arquivo de configuração não for encontrado ou não puder ser lido.
 *
 * @author Lucas
 */
public class ConfigLoader {

    /** Armazena as propriedades carregadas do arquivo config.properties. */
    private final Properties properties = new Properties();

    /**
     * Constrói uma nova instância de ConfigLoader e carrega imediatamente as propriedades
     * do arquivo {@code /config.properties} localizado na pasta de recursos.
     * <p>
     * Em caso de falha na leitura do arquivo (ex: arquivo não encontrado), um erro
     * será impresso no console e a aplicação será encerrada, pois as configurações
     * são essenciais para a execução do bot.
     */
    public ConfigLoader() {
        // Tenta carregar o arquivo config.properties da pasta resources
        try (InputStream input = new FileInputStream("config.properties")) {
            properties.load(input);
        } catch (IOException ex) {
            System.err.println("ERRO FATAL: Não foi possível encontrar ou ler o arquivo 'config.properties' na pasta de execução.");
            System.err.println("Certifique-se de que o arquivo config.properties está na mesma pasta que o arquivo .jar.");
            System.exit(1);
        }
    }

    /**
     * Retorna o token do bot lido do arquivo de configuração.
     *
     * @return O valor da propriedade 'BOT_TOKEN'.
     */
    public String getToken() {
        return properties.getProperty("BOT_TOKEN");
    }

    /**
     * Retorna o ID do servidor (guild) de teste lido do arquivo de configuração.
     *
     * @return O valor da propriedade 'TEST_GUILD_ID'.
     */
    public String getTestGuildId() {
        return properties.getProperty("TEST_GUILD_ID");
    }

    /**
     * Verifica se o modo de desenvolvimento está ativado, com base na configuração.
     *
     * @return {@code true} se a propriedade 'DEV_MODE' for igual a "true" (ignorando maiúsculas/minúsculas),
     * {@code false} em todos os outros casos.
     */
    public boolean isDevMode() {
        return Boolean.parseBoolean(properties.getProperty("DEV_MODE"));
    }
}