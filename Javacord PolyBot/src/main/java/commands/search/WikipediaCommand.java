package commands.search;

import api.WikipediaApi;
import com.google.gson.Gson;
import framework.Command;
import framework.Parameter;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.interaction.SlashCommandCreateEvent;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.listener.interaction.SlashCommandCreateListener;
import ressources.Global;
import ressources.RequestBuilder;

@Command(name = "wikipedia", description = "Rechercher un article Wikipedia")
@Parameter(name = "article", description = "Le nom de l'article à rechecher")
public class WikipediaCommand implements SlashCommandCreateListener {
    @Override
    public void onSlashCommandCreate(SlashCommandCreateEvent event) {
        SlashCommandInteraction interaction = event.getSlashCommandInteraction();
        if (!interaction.getCommandName().equals("wikipedia")) return;

        if (interaction.getOptionStringValueByName("article").isEmpty()) {
            Global.sendErrorMessage(interaction, "Tu n'as pas spécifié de ville");
            return;
        }

        String article = interaction.getOptionStringValueByName("article").get();

        String json = RequestBuilder.create()
                .setMethod("GET")
                .setUrl("https://fr.wikipedia.org/w/api.php?format=json&action=query&prop=extracts|pageimages&exintro&explaintext&redirects=1&titles="+article)
                .execute()
                .getResponseBody()
                .replaceFirst("\\{\"[0-9]+\":", "")
                .replaceFirst("\"}}", "\"}");

        WikipediaApi data = new Gson().fromJson(json, WikipediaApi.class);
        if (data.isEmpty()) {
            Global.sendErrorMessage(interaction, "Aucun article correspondant trouvé");
            return;
        }

        Global.sendResponseEmbed(
                interaction,
                new EmbedBuilder()
                        .setColor(Global.GRAY)
                        .setAuthor("Wikipedia - "+data.getTitle(), "", "https://i.imgur.com/nDTQgbf.png")
                        .setDescription(data.getExtract() + " [Lire l'article]("+data.getUrl()+")")
        );
    }
}
