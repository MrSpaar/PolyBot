package commands.search

import api.WikiepediaObj
import com.github.kittinunf.fuel.gson.responseObject
import com.github.kittinunf.fuel.httpGet
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import replyEmbed

object Wikipedia {
    val commandData = SubcommandData("wikipedia", "Rechercher un article Wikipedia").addOption(
        OptionType.STRING, "article", "L'article à rechercher", true
    )

    fun execute(event: SlashCommandInteractionEvent) {
        val name = event.getOption("article")!!.asString

        "https://fr.wikipedia.org/w/api.php?format=json&action=query&prop=extracts|pageimages&exintro&explaintext&redirects=1&titles=${name}"
            .httpGet()
            .responseObject<WikiepediaObj> { _, _, (data, _) ->
                if (data == null || data.query.pages.containsKey(-1))
                    return@responseObject replyEmbed(event.interaction, Colors.RED, "❌ Article introuvable", true)

                val article = data.query.pages.toList()[0].second

                event.interaction.replyEmbeds(
                    EmbedBuilder()
                        .setColor(Colors.BLUE)
                        .setAuthor("Wikipedia - ${article.title}", null, "https://i.imgur.com/nDTQgbf.png")
                        .setDescription("${article.extract} [Lire l'article](https://fr.wikipedia.org/wiki/${article.title})")
                        .build()
                ).queue()
            }
    }

}