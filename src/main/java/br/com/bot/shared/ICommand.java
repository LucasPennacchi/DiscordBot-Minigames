package br.com.bot.shared;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

// A interface que todos os nossos comandos de barra irão implementar.
public interface ICommand {
    void execute(SlashCommandInteractionEvent event);

    SlashCommandData getCommandData();
}