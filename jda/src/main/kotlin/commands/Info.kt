package commands

import Colors
import Translate
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import okhttp3.internal.toHexString
import replyEmbed

class Info: ListenerAdapter() {
    companion object {
        fun build(jda: JDA): SlashCommandData {
            jda.addEventListener(Info())

            return Commands.slash("info", "Base info").addSubcommands(
                SubcommandData("serveur", "Afficher des informations sur le serveur"),
                SubcommandData("role", "Afficher des informations sur un rôle").addOption(
                    OptionType.ROLE, "mention", "Le rôle dont tu veux les informations", true
                ),
                SubcommandData("membre", "Afficher des informations sur un membre").addOption(
                    OptionType.USER, "mention", "Le membre dont tu veux les informations"
                )
            )
        }
    }

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        if (event.guild == null) return

        when (event.subcommandName) {
            "membre" -> userInfoCommand(event)
            "serveur" -> serverInfoCommand(event)
            "role" -> roleInfoCommand(event)
        }
    }

    private fun userInfoCommand(event: SlashCommandInteractionEvent) {
        val member = event.guild!!.getMember(event.getOption("mention")?.asUser ?: event.user)
            ?: return replyEmbed(event.interaction, Colors.RED, "❌ Cette commande n'est utilisable que dans un serveur")

        var description = "\uD83D\uDCDD A créé son compte <t:${member.timeCreated.toEpochSecond()}:R>\n"
        description += "⏱️ A rejoint le serveur <t:${member.timeJoined.toEpochSecond()}:R>\n"
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

    private fun serverInfoCommand(event: SlashCommandInteractionEvent) {
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

    private fun roleInfoCommand(event: SlashCommandInteractionEvent) {
        val role = event.getOption("mention")!!.asRole

        var description = "⏱️ Créé <t:${role.timeCreated.toEpochSecond()}:R>\n"

        if (role.color != null)
            description += "\uD83C\uDF08 Couleur : `#${role.color!!.rgb.toHexString()}`\n"

        description += "\uD83D\uDD14 " + if(role.isMentionable) "Mentionnable" else "Non mentionnable"
        description += if(role.isHoisted) " et affiché séparemment\n" else "\n"

        description += "\n✅ Permissions :\n"
        role.permissions.forEach{description += "- ${Translate.PERMISSION[it]}\n"; println(it)}

        event.interaction.replyEmbeds(
            EmbedBuilder()
                .setColor(role.color)
                .setDescription(description)
                .setAuthor("${role.name} - ${role.id}", null, event.guild!!.iconUrl)
                .build()
        ).queue()
    }
}