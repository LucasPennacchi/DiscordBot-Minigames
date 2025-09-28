package br.com.bot.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Classe responsável por carregar as configurações do arquivo config.properties.
 */
public class ConfigLoader {

    private final Properties properties = new Properties();

    public ConfigLoader() {
        // Tenta carregar o arquivo config.properties da pasta resources
        try (InputStream input = getClass().getResourceAsStream("/config.properties")) {
            if (input == null) {
                System.out.println("Desculpe, não foi possível encontrar o arquivo config.properties");
                throw new IOException("Arquivo de configuração não encontrado.");
            }
            // Carrega as propriedades do arquivo
            properties.load(input);
        } catch (IOException ex) {
            System.err.println("Erro ao carregar o arquivo de configuração.");
            ex.printStackTrace();
            // Encerra a aplicação se a configuração não puder ser lida.
            System.exit(1);
        }
    }

    public String getToken() {
        return properties.getProperty("BOT_TOKEN");
    }

    public String getTestGuildId() {
        return properties.getProperty("TEST_GUILD_ID");
    }

    public boolean isDevMode() {
        return Boolean.parseBoolean(properties.getProperty("DEV_MODE"));
    }
}