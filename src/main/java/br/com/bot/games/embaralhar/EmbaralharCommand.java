package br.com.bot.games.embaralhar;

import br.com.bot.core.ConfigManager;
import br.com.bot.core.GameManager;
import br.com.bot.shared.AbstractSimpleGameCommand;
import br.com.bot.shared.Game;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Comando que implementa o Jogo de Embaralhar Palavras.
 * <p>
 * Esta classe herda do molde de jogos simples, {@link AbstractSimpleGameCommand}, e fornece a lógica para
 * embaralhar uma palavra e iniciar o jogo de adivinhação.
 *
 * @author Lucas
 */
public class EmbaralharCommand extends AbstractSimpleGameCommand {

    /**
     * Constrói o comando de embaralhar com suas dependências necessárias,
     * passando-as para a classe-mãe abstrata.
     *
     * @param gameManager O gerenciador de jogos ativos.
     * @param configManager O gerenciador de configurações de servidor.
     * @param scheduler O agendador de tarefas para os timers.
     */
    public EmbaralharCommand(GameManager gameManager, ConfigManager configManager, ScheduledExecutorService scheduler) {
        super(gameManager, configManager, scheduler);
    }

    /**
     * Embaralha os caracteres de uma palavra de forma aleatória.
     * Garante que a palavra embaralhada não seja igual à original.
     *
     * @param palavra A palavra original.
     * @return A palavra com os caracteres embaralhados.
     */
    private String embaralharPalavra(String palavra) {
        List<Character> chars = new ArrayList<>();
        for (char c : palavra.toCharArray()) {
            chars.add(c);
        }

        String embaralhada;
        // Evita que uma palavra curta como "oi" seja embaralhada para "oi" novamente.
        do {
            Collections.shuffle(chars);
            StringBuilder sb = new StringBuilder();
            for (char c : chars) {
                sb.append(c);
            }
            embaralhada = sb.toString();
        } while (embaralhada.equals(palavra) && palavra.length() > 1);

        return embaralhada;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Valida as opções 'tempo' e 'palavra' fornecidas pelo usuário
     * e cria uma instância de {@link EmbaralharGame} com esses dados.
     */
    @Override
    protected Optional<Game> createGame(SlashCommandInteractionEvent event) {
        Optional<Long> tempoOpt = parseTimeOption(event, "tempo");
        if (tempoOpt.isEmpty()) {
            return Optional.empty();
        }

        String palavra = event.getOption("palavra").getAsString();
        String issuerId = event.getUser().getId();

        return Optional.of(new EmbaralharGame(tempoOpt.get(), palavra, issuerId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getPrepareMessage() {
        return String.format("O jogo de embaralhar vai começar em %d segundos...", PREPARE_DELAY_SECONDS);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getStartMessage(Game game) {
        EmbaralharGame embaralharGame = (EmbaralharGame) game;
        String palavraOriginal = embaralharGame.getPalavraOriginal();
        String palavraEmbaralhada = embaralharPalavra(palavraOriginal);
        double tempoEmSegundos = embaralharGame.getTempoLimiteMs() / 1000.0;

        return String.format(
                "Tempo limite: **%.1f segundos**\n\nDesembaralhe a seguinte palavra: `%s`",
                tempoEmSegundos,
                palavraEmbaralhada
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getTimeoutMessage(Game game) {
        EmbaralharGame embaralharGame = (EmbaralharGame) game;
        return "O tempo esgotou! A palavra correta era: `" + embaralharGame.getPalavraOriginal() + "`";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash("embaralhar", "Inicia um jogo de adivinhar a palavra embaralhada.")
                .addOption(OptionType.STRING, "tempo", "O tempo limite em segundos.", true)
                .addOption(OptionType.STRING, "palavra", "A palavra a ser embaralhada.", true);
    }
}