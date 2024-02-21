package commands.music

import Colors
import replyEmbed
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.audio.AudioSendHandler
import net.dv8tion.jda.api.entities.AudioChannel
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction
import java.nio.ByteBuffer

class Manager(val guild: Guild, private val channel: AudioChannel, val interaction: SlashCommandInteraction): DefaultAudioPlayerManager() {
    val player: AudioPlayer = this.createPlayer()
    val queue = ArrayList<AudioTrack>()

    companion object {
        val MANAGERS = HashMap<Long, Manager>()

        fun loadItem(guild: Guild, channel: AudioChannel, interaction: SlashCommandInteraction, query: String) {
            MANAGERS.computeIfAbsent(guild.idLong) { Manager(guild, channel, interaction) }.apply {
                this.registerSourceManager(YoutubeAudioSourceManager())
                this.loadItem(query, ResultHandler(this, interaction, query.startsWith("ytsearch")))
            }
        }
    }

    private fun getQueue(): String {
        var str = if(queue.isEmpty()) "*Aucune vidéo en attente*" else ""

        queue.forEachIndexed {i, t ->
            str += "${i+1}) [`${t.info.title}`](${t.info.uri}) de `${t.info.author}`\n"
        }

        return str
    }

    fun ensureConnection() {
        if (guild.audioManager.connectedChannel != null) return

        player.addListener(EventHandler(this))
        guild.audioManager.openAudioConnection(channel)
        guild.audioManager.isSelfDeafened = true
        guild.audioManager.sendingHandler = SendHandler(player)
    }

    fun createMessage(track: AudioTrack? = null, init: Boolean = true) {
        val info = track?.info ?: player.playingTrack.info

        val nowPlaying = EmbedBuilder()
            .setColor(Colors.BLUE)
            .setDescription("\uD83C\uDFB5 [`${info.title}`](${info.uri}) de `${info.author}`")
            .build()

        val queue = EmbedBuilder()
            .setColor(Colors.LIGHT_GREEN)
            .setDescription(getQueue())
            .build()

        if (init) interaction.replyEmbeds(nowPlaying, queue).queue()
        else interaction.hook.editOriginalEmbeds(nowPlaying, queue).queue()
    }
}

class ResultHandler(private val manager: Manager, private val interaction: SlashCommandInteraction, private val search: Boolean): AudioLoadResultHandler {
    override fun trackLoaded(track: AudioTrack) {
        manager.ensureConnection()
        manager.player.startTrack(track, true).apply {
            if (this) return manager.createMessage(track)

            manager.queue.add(track)
            manager.createMessage(init = true)

            replyEmbed(interaction, Colors.GREEN,"✅ `${track.info.title}` ajouté à la file d'attente", true)
        }
    }

    override fun playlistLoaded(playlist: AudioPlaylist) {
        manager.ensureConnection()
        manager.player.startTrack(playlist.tracks[0], true).apply {
            if (this && !search)
                manager.queue.addAll(playlist.tracks.drop(0))
            else if (!this && search)
                manager.queue.add(playlist.tracks[0])
            else if (!search)
                manager.queue.addAll(playlist.tracks)

            if (this)
                manager.createMessage(playlist.tracks[0])
            else {
                manager.createMessage(init = false)
                replyEmbed(interaction, Colors.GREEN, "✅ Vidéos ajoutées", true)
            }
        }
    }

    override fun noMatches() =
        replyEmbed(interaction, Colors.RED, "❌ Aucun résultat trouvé", true)

    override fun loadFailed(e: FriendlyException?) =
        replyEmbed(interaction, Colors.RED, "❌ Aucun résultat trouvé", true)
}

class EventHandler(private val manager: Manager): AudioEventAdapter() {
    override fun onTrackEnd(player: AudioPlayer, track: AudioTrack, endReason: AudioTrackEndReason?) {
        if (manager.queue.isNotEmpty()) {
            player.playTrack(manager.queue.removeFirst())
            manager.createMessage(init = false)
            return
        }

        manager.interaction.hook.deleteOriginal().queue()
        manager.guild.audioManager.closeAudioConnection()
        manager.player.destroy()
        Manager.MANAGERS.remove(manager.guild.idLong)
    }
}

class SendHandler(private val player: AudioPlayer): AudioSendHandler {
    private var lastFrame: AudioFrame? = null

    override fun canProvide(): Boolean {
        lastFrame = player.provide()
        return lastFrame != null
    }

    override fun provide20MsAudio(): ByteBuffer? {
        return ByteBuffer.wrap(lastFrame?.data)
    }

    override fun isOpus(): Boolean {
        return true
    }
}