package commands.games

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.interactions.commands.build.Commands
import java.io.File
import java.text.Normalizer
import java.util.*
import kotlin.collections.ArrayList

object Hangman {
    val commandData = Commands.slash("pendu", "Jouer au pendu")

    fun execute(event: SlashCommandInteractionEvent) = Listener(event)

    class Listener(private val event: SlashCommandInteractionEvent): ListenerAdapter() {
        private lateinit var hook: InteractionHook
        private val word = File("wordlist.txt").readLines().shuffled()[0]

        private var lives = 5
        private val normalized = normalize(word)
        private val errors = ArrayList<String>()
        private var discovered = Array(word.length) {false}

        init {
            event.interaction.replyEmbeds(buildEmbed()).queue {
                hook = it
                event.jda.addEventListener(this)
            }
        }

        override fun onMessageReceived(event: MessageReceivedEvent) {
            if (hook.interaction.user != event.author) return
            if (hook.interaction.messageChannel.idLong != event.channel.idLong) return

            val guess = normalize(event.message.contentRaw).lowercase(Locale.getDefault())
            event.message.delete().queue()

            if (guess in errors) return

            if (lives == 1) {
                hook.jda.removeEventListener(this)
                updateEmbed(EmbedBuilder().setColor(Colors.RED).setDescription("Perdu ! Le mot était  `$word`").build())
                return
            }

            if (guess != normalized && guess !in normalized) {
                lives--
                errors.add(guess)
                updateEmbed()
                return
            }

            word.forEachIndexed pass@{i, c ->
                if (discovered[i]) return@pass
                discovered[i] = c == guess[0]
            }

            if (guess.length > 1 || discovered.all{b -> b}) {
                hook.jda.removeEventListener(this)
                updateEmbed(EmbedBuilder().setColor(Colors.GOLD).setDescription("Gagné ! Tu as deviné `$word`").build())
                return
            }

            updateEmbed()
        }

        private fun updateEmbed(embed: MessageEmbed? = null) {
            hook.editOriginalEmbeds(embed ?: buildEmbed()).queue()
        }

        private fun buildEmbed(): MessageEmbed {
            val errorsStr = if (errors.isEmpty()) "\u200b" else errors.joinToString(", ")
            val wordStr = word.mapIndexed{i, c -> if(discovered[i]) c else "-"}.joinToString("")

            return EmbedBuilder().setTitle("Partie de pendu")
                .addField("Mot : ", "```$wordStr```", false)
                .addField("Erreurs", "```$errorsStr```", false)
                .setFooter("Vies : $lives")
                .setColor(Colors.BLUE)
                .build()
        }

        private fun normalize(word: String): String {
            return Normalizer.normalize(word, Normalizer.Form.NFKD)
        }
    }
}