package commands.music

import Colors
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.Commands
import replyEmbed

object Skip {
    val commandData = Commands.slash("skip", "Passer la vidéo en cours de lecture")

    fun execute(event: SlashCommandInteractionEvent) {
        if (event.member!!.voiceState == null)
            return replyEmbed(event.interaction, Colors.RED, "❌ Vous devez être connecté à un salon")

        val manager = Manager.MANAGERS[event.guild!!.idLong]
            ?: return replyEmbed(event.interaction, Colors.RED, "❌ Aucune vidéo en cours de lecture")

        manager.player.stopTrack()
        replyEmbed(event.interaction, Colors.GREEN, "✅ La vidéo a été passée", true)
    }
}