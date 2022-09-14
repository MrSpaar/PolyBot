package events.channels

import database.Database
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class DeleteChannel: ListenerAdapter() {
    override fun onGuildVoiceLeave(event: GuildVoiceLeaveEvent) {
        if (event.channelLeft.members.size > 0) return

        val entry = Database.findTempChannel(event.channelLeft.idLong) ?: return

        event.guild.getTextChannelById(entry.txtId)?.delete()?.queue()
        event.guild.getVoiceChannelById(entry._id)?.delete()?.queue()

        Database.deleteTempChannel(entry._id)
    }
}