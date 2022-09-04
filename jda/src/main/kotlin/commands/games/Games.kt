package commands.games

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

class Games: ListenerAdapter() {
    companion object {
        fun build(jda: JDA): Array<SlashCommandData> {
            jda.addEventListener(Games())

            return arrayOf(Coinflip.commandData, Roll.commandData, Hangman.commandData)
        }
    }

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        when (event.name) {
            "coinflip" -> Coinflip.execute(event)
            "roll" -> Roll.execute(event)
            "pendu" -> Hangman.execute(event)
        }
    }
}


