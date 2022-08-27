package commands.infos;

import framework.Command;
import framework.Parameter;
import org.javacord.api.entity.activity.Activity;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.entity.user.UserFlag;
import org.javacord.api.event.interaction.SlashCommandCreateEvent;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.interaction.SlashCommandOptionType;
import org.javacord.api.listener.interaction.SlashCommandCreateListener;
import ressources.Global;

import java.awt.*;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Command(name = "membre", description = "Afficher des informations sur un membre")
@Parameter(name = "mention", description = "Le membre dont tu veux les informations", type = SlashCommandOptionType.USER, isRequired = false)
public class UserInfoCommand implements SlashCommandCreateListener {
    @Override
    public void onSlashCommandCreate(SlashCommandCreateEvent event) {
        SlashCommandInteraction interaction = event.getSlashCommandInteraction();
        if (interaction.getOptionByName("membre").isEmpty()) return;

        User user = interaction.getOptionByName("membre").get().getOptionUserValueByName("mention").orElse(interaction.getUser());

        StringBuilder description = new StringBuilder();

        long joinedAt;
        long creation = user.getCreationTimestamp().getEpochSecond();
        description.append("\uD83D\uDCDD A créé son compte <t:").append(creation).append(":R>\n");

        if (interaction.getServer().isPresent()) {
            Server server = interaction.getServer().get();
            Optional<Instant> instant = user.getJoinedAtTimestamp(server);

            if (instant.isPresent()) {
                joinedAt = instant.get().getEpochSecond();
                description.append("⏱️ A rejoint le serveur <t:").append(joinedAt).append(":R>\n");
            }

             List<Role> roles = server.getRoles(user);

            description.append("\uD83D\uDCB3 Surnom : `").append(user.getDisplayName(server)).append("`\n");
            description.append("\uD83C\uDFF7️ Rôle principal : ").append(roles.get(roles.size()-1).getMentionTag()).append("\n");
        }

        for (UserFlag flag: user.getUserFlags()) {
            switch (flag) {
                case HOUSE_BALANCE -> description.append("\uD83D\uDEA9 Flag : Hypesquad Balance\n");
                case HOUSE_BRAVERY -> description.append("\uD83D\uDEA9 Flag : Hypesquad Bravery\n");
                case HOUSE_BRILLIANCE -> description.append("\uD83D\uDEA9 Flag : Hypesquad Brilliance\n");
            }
        }

        StringBuilder activities = new StringBuilder();
        for (Activity activity: user.getActivities()) {
            switch (activity.getType()) {
                case PLAYING -> activities.append("Joue à `").append(activity.getName()).append("`\n");
                case LISTENING -> activities.append("Ecoute `").append(activity.getDetails().orElse("")).append("`\n");
                case STREAMING -> activities.append("Stream sur ").append(activity.getName()).append(" : `")
                        .append(activity.getDetails().orElse("")).append("`\n");
            }
        }

        if (!activities.isEmpty()) {
            description.append("\n").append("\uD83C\uDFC3\u200D♂️ Activités :\n").append(activities);
        }

        String status = "";
        switch (user.getStatus()) {
            case IDLE -> status="Absent";
            case ONLINE -> status="En ligne";
            case OFFLINE -> status="Hors ligne";
            case INVISIBLE -> status="Invisible";
            case DO_NOT_DISTURB -> status="Ne pas déranger";
        }

        Global.sendResponseEmbed(
                interaction,
                new EmbedBuilder()
                        .setColor(Color.getColor("#1ABC9C"))
                        .setDescription(description.toString())
                        .setAuthor(user.getName() + " - " + status, "", user.getAvatar())
        );
    }
}
