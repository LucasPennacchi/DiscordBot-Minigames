package br.com.bot.shared;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

/**
 * A interface de contrato para todos os comandos do bot.
 * Garante que qualquer classe de comando tenha um método de execução e um método
 * para se descrever para a API do Discord.
 *
 * @author Lucas
 */
public interface ICommand {

    /**
     * Executa a lógica principal do comando.
     * @param event O evento de interação do comando de barra que acionou a execução.
     */
    void execute(SlashCommandInteractionEvent event);

    /**
     * Cria e retorna a definição do comando de barra para o Discord.
     * Esta definição inclui o nome, descrição e opções do comando.
     * @return Um objeto {@link SlashCommandData} contendo a definição do comando.
     */
    SlashCommandData getCommandData();
}