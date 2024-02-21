package events.channels

import Database
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class CreateChannel: ListenerAdapter() {
    override fun onGuildVoiceJoin(event: GuildVoiceJoinEvent) {
        if ("Créer" !in  event.channelJoined.name) return

        val entry = Database.findTempChannel(event.guild.idLong, event.channelJoined.idLong)
        if (entry.next()) return

        val name = "Salon de ${event.member.effectiveName}"
        val category = event.guild.getVoiceChannelById(event.channelJoined.id)?.parentCategory ?: return

        event.guild.createVoiceChannel(name).setParent(category).queue { voc ->
            event.guild.createTextChannel(name).setParent(category).queue { txt ->
                event.guild.moveVoiceMember(event.member, voc).queue()
                Database.insertTempChannel(event.guild.idLong, event.member.idLong, voc.idLong, txt.idLong)
            }
        }
    }
}
