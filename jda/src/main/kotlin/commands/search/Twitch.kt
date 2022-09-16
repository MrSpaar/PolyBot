package commands.search

import Colors
import Vars
import api.OAuth
import api.TwitchObj
import com.github.kittinunf.fuel.gson.responseObject
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPost
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import replyEmbed
import java.time.Instant

object Twitch {
    val commandData = SubcommandData("twitch", "Rechercher des streams Twitch").addOptions(
        OptionData(OptionType.STRING, "categorie", "La catégorie des streams", true),
        OptionData(OptionType.STRING, "filtres", "Mots-clés pour filtrer les résultats")
    )

    private var expireIn = Instant.now()
    private var accessToken = ""

    fun execute(event: SlashCommandInteractionEvent) {
        if (accessToken == "" || expireIn.isBefore(Instant.now()))
            updateToken()

        val category = event.getOption("categorie")!!.asString
        val filters = event.getOption("filtres")?.asString?.split(" ")

        val limit = if(filters != null) 100 else 10

        "https://api.twitch.tv/helix/search/channels?query=$category&first=$limit&live_only=true"
            .httpGet()
            .header("Client-ID" to Vars.TWITCH_CLIENT, "Authorization" to "Bearer $accessToken")
            .responseObject<TwitchObj> { _, _, (data, _) ->
                if (data?.data == null)
                    return@responseObject replyEmbed(event.interaction, Colors.RED, "❌ Aucun stream trouvé", true)

                val embed = EmbedBuilder()
                    .setColor(Colors.BLUE)
                    .setAuthor("Twitch - ${data.data[0].game_name}", null, "https://i.imgur.com/gArdgyC.png")

                data.data.forEach {
                    if (filters == null || filters.any { filt -> filt.lowercase() in it.title.lowercase() })
                        embed.addField(it.display_name, "[${it.title}](https://www.twitch.tv/${it.broadcaster_login})", true)
                }

                event.interaction.replyEmbeds(embed.build()).queue()
            }
    }

    private fun updateToken() {
        val (_, _, res) = "https://id.twitch.tv/oauth2/token?client_id=${Vars.TWITCH_CLIENT}&client_secret=${Vars.TWITCH_TOKEN}&grant_type=client_credentials"
            .httpPost()
            .responseObject<OAuth>()

        if (res.component1() == null) return

        expireIn = Instant.now().plusSeconds(res.component1()!!.expires_in)
        accessToken = res.component1()!!.access_token
    }
}