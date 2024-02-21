package commands.info

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData

object Server {
    val commandData = SubcommandData("serveur", "Afficher des informations sur le serveur")


    fun execute(event: SlashCommandInteractionEvent) {
        val guild = event.guild!!
        val emojis = guild.emojis.joinToString(" ") { it.asMention }
        val description = if(guild.description != null) "\n\n${guild.description}" else ""

        event.interaction.replyEmbeds(
            EmbedBuilder()
                .setColor(Colors.CYAN)
                .setAuthor("${guild.name} - ${guild.id}", null, guild.iconUrl)
                .setDescription(
                    description +
                            "\uD83D\uDE4D ${guild.memberCount} membres au total " +
                            "\uD83D\uDCDD ${guild.roles.size} rôles et ${guild.channels.size} salons\n" +
                            "\uD83D\uDD10 Géré par ${guild.owner!!.asMention} et créé le <t:${guild.timeCreated.toEpochSecond()}:D>\n\n" +
                            "Emojis du serveur : $emojis"
                )
                .build()
        ).queue()
    }
}