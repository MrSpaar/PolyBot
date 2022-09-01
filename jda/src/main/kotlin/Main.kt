import commands.Config
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Activity
import sun.misc.Signal

fun main() {
    val jda = JDABuilder.createDefault(Vars.DISCORD_TOKEN)
                        .setStatus(OnlineStatus.ONLINE)
                        .setActivity(Activity.playing("vous observer"))
                        .build()

    arrayOf(1013076480961560628L).forEach {
        jda.awaitReady().getGuildById(it)!!.updateCommands().addCommands(
            Config.build(jda)
        ).queue()
    }

    println("Bot is ready !")
    Signal.handle(Signal("INT")) { jda.shutdown() }
}