package events

import Colors
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.components.ComponentInteraction
import org.apache.commons.collections4.CollectionUtils.intersection

class Roles: ListenerAdapter() {
    override fun onButtonInteraction(event: ButtonInteractionEvent) {
        addRole(event, event.componentId, emptyList())
    }

    override fun onSelectMenuInteraction(event: SelectMenuInteractionEvent) {
        addRole(event, event.values[0], event.component.options.map { it.value })
    }

    private fun addRole(event: GenericComponentInteractionCreateEvent, roleId: String, filters: List<String>) {
        if (!event.isFromGuild) return
        val userRoles = event.member!!.roles.map { it.id }

        if (intersection(filters, userRoles).isNotEmpty())
            return replyEmbed(event.interaction, Colors.RED, "❌ Tu as déjà un des rôles")

        val role = event.guild!!.getRoleById(roleId)
            ?: return replyEmbed(event.interaction, Colors.RED, "❌ Rôle introuvable")

        if (roleId in userRoles)
            return replyEmbed(event.interaction, Colors.RED, "❌ Tu as déjà le rôle ${role.asMention}")

        event.guild!!.addRoleToMember(event.user, role).queue()
        replyEmbed(event.interaction, Colors.GREEN, "✅ Rôle ${role.asMention} ajouté")
    }

    private fun replyEmbed(interaction: ComponentInteraction, color: Int, description: String) {
        interaction.replyEmbeds(
            EmbedBuilder()
                .setColor(color)
                .setDescription(description)
                .build()
        ).setEphemeral(true).queue()
    }
}