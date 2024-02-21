package events.logs

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.TextChannel

object Logs {
    val listenerData = arrayOf(MemberLogger(), GuildLogger(), RoleLogger())

    fun sendLog(channel: TextChannel, color: Int, description: String) {
        channel.sendMessageEmbeds(
            EmbedBuilder()
                .setColor(color)
                .setDescription(description)
                .build()
        ).queue()
    }
}