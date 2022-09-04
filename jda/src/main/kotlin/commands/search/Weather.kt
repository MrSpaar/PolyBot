package commands.search

import api.WeatherObj
import com.github.kittinunf.fuel.gson.responseObject
import com.github.kittinunf.fuel.httpGet
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import replyEmbed

object Weather {
    val commandData = SubcommandData("meteo", "Rechercher la météo actuelle d'une ville").addOption(
            OptionType.STRING, "ville", "La ville dont tu veux la météo", true
        )

    fun execute(event: SlashCommandInteractionEvent) {
        val city = event.getOption("ville")!!.asString
        "https://api.openweathermap.org/data/2.5/forecast?q=$city&units=metric&APPID=${Vars.WEATHER_TOKEN}"
            .httpGet()
            .responseObject<WeatherObj> { _, _, (data, _) ->
                if (data?.list == null)
                    return@responseObject replyEmbed(event.interaction, Colors.RED, "❌ Ville introuvable", true)

                val cast = data.list[0]
                val weather = when(cast.weather[0].icon) {
                    "01d", "01n" -> "ensoleillé"
                    "02d", "02n" -> "légèrement nuageux"
                    "03d", "03n" -> "nuageux"
                    "04d", "04n" -> "gris"
                    "09d" -> "très pluivieux"
                    "10d" -> "pluivieux"
                    "11d" -> "orageux"
                    "13d" -> "enneigé"
                    "50d" -> "brumeux"
                    else -> "inconnu"
                }

                event.interaction.replyEmbeds(
                    EmbedBuilder()
                        .setColor(Colors.BLUE)
                        .setTitle("\uD83D\uDD0E Météo à $city")
                        .setDescription(
                            "Le temps est actuellement $weather.\n\n" +
                                    "\uD83C\uDF21️ Température : ${cast.main.temp}°C\n" +
                                    "\uD83D\uDCA7 Humidité : ${cast.main.humidity}%\n" +
                                    "\uD83C\uDF2C️ Vent : ${cast.wind.speed}km/h\n"
                        )
                        .setThumbnail("https://openweathermap.org/img/w/${cast.weather[0].icon}.png")
                        .build()
                ).queue()
            }
    }

}