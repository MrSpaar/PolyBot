package commands.misc;

import framework.Command;
import framework.Parameter;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.interaction.SlashCommandCreateEvent;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.listener.interaction.SlashCommandCreateListener;
import ressources.Global;


import java.util.Optional;

@Command(name = "sondage", description = "Faire un sondage (9 réponses maximales)")
@Parameter(name = "texte", description = "Le texte au format Question | Réponse 1 | Réponse 2 | ...")
public class PollCommand implements SlashCommandCreateListener {
    @Override
    public void onSlashCommandCreate(SlashCommandCreateEvent event) {
        SlashCommandInteraction interaction = event.getSlashCommandInteraction();
        if (!interaction.getCommandName().equals("sondage")) return;

        Optional<String> arg = interaction.getOptionStringValueByName("texte");
        if (arg.isEmpty()) {
            Global.sendErrorMessage(interaction, "Aucun texte fourni");
            return;
        }

        User author = interaction.getUser();
        Optional<Server> server = interaction.getServer();
        
        if (server.isEmpty()) {
            Global.sendErrorMessage(interaction, "Cette commande n'est utilisable que dans un serveur");
            return;
        }

        String[] items = arg.get().split("\\|");
        for (int i=0; i<items.length; i++) {
            items[i] = items[i].strip();
        }
        
        String[] reactions = {"1️⃣", "2️⃣", "3️⃣", "4️⃣", "5️⃣", "6️⃣", "7️⃣", "8️⃣", "9️⃣"};

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle(">> " + items[0])
                .setColor(Global.BLUE)
                .setAuthor("Sondage de " + author.getDisplayName(server.get()), "", author.getAvatar());

        for (int i=1; i<items.length; i++) {
            embed.addField(reactions[i-1] + " Option N°" + i, "```" + items[i] + "```");
        }

        interaction.createImmediateResponder()
                .addEmbed(embed)
                .respond()
                .thenAccept(updater -> updater.update().thenAccept(message -> {
                    for (int i=0; i<items.length-1; i++) {
                        message.addReaction(reactions[i]);
                    }
                }));
    }
}
