package commands.search

import api.AnimeObj
import com.github.kittinunf.fuel.gson.responseObject
import com.github.kittinunf.fuel.httpGet
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import replyEmbed
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object Anime {
    val commandData = SubcommandData("anime", "Rechercher un anime").addOption(
        OptionType.STRING, "nom", "Le nom de l'anime", true
    )

    fun execute(event: SlashCommandInteractionEvent) {
        val name = event.getOption("nom")!!.asString
        "https://kitsu.io/api/edge/anime?filter[text]=$name"
            .httpGet()
            .responseObject<AnimeObj> { _, _, (data, _) ->
                if (data?.data == null)
                    return@responseObject replyEmbed(event.interaction, Colors.RED, "❌ Anime introuvable", true)

                val anime = data.data[0].attributes

                val startDate = formatDate(anime.startDate)
                val endDate = if(anime.endDate == "") "En cours" else formatDate(anime.endDate)

                event.interaction.replyEmbeds(
                    EmbedBuilder()
                        .setColor(Colors.BLUE)
                        .setDescription(anime.synopsis.dropLast(24))
                        .setAuthor("Anime - ${anime.canonicalTitle}", null, anime.posterImage.tiny)
                        .addField("\uD83E\uDD47 Score", "${anime.averageRating}", true)
                        .addField("\uD83D\uDDA5️ Épisodes", "${anime.episodeCount} (${anime.totalLength/3600}h)", true)
                        .addField("\uD83D\uDCC5 Diffusion", "$startDate → $endDate", true)
                        .build()
                ).queue()
            }
    }

    private fun formatDate(string: String): String {
        val local = LocalDate.parse(string)
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

        return formatter.format(local)
    }
}