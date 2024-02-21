package events.channels

import Database
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class DeleteChannel: ListenerAdapter() {
    override fun onGuildVoiceLeave(event: GuildVoiceLeaveEvent) {
        if (event.channelLeft.members.size > 0) return

        val entry = Database.findTempChannel(event.guild.idLong, event.channelLeft.idLong)

        if (!entry.next())
            return

        event.guild.getTextChannelById(entry.getLong("text_chan_id"))?.delete()?.queue()
        event.guild.getVoiceChannelById(entry.getLong("voice_chan_id"))?.delete()?.queue()

        Database.deleteTempChannel(event.guild.idLong, entry.getLong("voice_chan_id"))
    }
}