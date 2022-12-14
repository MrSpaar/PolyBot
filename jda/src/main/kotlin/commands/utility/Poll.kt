package commands.utility

import Colors
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import replyEmbed

object Poll {
    val commandData = Commands.slash("sondage", "Faire un sondage (26 choix maximum)").addOptions(
        OptionData(OptionType.STRING, "question", "La question du sondage", true),
        OptionData(OptionType.STRING, "choix", "Les choix du sondage (Choix 1 | Choix 2 | ...)", true)
    )

    fun execute(event: SlashCommandInteractionEvent) {
        val name = event.member?.effectiveName ?: event.user.name
        val question = event.getOption("question")!!.asString
        val choices = event.getOption("choix")!!.asString.split("|")

        if(choices.size > 26)
            return replyEmbed(event.interaction, Colors.RED, "โ Le nombre de choix est limitรฉ ร  26", true)

        var description = "> **$question**\n\n"
        val reactions = arrayOf("๐ฆ", "๐ง", "๐จ", "๐ฉ", "๐ช", "๐ซ", "๐ฌ", "๐ญ", "๐ฎ", "๐ฏ", "๐ฐ", "๐ฑ", "๐ฒ", "๐ณ", "๐ด", "๐ต", "๐ถ", "๐ท", "๐ธ", "๐น", "๐บ", "๐ป", "๐ผ", "๐ฝ", "๐พ", "๐ฟ")

        choices.forEachIndexed { i, s ->
            description += "**Option ${reactions[i]}**\n\u200E โณ $s\n\n"
        }

        event.interaction.replyEmbeds(
            EmbedBuilder()
                .setColor(Colors.BLUE)
                .setDescription(description)
                .setAuthor("Sondage de $name", null, event.user.avatarUrl)
                .build()
        ).queue {
            it.retrieveOriginal().queue { msg ->
                for (i in choices.indices)
                    msg.addReaction(Emoji.fromUnicode(reactions[i])).queue()
            }
        }
    }
}