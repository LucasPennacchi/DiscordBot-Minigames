package br.com.bot.games.forca;

import br.com.bot.core.ConfigManager;
import br.com.bot.core.GameManager;
import br.com.bot.shared.AbstractInteractiveGameCommand;
import br.com.bot.shared.Game;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Comando que implementa o Jogo da Forca.
 * <p>
 * Esta classe herda do molde de jogos interativos, {@link AbstractInteractiveGameCommand},
 * pois precisa enviar uma mensagem de estado (o "tabuleiro") e editá-la
 * conforme o jogo progride.
 *
 * @author Lucas
 */
public class ForcaCommand extends AbstractInteractiveGameCommand {

    /**
     * Constrói o comando da Forca com suas dependências necessárias,
     * passando-as para a classe-mãe abstrata.
     *
     * @param gameManager O gerenciador de jogos ativos.
     * @param configManager O gerenciador de configurações de servidor.
     * @param scheduler O agendador de tarefas para os timers.
     */
    public ForcaCommand(GameManager gameManager, ConfigManager configManager, ScheduledExecutorService scheduler) {
        super(gameManager, configManager, scheduler);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Valida as opções 'tempo', 'palavra' e 'erros' e cria uma instância de {@link ForcaGame}.
     */
    @Override
    protected Optional<Game> createGame(SlashCommandInteractionEvent event) {
        Optional<Long> tempoOpt = parseTimeOption(event, "tempo");
        if (tempoOpt.isEmpty()) {
            return Optional.empty();
        }

        String palavra = event.getOption("palavra").getAsString();
        int erros = event.getOption("erros").getAsInt();
        String issuerId = event.getUser().getId();

        return Optional.of(new ForcaGame(tempoOpt.get(), palavra, erros, issuerId));
    }

    /**
     * {@inheritDoc}
     * <p>
     * Cria a mensagem inicial do jogo, que é o Embed com o "tabuleiro" da forca.
     */
    @Override
    protected MessageCreateData getInitialMessage(Game game) {
        ForcaGame forcaGame = (ForcaGame) game;
        return MessageCreateData.fromEmbeds(forcaGame.buildGameEmbed());
    }

    /**
     * {@inheritDoc}
     * <p>
     * Define o ID da mensagem no objeto do jogo para que ele possa ser editado posteriormente.
     */
    @Override
    protected void setGameMessageId(Game game, String messageId) {
        ((ForcaGame) game).setMessageId(messageId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getTimeoutMessage(Game game) {
        return "O tempo para o jogo da forca esgotou! A palavra era `" + ((ForcaGame) game).getPalavraSecreta() + "`.";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash("forca", "Inicia um jogo da forca.")
                .addOption(OptionType.STRING, "palavra", "A palavra secreta a ser adivinhada.", true)
                .addOption(OptionType.STRING, "tempo", "O tempo limite em segundos (ex: 120).", true)
                .addOption(OptionType.INTEGER, "erros", "O número máximo de erros permitidos (mínimo 1).", true);
    }
}