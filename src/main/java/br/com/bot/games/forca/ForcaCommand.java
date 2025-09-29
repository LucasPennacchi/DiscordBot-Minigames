package br.com.bot.games.forca;

import br.com.bot.core.ConfigManager;
import br.com.bot.core.GameManager;
import br.com.bot.shared.Game;
import br.com.bot.shared.ICommand;
import br.com.bot.utils.ValidationUtils;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Comando responsável por iniciar e gerenciar a lógica do Jogo da Forca.
 * Esta é uma implementação direta de {@link ICommand} devido ao seu fluxo de inicialização
 * único, que requer a captura do ID da mensagem do jogo.
 * @author Lucas
 */
public class ForcaCommand implements ICommand {
    private final GameManager gameManager;
    private final ConfigManager configManager;
    private final ScheduledExecutorService scheduler;
    private static final int PREPARE_DELAY_SECONDS = 3;

    /**
     * Constrói o comando da Forca com suas dependências necessárias.
     * @param gameManager O gerenciador de jogos ativos.
     * @param configManager O gerenciador de configurações de servidor.
     * @param scheduler O agendador de tarefas para os timers.
     */
    public ForcaCommand(GameManager gameManager, ConfigManager configManager, ScheduledExecutorService scheduler) {
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
     * Executa a lógica do comando /forca, validando as opções e iniciando o jogo.
     */
    @Override
    public void execute(SlashCommandInteractionEvent event) {
        // ... (código interno do método)
    }

    /**
     * {@inheritDoc}
     * Cria e retorna a definição do comando /forca para o Discord.
     * @return A definição do comando de barra.
     */
    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash("forca", "Inicia um jogo da forca.")
                .addOption(OptionType.STRING, "palavra", "A palavra secreta a ser adivinhada.", true)
                .addOption(OptionType.INTEGER, "erros", "O número máximo de erros permitidos (mínimo 1).", true)
                .addOption(OptionType.STRING, "tempo", "O tempo limite em segundos (ex: 120).", true);
    }
}