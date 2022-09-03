package commands

import Colors
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import replyEmbed

class Utility: ListenerAdapter() {
    companion object {
        fun build(jda: JDA): Array<SlashCommandData> {
            jda.addEventListener(Utility())

            return arrayOf(
                Commands.slash("emoji", "Récupérer l'image d'un émoji").addOption(
                    OptionType.STRING, "emoji", "L'emoji dont tu veux la source", true
                ),
                Commands.slash("sondage", "Faire un sondage (9 choix maximum)").addOptions(
                    OptionData(OptionType.STRING, "question", "La question du sondage", true),
                    OptionData(OptionType.STRING, "choix", "Les choix du sondage (Choix 1 | Choix 2 | ...)", true)
                ),
                Commands.slash("pfp", "Récupérer une image de profil").addOption(
                    OptionType.USER, "membre", "Le membre dont tu veux l'image de profil"
                )
            )
        }
    }

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        when (event.name) {
            "emoji" -> emojiCommand(event)
            "sondage" -> pollCommand(event)
            "pfp" -> ppCommand(event)
        }
    }

    private fun emojiCommand(event: SlashCommandInteractionEvent) {
        val arg = event.getOption("emoji")!!.asString
        val emoji = event.jda.getEmojiById(arg.replace("\\D+".toRegex(), ""))

        if (emoji == null) {
            replyEmbed(event.interaction, Colors.RED, "❌ Emoji introuvable", true)
            return
        }

        event.interaction.replyEmbeds(
            EmbedBuilder().setColor(Colors.BLUE).setImage(emoji.imageUrl).build()
        ).queue()
    }

    private fun pollCommand(event: SlashCommandInteractionEvent) {
        val name = event.member?.effectiveName ?: event.user.name
        val question = event.getOption("question")!!.asString
        val choices = event.getOption("choix")!!.asString.split("|")

        val embed = EmbedBuilder().setTitle(">> $question")
                                  .setColor(Colors.BLUE)
                                  .setAuthor("Sondage de $name", "", event.user.avatarUrl)

        val reactions = arrayOf("1️⃣", "2️⃣", "3️⃣", "4️⃣", "5️⃣", "6️⃣", "7️⃣", "8️⃣", "9️⃣")

        for (i in choices.indices)
            embed.addField("${reactions[i]} Option n°${i+1}", "```${choices[i].trim()}```", false)

        event.interaction.replyEmbeds(embed.build()).queue {
            it.retrieveOriginal().queue { msg ->
                for (i in choices.indices)
                    msg.addReaction(Emoji.fromUnicode(reactions[i])).queue()
            }
        }
    }

    private fun ppCommand(event: SlashCommandInteractionEvent) {
        val user = event.getOption("membre")?.asUser ?: event.user

        user.retrieveProfile().queue {
            event.interaction.replyEmbeds(
                EmbedBuilder().setColor(it.accentColorRaw).setImage(user.avatarUrl ?: user.defaultAvatarUrl).build()
            ).queue()
        }
    }
}