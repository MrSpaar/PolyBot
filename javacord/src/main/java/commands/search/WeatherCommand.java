package commands.search;

import api.WeatherAPI;
import com.google.gson.Gson;
import framework.Command;
import framework.Parameter;
import org.asynchttpclient.Response;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.interaction.SlashCommandCreateEvent;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.listener.interaction.SlashCommandCreateListener;
import ressources.Global;
import ressources.RequestBuilder;

@Command(name = "meteo", description = "Rechercher la météo actuelle d'une ville")
@Parameter(name = "ville", description = "La ville dont tu veux la météo")
public class WeatherCommand implements SlashCommandCreateListener {
    @Override
    public void onSlashCommandCreate(SlashCommandCreateEvent event) {
        SlashCommandInteraction interaction = event.getSlashCommandInteraction();
        if (!interaction.getCommandName().equals("meteo")) return;

        if (interaction.getOptionStringValueByName("ville").isEmpty()) {
            Global.sendErrorMessage(interaction, "Tu n'as pas spécifié de ville");
            return;
        }

        String city = interaction.getOptionStringValueByName("ville").get();

        Response response = RequestBuilder.create()
                .setMethod("GET")
                .setUrl("https://api.openweathermap.org/data/2.5/forecast?q="+city+"&units=metric&APPID="+Global.ENV.get("WEATHER_TOKEN"))
                .execute();

        WeatherAPI data = new Gson().fromJson(response.getResponseBody(), WeatherAPI.class);

        if (data.isEmpty()) {
            Global.sendErrorMessage(interaction, "Aucune ville correspondante trouvée");
            return;
        }

        String weather = switch (data.getIconCode()) {
            case "01d" -> "ensoleillé";
            case "02d" -> "peu nuageux";
            case "03d" -> "nuageux";
            case "04d" -> "nuages gris";
            case "09d" -> "très pluivieux";
            case "10d" -> "pluivieux";
            case "11d" -> "orageux";
            case "13d" -> "neigeux";
            default -> "brumeux";
        };

        String description = "Le temps est actuellement " + weather + ".\n\n" +
        "\uD83C\uDF21️ Température : " + data.getCurrentTemp() + "\n" +
                "\uD83D\uDCA7 Humidité : " + data.getHumidity() + "\n" +
                "\uD83C\uDF2C️ Vent : " + data.getWindSpeed() + "\n";

        EmbedBuilder embed = new EmbedBuilder()
                .setColor(Global.BLUE)
                .setTitle("\uD83D\uDD0E Météo à "+city)
                .setDescription(description)
                .setThumbnail(data.getIcon());

        Global.sendResponseEmbed(interaction, embed);
    }
}
