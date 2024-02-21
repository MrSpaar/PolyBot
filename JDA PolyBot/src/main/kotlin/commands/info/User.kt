package commands.info

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import replyEmbed

object User {
    val commandData = SubcommandData("role", "Afficher des informations sur un rôle").addOption(
        OptionType.ROLE, "mention", "Le rôle dont tu veux les informations", true
    )

    fun execute(event: SlashCommandInteractionEvent) {
        val member = event.guild!!.getMember(event.getOption("mention")?.asUser ?: event.user)
            ?: return replyEmbed(event.interaction, Colors.RED, "❌ Cette commande n'est utilisable que dans un serveur", true)

        var description = "\uD83D\uDCDD A créé son compte <t:${member.timeCreated.toEpochSecond()}:R>\n"
        description += "⏱️ A rejoint le serveur <t:${member.timeJoined.toEpochSecond()}:R>\n"

        if (member.roles.isNotEmpty())
            description += "\uD83C\uDFF7️ Rôle principal : ${member.roles.last().asMention}\n"

        if (member.activities.isNotEmpty()) {
            description += "\n\uD83C\uDFC3\u200D♂️ Activités :\n"

            member.activities.forEach pass@{when (it.type) {
                Activity.ActivityType.PLAYING -> description += "Joue à `${it.name}`\n"
                Activity.ActivityType.WATCHING -> description += "Regarde `${it.name}`\n"
                Activity.ActivityType.LISTENING -> {
                    if (!it.isRich) return@pass
                    val rich = it.asRichPresence()!!

                    if (rich.details == null) return@pass
                    description += "Ecoute `${rich.details!!}`\n"
                }
                else -> {}
            }}
        }

        event.interaction.replyEmbeds(
            EmbedBuilder()
                .setColor(Colors.LIGHT_GREEN)
                .setDescription(description)
                .setAuthor("${member.effectiveName} - ${Translate.STATUS[member.onlineStatus]}", null, member.effectiveAvatarUrl)
                .build()
        ).queue()
    }
}
