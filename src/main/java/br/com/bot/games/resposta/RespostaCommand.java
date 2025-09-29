package br.com.bot.games.resposta;

import br.com.bot.core.ConfigManager;
import br.com.bot.core.GameManager;
import br.com.bot.shared.AbstractSimpleGameCommand;
import br.com.bot.shared.Game;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Comando que implementa o Jogo de Pergunta e Resposta.
 * <p>
 * Esta classe herda do molde de jogos simples, {@link AbstractSimpleGameCommand},
 * e define a lógica específica para iniciar um jogo de trivia.
 *
 * @author Lucas
 */
public class RespostaCommand extends AbstractSimpleGameCommand {

    /**
     * Constrói o comando de resposta com suas dependências necessárias,
     * passando-as para a classe-mãe abstrata.
     *
     * @param gameManager O gerenciador de jogos ativos.
     * @param configManager O gerenciador de configurações de servidor.
     * @param scheduler O agendador de tarefas para os timers.
     */
    public RespostaCommand(GameManager gameManager, ConfigManager configManager, ScheduledExecutorService scheduler) {
        super(gameManager, configManager, scheduler);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Valida as opções 'tempo', 'pergunta' e 'resposta' fornecidas pelo usuário
     * e cria uma instância de {@link RespostaGame} com esses dados.
     */
    @Override
    protected Optional<Game> createGame(SlashCommandInteractionEvent event) {
        Optional<Long> tempoOpt = parseTimeOption(event, "tempo");
        if (tempoOpt.isEmpty()) {
            return Optional.empty();
        }

        String pergunta = event.getOption("pergunta").getAsString();
        String resposta = event.getOption("resposta").getAsString();
        String issuerId = event.getUser().getId();

        return Optional.of(new RespostaGame(tempoOpt.get(), pergunta, resposta, issuerId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getPrepareMessage() {
        return String.format("O jogo de perguntas vai começar em %d segundos...", PREPARE_DELAY_SECONDS);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getStartMessage(Game game) {
        RespostaGame respostaGame = (RespostaGame) game;
        double tempoEmSegundos = respostaGame.getTempoLimiteMs() / 1000.0;

        return String.format(
                "Tempo limite: **%.1f segundos**\n\nQual é a resposta para:\n>>> %s",
                tempoEmSegundos,
                respostaGame.getPergunta()
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getTimeoutMessage(Game game) {
        RespostaGame respostaGame = (RespostaGame) game;
        return "O tempo esgotou! A resposta correta era: `" + respostaGame.getRespostaCorreta() + "`";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash("resposta", "Inicia um jogo de pergunta e resposta.")
                .addOption(OptionType.STRING, "tempo", "O tempo para responder em segundos (ex: 30).", true)
                .addOption(OptionType.STRING, "pergunta", "A pergunta a ser exibida.", true)
                .addOption(OptionType.STRING, "resposta", "A resposta correta esperada.", true);
    }
}