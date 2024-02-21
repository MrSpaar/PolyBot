package commands.search;

import api.AnimeApi;
import com.google.gson.GsonBuilder;
import framework.Command;
import framework.Parameter;
import org.asynchttpclient.Response;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.interaction.SlashCommandCreateEvent;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.listener.interaction.SlashCommandCreateListener;
import ressources.Global;
import ressources.RequestBuilder;

@Command(name = "anime", description = "Rechercher un anime")
@Parameter(name = "nom", description = "Le nom de l'anime à rechecher")
public class AnimeCommand implements SlashCommandCreateListener {
    @Override
    public void onSlashCommandCreate(SlashCommandCreateEvent event) {
        SlashCommandInteraction interaction = event.getSlashCommandInteraction();
        if (!interaction.getCommandName().equals("anime")) return;

        if (interaction.getOptionStringValueByName("nom").isEmpty()) {
            Global.sendErrorMessage(interaction, "Tu n'as pas spécifié d'anime à rechercher");
            return;
        }

        String name = interaction.getOptionStringValueByName("nom").get();

        Response response = RequestBuilder.create()
                .setMethod("GET")
                .setUrl("https://kitsu.io/api/edge/anime?filter[text]="+name)
                .execute();

        AnimeApi data = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd")
                .create()
                .fromJson(response.getResponseBody(), AnimeApi.class);

        if (data.isEmpty()) {
            Global.sendErrorMessage(interaction, "Aucun résultat trouvé");
            return;
        }

        Global.sendResponseEmbed(
                interaction,
                new EmbedBuilder()
                        .setColor(Global.GRAY)
                        .setDescription(data.getSynopsis())
                        .addField("🥇 Score", ""+data.getAverageRating(), true)
                        .addField("🖥️ Épisodes", data.getEpisodeCount()+" ("+ data.getTotalLength(), true)
                        .addField("\uD83D\uDCC5 Diffusion", data.getDiffusionPeriod(), true)
                        .setAuthor("Anime - "+data.getName(), "", data.getPosterImage())
        );
    }
}
