package commands

import Colors
import Database
import replyEmbed
import checkPermissions
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

class Config: ListenerAdapter() {
    companion object {
        fun build(jda: JDA): SlashCommandData {
            jda.addEventListener(Config())

            return Commands.slash("config", "Base configuration").addOptions(
                OptionData(OptionType.STRING, "parametre", "Le paramètre à modifier", true)
                    .addChoice("Salon des logs", "logs_chan_id")
                    .addChoice("Salon des annonces", "announce_chan_id")
                    .addChoice("Rôle des nouveaux", "newcomer_role_id")
                    .addChoice("Message de bienvenue", "welcome_text")
                    .addChoice("Salon des messages de bienvenue", "welcome_chan_id"),
                OptionData(OptionType.CHANNEL, "salon", "Le nouveau salon"),
                OptionData(OptionType.ROLE, "role", "Le nouveau rôle"),
                OptionData(OptionType.STRING, "message", "Le nouveau message")
            )
        }
    }

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        if (event.name != "config" || event.guild == null) return
        if (checkPermissions(event, false, Permission.ADMINISTRATOR)) return

        val param = event.getOption("parametre")!!.asString

        if ("chan" in param)
            Database.updateSetting(
                event.guild!!.idLong, param,
                event.getOption("salon")?.asChannel?.idLong ?: 0
            )
        else if ("role" in param)
            Database.updateSetting(
                event.guild!!.idLong, param,
                event.getOption("role")?.asRole?.idLong ?: 0
            )
        else
            Database.updateSetting(
                event.guild!!.idLong, param,
                event.getOption("message")?.asString ?: ""
            )

        replyEmbed(event.interaction, Colors.GREEN, "✒️ Paramètre modifié avec succès")
    }
}