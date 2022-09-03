import commands.*
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.cache.CacheFlag
import sun.misc.Signal

fun main() {
    val jda = JDABuilder.createDefault(Vars.DISCORD_TOKEN)
        .setStatus(OnlineStatus.ONLINE)
        .setActivity(Activity.playing("vous observer"))
        .enableIntents(
            GatewayIntent.GUILD_PRESENCES,
            GatewayIntent.MESSAGE_CONTENT,
            GatewayIntent.GUILD_MEMBERS,
        )
        .enableCache(
            CacheFlag.ACTIVITY,
            CacheFlag.ONLINE_STATUS,
            CacheFlag.ROLE_TAGS
        )
        .build()

    buildCommands(jda)
    println("Bot is ready !")
    Signal.handle(Signal("INT")) { jda.shutdown() }
}

fun buildCommands(jda: JDA) {
    val commands = arrayListOf<SlashCommandData>().apply {
        this.add(Config.build(jda))
        this.add(Info.build(jda))
        this.addAll(Games.build(jda))
        this.addAll(Moderation.build(jda))
        this.addAll(Utility.build(jda))
    }

    jda.awaitReady().getGuildById(1013076480961560628L)!!.updateCommands().addCommands(commands).queue()
}