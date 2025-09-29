package br.com.bot.games.memoria;

import br.com.bot.core.ConfigManager;
import br.com.bot.core.GameManager;
import br.com.bot.shared.Game;
import br.com.bot.shared.ICommand;
import br.com.bot.utils.ValidationUtils;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Comando responsável por iniciar e gerenciar a lógica do Jogo da Memória.
 * Possui um fluxo de execução especial com dois timers (ocultar e limite),
 * implementando {@link ICommand} diretamente.
 * @author Lucas
 */
public class MemoriaCommand implements ICommand {
    private final GameManager gameManager;
    private final ConfigManager configManager;
    private final ScheduledExecutorService scheduler;
    private static final int PREPARE_DELAY_SECONDS = 3;

    /**
     * Constrói o comando da Memória com suas dependências necessárias.
     * @param gameManager O gerenciador de jogos ativos.
     * @param configManager O gerenciador de configurações de servidor.
     * @param scheduler O agendador de tarefas para os timers.
     */
    public MemoriaCommand(GameManager gameManager, ConfigManager configManager, ScheduledExecutorService scheduler) {
        this.gameManager = gameManager;
        this.configManager = configManager;
        this.scheduler = scheduler;
    }

    /**
     * Converte uma String de tempo (aceitando ',' ou '.') para um valor long em milissegundos.
     * @param input A String fornecida pelo usuário.
     * @return O tempo em milissegundos.
     * @throws NumberFormatException Se a String não for um número válido.
     */
    private long parseTimeInput(String input) throws NumberFormatException {
        return (long) (Double.parseDouble(input.replace(',', '.')) * 1000);
    }

    /**
     * {@inheritDoc}
     * Executa a lógica do comando /memoria, orquestrando o agendamento
     * de mostrar, ocultar e finalizar o jogo.
     */
    @Override
    public void execute(SlashCommandInteractionEvent event) {
        // ... (código interno do método)
    }

    /**
     * {@inheritDoc}
     * Cria e retorna a definição do comando /memoria para o Discord.
     * @return A definição do comando de barra.
     */
    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash("memoria", "Inicia um jogo de memória.")
                .addOption(OptionType.STRING, "tempo_ocultar", "Tempo para memorizar a string (ex: 5).", true)
                .addOption(OptionType.STRING, "tempo_limite", "Tempo para responder após a string sumir (ex: 10).", true)
                .addOption(OptionType.STRING, "string", "A string a ser memorizada.", true);
    }
}