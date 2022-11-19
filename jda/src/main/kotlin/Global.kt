import io.github.cdimascio.dotenv.dotenv
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction

object Vars {
    private val dotenv = dotenv()
    val DISCORD_TOKEN = dotenv["DISCORD_TOKEN"]!!
    val WEATHER_TOKEN = dotenv["WEATHER_TOKEN"]!!
    val TWITCH_CLIENT = dotenv["TWITCH_CLIENT"]!!
    val TWITCH_TOKEN = dotenv["TWITCH_TOKEN"]!!
}

object Colors {
    const val GREEN = 0x2ECC71
    const val ORANGE = 0xC27C0E
    const val RED = 0xE74C3C
    const val BLUE = 0x3498DB
    const val CYAN = 0x346e7a
    const val GOLD = 0xF1C40F
    const val LIGHT_GREEN = 0x1ABC9C
}

object Translate {
    val PERMISSION = hashMapOf(
        Permission.ADMINISTRATOR to "Administrateur",
        Permission.MANAGE_SERVER to "Gérer le serveur",
        Permission.MANAGE_ROLES to "Gérer les rôles",
        Permission.MANAGE_CHANNEL to "Gérer les salons",
        Permission.MANAGE_PERMISSIONS to "Gérer les permissions",
        Permission.MESSAGE_MANAGE to "Gérer les messages",
        Permission.NICKNAME_MANAGE to "Gérer les surnoms",
        Permission.MANAGE_THREADS to "Gérer les fils",
        Permission.MANAGE_WEBHOOKS to "Gérer les webhooks",
        Permission.MANAGE_EMOJIS_AND_STICKERS to "Gérer les emojis",
        Permission.VIEW_GUILD_INSIGHTS to "Voir les analyses",
        Permission.VIEW_AUDIT_LOGS to "Voir les logs",
        Permission.BAN_MEMBERS to "Bannir des membres",
        Permission.KICK_MEMBERS to "Expulser des membres",
        Permission.MODERATE_MEMBERS to "Exclure des membres",
        Permission.VOICE_MUTE_OTHERS to "Rendre des membres muets",
        Permission.VOICE_MOVE_OTHERS to "Bouger des membres de salon",
        Permission.VOICE_DEAF_OTHERS to "Rendre sourd des membres",
        Permission.MESSAGE_ADD_REACTION to "Ajouter des réactions",
        Permission.NICKNAME_CHANGE to "Changer de surnom",
        Permission.CREATE_INSTANT_INVITE to "Créer des invitations",
        Permission.MESSAGE_ATTACH_FILES to "Envoyer des fichiers",
        Permission.MESSAGE_TTS to "Envoyer des TTS",
        Permission.MESSAGE_EMBED_LINKS to "Envoyer des intégrations",
        Permission.VOICE_START_ACTIVITIES to "Démarrer des activités",
        Permission.CREATE_PUBLIC_THREADS to "Créer des fils publiques",
        Permission.CREATE_PRIVATE_THREADS to "Créer des fils privés",
        Permission.USE_APPLICATION_COMMANDS to "Utiliser des commandes",
        Permission.MANAGE_EMOJIS_AND_STICKERS to "Envoyer des emojis et des stickers",
        Permission.MESSAGE_EXT_EMOJI to "Envoyer des emojis externes",
        Permission.MESSAGE_EXT_STICKER to "Envoyer des stickers externes",
        Permission.MESSAGE_SEND to "Envoyer des messages",
        Permission.MESSAGE_SEND_IN_THREADS to "Envoyer des messages dans un fil",
        Permission.MESSAGE_HISTORY to "Voir l'historique des messages",
        Permission.VOICE_SPEAK to "Parler dans un salon",
        Permission.REQUEST_TO_SPEAK to "Demander la parole",
        Permission.PRIORITY_SPEAKER to "Parler en priorité",
        Permission.VOICE_STREAM to "Faire un stream",
        Permission.VOICE_CONNECT to "Se connecter à un salon",
        Permission.VIEW_CHANNEL to "Voir des salons",
        Permission.VOICE_USE_VAD to "Voir les activités vocales",
        Permission.MESSAGE_MENTION_EVERYONE to "Mentionner tous les rôles"
    )

    val STATUS = hashMapOf(
        OnlineStatus.ONLINE to "En ligne",
        OnlineStatus.OFFLINE to "Hors ligne",
        OnlineStatus.IDLE to "Absent",
        OnlineStatus.DO_NOT_DISTURB to "Ne pas déranger",
        OnlineStatus.INVISIBLE to "Invisible",
        OnlineStatus.UNKNOWN to "Inconnu"
    )
}

fun checkPermissions(event: SlashCommandInteractionEvent, self: Boolean, vararg permissions: Permission): Boolean {
    val member = if (self) event.guild!!.selfMember else event.member!!
    val desc = if (self) "Je n'ai" else "Tu n'as"

    if (member.hasPermission(*permissions))
        return false

    replyEmbed(event.interaction, Colors.RED, "❌ $desc la permission de faire ça", true)
    return true
}

fun replyEmbed(interaction: SlashCommandInteraction, color: Int, description: String, ephemeral: Boolean = false) {
    interaction.replyEmbeds(
        EmbedBuilder().setColor(color).setDescription(description).build()
    ).setEphemeral(ephemeral).queue()
}