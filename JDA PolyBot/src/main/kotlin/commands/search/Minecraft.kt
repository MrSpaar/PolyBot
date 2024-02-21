package commands.search

import Colors
import replyEmbed
import api.StatusObj
import com.github.kittinunf.fuel.gson.responseObject
import com.github.kittinunf.fuel.httpGet
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData

object Minecraft {
    val commandData = SubcommandData("mc", "Status du serveur NNL")

    fun execute(event: SlashCommandInteractionEvent) {
        "https://panel.omgserv.com/json/395089/status"
            .httpGet()
            .responseObject<StatusObj> { _, _, (data, _) ->
                if (data == null)
                    return@responseObject replyEmbed(event.interaction, Colors.RED, "❌ Aucunes données", true)

                val emoji: String
                val online: String
                val status = data.status

                if (status.online) {
                    online = "en ligne"
                    emoji = "\uD83D\uDFE2"
                } else {
                    online = "hors ligne"
                    emoji = "\uD83D\uDD34"
                }

                replyEmbed(
                    event.interaction,
                    Colors.BLUE,
                    "$emoji Le serveur est $online\n\n" +
                        "\uD83C\uDF9A️ CPU : ${status.cpu}%\n" +
                        "\uD83D\uDCBE RAM : %.2fGo\n".format(status.ram/1_000_000.0) +
                        "\uD83E\uDDD1 Connectés : ${status.players.online}"
                )
            }
    }
}