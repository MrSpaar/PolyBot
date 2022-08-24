package commands.infos;

import framework.Command;
import org.javacord.api.entity.emoji.KnownCustomEmoji;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.interaction.SlashCommandCreateEvent;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.listener.interaction.SlashCommandCreateListener;
import ressources.Global;

import java.time.Instant;

@Command(name = "serveur", description = "Afficher des informations sur le serveur")
public class ServerInfoCommand implements SlashCommandCreateListener {
    @Override
    public void onSlashCommandCreate(SlashCommandCreateEvent event) {
        SlashCommandInteraction interaction = event.getSlashCommandInteraction();
        if (interaction.getOptionByName("serveur").isEmpty()) return;

        if (interaction.getServer().isEmpty()) {
            Global.sendErrorMessage(interaction, "Cette commande n'est utilisable que dans un serveur");
            return;
        }

        Server server = interaction.getServer().get();

        int textChannelCount = server.getTextChannels().size();
        int voiceChannelCount = server.getVoiceChannels().size();
        int roleCount = server.getRoles().size();
        int memberCount = server.getMemberCount();

        Instant creation = server.getCreationTimestamp();
        User owner = server.getOwner().orElse(server.requestOwner().join());

        StringBuilder description = new StringBuilder();
        if (server.getDescription().isPresent())
            description.append(server.getDescription().get()).append("\n\n");

        description.append("\uD83D\uDE4D ").append(memberCount).append(" membres au total\n");

        if (server.getBoostCount() > 0)
            description.append("📈 Nitro niveau ")
                    .append(server.getBoostLevel().getId())
                    .append(" avec ")
                    .append(server.getBoostCount())
                    .append(" boosts\n");

        description.append("\uD83D\uDCDD ")
                .append(roleCount)
                .append(" rôles et ")
                .append(textChannelCount+voiceChannelCount)
                .append(" salons ")
                .append("( ")
                .append(textChannelCount)
                .append(" textuels et ")
                .append(voiceChannelCount)
                .append(" vocaux)\n");

        description.append("\uD83D\uDD10 Géré par ")
                .append(owner.getMentionTag())
                .append(" et créé le ")
                .append("<t:")
                .append(creation.getEpochSecond())
                .append(":D>\n\n");

        description.append("Emojis du serveur : ");

        for (KnownCustomEmoji emoji: server.getCustomEmojis())
            description.append(emoji.getMentionTag());

        EmbedBuilder embed = new EmbedBuilder()
                .setColor(Global.CYAN)
                .setDescription(description.toString());

        if (server.getIcon().isPresent())
            embed.setAuthor(server.getName() + " - " + server.getId(), "", server.getIcon().get());
        else
            embed.setAuthor(server.getName() + " - " + server.getId());

        Global.sendResponseEmbed(interaction, embed);
    }
}
