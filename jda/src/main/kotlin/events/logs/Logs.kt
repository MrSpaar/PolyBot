package events.logs

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.TextChannel

object Logs {
    val listenerData = arrayOf(MemberLogger(), MiscLogger(), RoleLogger())

    fun sendLog(channel: TextChannel, description: String, color: Int) {
        channel.sendMessageEmbeds(
            EmbedBuilder()
                .setColor(color)
                .setDescription(description)
                .build()
        ).queue()
    }
}