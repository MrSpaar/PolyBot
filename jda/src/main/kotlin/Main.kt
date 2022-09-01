import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Activity
import sun.misc.Signal

fun main() {
    val jda = JDABuilder.createDefault(Vars.DISCORD_TOKEN)
                        .setStatus(OnlineStatus.ONLINE)
                        .setActivity(Activity.playing("vous observer"))
                        .build()

    println("Bot is ready !")
    Signal.handle(Signal("INT")) { jda.shutdown() }
}