package commands.menu

import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction
import net.dv8tion.jda.api.interactions.components.ItemComponent
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu

class CreateListener(private val interaction: SlashCommandInteraction, private val type: Int) : ListenerAdapter() {
    override fun onMessageReceived(event: MessageReceivedEvent) {
        if (!event.isFromGuild || interaction.user.id != event.author.id) return

        val components = arrayListOf<ItemComponent>()
        val emojis = event.message.contentRaw.split(" ").filter { it[0] != '<' }

        when (type) {
            0 -> event.message.mentions.roles.forEachIndexed { i, r ->
                components.add(
                    Button.primary(r.id, r.name)
                        .withEmoji(Emoji.fromUnicode(emojis[i]))
                )
            }

            1 -> {
                val select = SelectMenu.create("Menu")

                event.message.mentions.roles.forEachIndexed { i, r ->
                    select.addOption(
                        r.name, r.id, "", Emoji.fromUnicode(emojis[i])
                    )
                }

                components.add(select.build())
            }
        }

        if (components.isEmpty())
            return

        interaction.hook
            .editOriginal("Menu de rôles - ${interaction.getOption("titre")!!.asString}")
            .setEmbeds()
            .setActionRow(components).queue()
        event.message.mentions.roles

        event.message.delete().queue()
        interaction.jda.removeEventListener(this)
    }
}