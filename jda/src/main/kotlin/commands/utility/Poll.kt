package commands.utility

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import replyEmbed

object Poll {
    val commandData = Commands.slash("sondage", "Faire un sondage (35 choix maximum)").addOptions(
        OptionData(OptionType.STRING, "question", "La question du sondage", true),
        OptionData(OptionType.STRING, "choix", "Les choix du sondage (Choix 1 | Choix 2 | ...)", true)
    )

    fun execute(event: SlashCommandInteractionEvent) {
        val name = event.member?.effectiveName ?: event.user.name
        val question = event.getOption("question")!!.asString
        val choices = event.getOption("choix")!!.asString.split("|")
        if(choices.size > 35) return replyEmbed(event.interaction, Colors.RED, "❌ Le nombre de réponses possibles ne peut excéder 35.", true)

        val embed = EmbedBuilder().setTitle(">> $question")
            .setColor(Colors.BLUE)
            .setAuthor("Sondage de $name", null, event.user.avatarUrl)

        val reactions = arrayOf("1️⃣", "2️⃣", "3️⃣", "4️⃣", "5️⃣", "6️⃣", "7️⃣", "8️⃣", "9️⃣", "🇦", "🇧", "🇨", "🇩", "🇪", "🇫", "🇬", "🇭", "🇮", "🇯", "🇰", "🇱", "🇲", "🇳", "🇴", "🇵", "🇶", "🇷", "🇸", "🇹", "🇺", "🇻", "🇼", "🇽", "🇾", "🇿")

        for (i in choices.indices)
            embed.addField("${reactions[i]} Option n°${i+1}", "```${choices[i].trim()}```", false)

        event.interaction.replyEmbeds(embed.build()).queue {
            it.retrieveOriginal().queue { msg ->
                for (i in choices.indices)
                    msg.addReaction(Emoji.fromUnicode(reactions[i])).queue()
            }
        }
    }
}