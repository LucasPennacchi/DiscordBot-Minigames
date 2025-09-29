package br.com.bot.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gerencia as configurações personalizadas de cada servidor,
 * salvando e carregando de um arquivo JSON.
 */
public class ConfigManager {
    private static final String CONFIG_FILE = "server_configs.json";
    private final Map<String, ServerConfig> serverConfigs;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public ConfigManager() {
        this.serverConfigs = loadConfigs();
    }

    private Map<String, ServerConfig> loadConfigs() {
        try (FileReader reader = new FileReader(CONFIG_FILE)) {
            Type type = new TypeToken<ConcurrentHashMap<String, ServerConfig>>(){}.getType();
            Map<String, ServerConfig> configs = gson.fromJson(reader, type);
            // Se o arquivo estiver vazio, retorna um novo mapa
            return configs == null ? new ConcurrentHashMap<>() : new ConcurrentHashMap<>(configs);
        } catch (IOException e) {
            // Se o arquivo não existe, retorna um mapa vazio. Ele será criado ao salvar.
            System.out.println("Arquivo de configuração de servidores não encontrado. Criando um novo.");
            return new ConcurrentHashMap<>();
        }
    }

    // NOVO: Método para definir o tempo máximo de jogo
    public void setMaxGameTime(String guildId, long seconds) {
        getConfig(guildId).setMaxGameTimeSeconds(seconds);
        saveConfigs();
    }

    // NOVO: Método para bloquear um canal
    public void blockChannel(String guildId, String channelId) {
        getConfig(guildId).blockChannel(channelId);
        saveConfigs();
    }

    // NOVO: Método para desbloquear um canal
    public void unblockChannel(String guildId, String channelId) {
        getConfig(guildId).unblockChannel(channelId);
        saveConfigs();
    }

    private synchronized void saveConfigs() {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            gson.toJson(serverConfigs, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ServerConfig getConfig(String guildId) {
        // Se um servidor ainda não tem configuração, cria uma padrão para ele.
        return serverConfigs.computeIfAbsent(guildId, id -> new ServerConfig());
    }

    public void setAllowCreatorToPlay(String guildId, boolean allow) {
        getConfig(guildId).setAllowCreatorToPlay(allow);
        saveConfigs();
    }
}