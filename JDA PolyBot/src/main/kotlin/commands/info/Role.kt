package commands.info

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import okhttp3.internal.toHexString

object Role {
    val commandData = SubcommandData("membre", "Afficher des informations sur un membre").addOption(
        OptionType.USER, "mention", "Le membre dont tu veux les informations"
    )

    fun execute(event: SlashCommandInteractionEvent) {
        val role = event.getOption("mention")!!.asRole

        var description = "⏱️ Créé <t:${role.timeCreated.toEpochSecond()}:R>\n"

        if (role.color != null)
            description += "\uD83C\uDF08 Couleur : `#${role.color!!.rgb.toHexString()}`\n"

        description += "\uD83D\uDD14 " + if (role.isMentionable) "Mentionnable" else "Non mentionnable"
        description += if (role.isHoisted) " et affiché séparemment\n" else "\n"

        description += "\n✅ Permissions :\n"
        role.permissions.forEach { description += "- ${Translate.PERMISSION[it]}\n" }

        event.interaction.replyEmbeds(
            EmbedBuilder()
                .setColor(role.color)
                .setDescription(description)
                .setAuthor("${role.name} - ${role.id}", null, event.guild!!.iconUrl)
                .build()
        ).queue()
    }
}
