package commands

import Colors
import Server
import updateConfig
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import replyEmbed

class Config: ListenerAdapter() {
    companion object {
        fun build(jda: JDA): SlashCommandData {
            jda.addEventListener(Config())

            return Commands.slash("config", "Base configuration").addOptions(
                OptionData(OptionType.STRING, "parametre", "Le paramètre à modifier", true)
                    .addChoice("Salon des logs", "logs")
                    .addChoice("Salon des annonces", "announce")
                    .addChoice("Rôle des nouveaux", "newcomer")
                    .addChoice("Message de bienvenue", "welcomeText")
                    .addChoice("Salon des messages de bienvenue", "welcomeChannel"),
                OptionData(OptionType.CHANNEL, "salon", "Le nouveau salon"),
                OptionData(OptionType.ROLE, "role", "Le nouveau rôle"),
                OptionData(OptionType.STRING, "message", "Le nouveau message")
            )
        }
    }

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        if (event.name != "config" || event.guild == null) return

        if (!event.member!!.hasPermission(Permission.ADMINISTRATOR)) {
            replyEmbed(event.interaction, Colors.RED, "❌ Tu n'as pas la permission d'utiliser cette commande", true)
            return
        }

        val guildId = event.guild!!.idLong
        val param = event.getOption("parametre")!!.asString

        val message = event.getOption("message")?.asString ?: ""
        val role = event.getOption("role")?.asRole?.idLong ?: 0
        val channel = event.getOption("role")?.asChannel?.idLong ?: 0

        when (param) {
            "logs" -> updateConfig(guildId, Server::logsChannelId, channel)
            "newcomer" -> updateConfig(guildId, Server::newcomerRoleId, role)
            "welcomeText" -> updateConfig(guildId, Server::welcomeText, message)
            "announce" -> updateConfig(guildId, Server::announceChannelId, channel)
            "welcomeChannel"  -> updateConfig(guildId, Server::announceChannelId, channel)
        }

        replyEmbed(event.interaction, Colors.GREEN, "✒️ Paramètre modifié avec succès")
    }
}