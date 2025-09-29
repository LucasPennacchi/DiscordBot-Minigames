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
 * Gerencia as configurações personalizadas de cada servidor, salvando e carregando de um arquivo JSON.
 * <p>
 * Esta classe atua como a única fonte de verdade para as configurações por servidor,
 * abstraindo a lógica de leitura e escrita do disco (persistência). Ela mantém um
 * cache em memória das configurações para acesso rápido e cria configurações padrão
 * para novos servidores sob demanda.
 *
 * @author Lucas
 */
public class ConfigManager {
    /** O nome do arquivo JSON onde as configurações são persistidas. */
    private static final String CONFIG_FILE = "server_configs.json";

    /** O cache em memória das configurações, mapeando ID do Servidor para seu objeto de configuração. */
    private final Map<String, ServerConfig> serverConfigs;

    /** Instância da biblioteca Gson para serialização e desserialização de JSON. */
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    /**
     * Constrói uma nova instância do ConfigManager.
     * Ao ser criado, ele imediatamente tenta carregar as configurações existentes do disco.
     */
    public ConfigManager() {
        this.serverConfigs = loadConfigs();
    }

    /**
     * Carrega as configurações do arquivo {@value #CONFIG_FILE} para a memória.
     * Se o arquivo não existir, um novo mapa vazio é criado e o programa continua normalmente.
     *
     * @return Um {@link Map} contendo as configurações de todos os servidores.
     */
    private Map<String, ServerConfig> loadConfigs() {
        try (FileReader reader = new FileReader(CONFIG_FILE)) {
            Type type = new TypeToken<ConcurrentHashMap<String, ServerConfig>>(){}.getType();
            Map<String, ServerConfig> configs = gson.fromJson(reader, type);
            return configs == null ? new ConcurrentHashMap<>() : new ConcurrentHashMap<>(configs);
        } catch (IOException e) {
            System.out.println("Arquivo de configuração de servidores não encontrado. Um novo será criado na primeira alteração.");
            return new ConcurrentHashMap<>();
        }
    }

    /**
     * Salva o estado atual do mapa de configurações em memória para o arquivo {@value #CONFIG_FILE}.
     * Este método é sincronizado para evitar problemas de concorrência ao escrever no arquivo.
     */
    private synchronized void saveConfigs() {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            gson.toJson(serverConfigs, writer);
        } catch (IOException e) {
            System.err.println("ERRO: Falha ao salvar as configurações dos servidores.");
            e.printStackTrace();
        }
    }

    /**
     * Obtém o objeto de configuração para um servidor específico.
     * <p>
     * Se o servidor ainda não tiver uma configuração salva, uma nova configuração
     * padrão será criada, armazenada em memória e retornada.
     *
     * @param guildId O ID do servidor (Guild) cuja configuração é desejada.
     * @return O objeto {@link ServerConfig} para o servidor especificado (nunca nulo).
     */
    public ServerConfig getConfig(String guildId) {
        return serverConfigs.computeIfAbsent(guildId, id -> new ServerConfig());
    }

    /**
     * Define a permissão para o criador de um jogo poder responder naquele servidor.
     *
     * @param guildId O ID do servidor a ser configurado.
     * @param allow {@code true} para permitir, {@code false} para proibir.
     */
    public void setAllowCreatorToPlay(String guildId, boolean allow) {
        getConfig(guildId).setAllowCreatorToPlay(allow);
        saveConfigs();
    }

    /**
     * Define o tempo máximo em segundos que um jogo pode durar em um servidor.
     *
     * @param guildId O ID do servidor a ser configurado.
     * @param seconds O tempo máximo em segundos. Um valor <= 0 desativa o limite.
     */
    public void setMaxGameTime(String guildId, long seconds) {
        getConfig(guildId).setMaxGameTimeSeconds(seconds);
        saveConfigs();
    }

    /**
     * Adiciona um canal à lista de canais bloqueados para jogos em um servidor.
     *
     * @param guildId O ID do servidor.
     * @param channelId O ID do canal a ser bloqueado.
     */
    public void blockChannel(String guildId, String channelId) {
        getConfig(guildId).blockChannel(channelId);
        saveConfigs();
    }

    /**
     * Remove um canal da lista de canais bloqueados para jogos em um servidor.
     *
     * @param guildId O ID do servidor.
     * @param channelId O ID do canal a ser desbloqueado.
     */
    public void unblockChannel(String guildId, String channelId) {
        getConfig(guildId).unblockChannel(channelId);
        saveConfigs();
    }
}