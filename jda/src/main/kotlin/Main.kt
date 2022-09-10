import commands.Config
import commands.games.Games
import commands.info.Info
import commands.levels.Levels
import commands.moderation.Moderation
import commands.music.Music
import commands.search.Search
import commands.utility.Utility
import events.Logs
import events.channels.CreateChannel
import events.channels.DeleteChannel
import events.levels.Pages
import events.levels.Xp
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.MemberCachePolicy
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
        .setMemberCachePolicy(
            MemberCachePolicy.ALL
        )
        .build()

    addListeners(jda)
    buildCommands(jda)
    println("Bot is ready !")
    Signal.handle(Signal("INT")) { jda.shutdown() }
}

fun addListeners(jda: JDA) {
    jda.addEventListener(Pages(), Xp(), Logs(), CreateChannel(), DeleteChannel())
}

fun buildCommands(jda: JDA) {
    val globalCommands = arrayListOf<SlashCommandData>().apply {
        this.add(Search.build(jda))
        this.addAll(Games.build(jda))
        this.addAll(Utility.build(jda))
    }

    val serverCommands = arrayListOf<SlashCommandData>().apply {
        this.add(Config.build(jda))
        this.add(Info.build(jda))
        this.addAll(Music.build(jda))
        this.addAll(Levels.build(jda))
        this.addAll(Moderation.build(jda))
    }

    jda.awaitReady().updateCommands().addCommands(globalCommands).queue()
    arrayOf(752921557214429316L, 339045627478540288L, 634339847108165632L).forEach {
        jda.awaitReady().getGuildById(it)!!.updateCommands().addCommands(serverCommands).queue()
    }
}