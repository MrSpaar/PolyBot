import commands.Config
import commands.Moderation
import commands.Utility
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import sun.misc.Signal

fun main() {
    val jda = JDABuilder.createDefault(Vars.DISCORD_TOKEN)
                        .setStatus(OnlineStatus.ONLINE)
                        .setActivity(Activity.playing("vous observer"))
                        .build()

    val commands = arrayListOf<SlashCommandData>().apply {
        this.add(Config.build(jda))
        this.addAll(Moderation.build(jda))
        this.addAll(Utility.build(jda))
    }

    jda.awaitReady().getGuildById(1013076480961560628L)!!.updateCommands().addCommands(commands).queue()

    println("Bot is ready !")
    Signal.handle(Signal("INT")) { jda.shutdown() }
}