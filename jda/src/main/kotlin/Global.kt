import io.github.cdimascio.dotenv.dotenv
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction

object Vars {
    private val dotenv = dotenv()
    val DISCORD_TOKEN = dotenv["DISCORD_TOKEN"]!!
    val DB_URI = dotenv["DB_URI"]!!
}

object Colors {
    const val GREEN = 0x2ECC71
    const val ORANGE = 0xC27C0E
    const val RED = 0x3498DB
}

fun replyEmbed(interaction: SlashCommandInteraction, color: Int, description: String) {
    interaction.replyEmbeds(
        EmbedBuilder().setColor(color).setDescription(description).build()
    ).queue()
}