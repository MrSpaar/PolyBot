package commands.search;

import api.TwitchApi;
import com.google.gson.Gson;
import framework.Command;
import framework.Parameter;
import org.asynchttpclient.*;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.interaction.SlashCommandCreateEvent;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.listener.interaction.SlashCommandCreateListener;
import ressources.Global;
import ressources.RequestBuilder;

import java.time.Instant;
import java.util.Optional;

@Command(name = "twitch", description = "Rechercher des streams Twitch")
@Parameter(name = "categorie", description = "La catégorie des streams à rechecher")
@Parameter(name = "filtre", description = "Mot-clés pour filtrer les résultats", isRequired = false)
public class TwitchCommand implements SlashCommandCreateListener {
    private Instant expireIn;
    private String accessToken;

    @Override
    public void onSlashCommandCreate(SlashCommandCreateEvent event) {
        SlashCommandInteraction interaction = event.getSlashCommandInteraction();
        if (!interaction.getCommandName().equals("twitch")) return;

        if (interaction.getOptionStringValueByName("categorie").isEmpty()) {
            Global.sendErrorMessage(interaction, "Tu n'as pas spécifié de catégorie");
            return;
        }

        String category = interaction.getOptionStringValueByName("categorie").get();
        Optional<String> filter = interaction.getOptionStringValueByName("filtre");
        int limit = filter.isPresent() ? 100: 10;

        if (accessToken == null)
            updateAccessToken();
        else if (Instant.now().isAfter(expireIn))
            updateAccessToken();

        Response response = RequestBuilder.create()
                .setMethod("GET")
                .addHeader("Client-ID", Global.ENV.get("TWITCH_CLIENT"))
                .addHeader("Authorization", "Bearer " + accessToken)
                .setUrl("https://api.twitch.tv/helix/search/channels?query="+category+"&first="+limit+"&live_only=true")
                .execute();

        TwitchApi parsed = new Gson().fromJson(response.getResponseBody(), TwitchApi.class);

        if (parsed.isEmpty()) {
            Global.sendErrorMessage(interaction, "Aucun résultat trouvé");
            return;
        }

        TwitchApi.Stream[] streams = parsed.getData();
        EmbedBuilder embed = new EmbedBuilder()
                .setColor(Global.BLUE)
                .setAuthor("Twitch - " + streams[0].getGameName(), "", "https://i.imgur.com/gArdgyC.png");

        if (filter.isPresent()) {
            String[] filters = filter.get().split(" ");

            for (TwitchApi.Stream stream: streams) {
                if (anySeqInTitle(stream.getTitle(), filters)) {
                    embed.addField(stream.getDisplayName(), "["+stream.getTitle()+"]("+stream.getStreamUrl()+")", true);
                }
            }
        } else {
            for (TwitchApi.Stream stream: streams) {
                embed.addField(stream.getDisplayName(), "["+stream.getTitle()+"]("+stream.getStreamUrl()+")", true);
            }
        }

        Global.sendResponseEmbed(interaction, embed);
    }

    private void updateAccessToken() {
        String url = "https://id.twitch.tv/oauth2/token?client_id=" + Global.ENV.get("TWITCH_CLIENT")
                                                                    + "&client_secret="
                                                                    + Global.ENV.get("TWITCH_TOKEN")
                                                                    + "&grant_type=client_credentials";

        Response response = RequestBuilder.create()
                .setMethod("POST")
                .setUrl(url)
                .execute();

        TwitchApi.Oauth parsed = new Gson().fromJson(response.getResponseBody(), TwitchApi.Oauth.class);
        expireIn = Instant.now().plusSeconds((long) parsed.getExpiresIn());
        accessToken = parsed.getAccessToken();
    }

    private boolean anySeqInTitle(String title, String[] filters) {
        for (String filter: filters) {
            if (title.toLowerCase().contains(filter.toLowerCase())) return true;
        }
        return false;
    }
}
