package ressources;

import io.github.cdimascio.dotenv.Dotenv;
import org.javacord.api.entity.message.MessageFlag;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.interaction.SlashCommandInteraction;

import java.awt.*;
import java.util.Random;

public class Global {
    private static final Random random = new Random();
    public static final Dotenv ENV = Dotenv.load();

    public static final Color BLUE = new Color(52, 152, 219);
    public static final Color CYAN = new Color(52, 110, 122);
    public static final Color GREEN = new Color(46, 204, 113);
    public static final  Color GRAY = new Color(84, 110, 122);
    public static final Color LIGHT_GRAY = new Color(153, 170, 181);
    public static final Color RED = new Color(231, 76, 60);
    public static final Color ORANGE = new Color(194, 124, 14);
    public static final Color YELLOW = new Color(254, 202, 87);
    public static final Color GOLD = new Color(241, 196, 15);

    public static void sendErrorMessage(SlashCommandInteraction interaction, String message) {
        EmbedBuilder embed = new EmbedBuilder()
                .setColor(Color.RED)
                .setDescription("❌ " + message);

        sendResponseEmbed(interaction, embed, true);
    }

    public static void sendResponseEmbed(SlashCommandInteraction interaction, EmbedBuilder embed) {
        sendResponseEmbed(interaction, embed, false);
    }

    public static void sendResponseEmbed(SlashCommandInteraction interaction, EmbedBuilder embed, boolean ephemeral) {
        if (ephemeral) {
            interaction.createImmediateResponder()
                    .setFlags(MessageFlag.EPHEMERAL)
                    .addEmbed(embed)
                    .respond();
        } else {
            interaction.createImmediateResponder()
                    .addEmbed(embed)
                    .respond();
        }
    }

    public static int randInt(int limit) {
        return random.nextInt(limit+1);
    }

    public static int randInt(int a, int b) {
        return random.nextInt(a, b+1);
    }

    public static Color randColor() {
        return new Color((int)(Math.random()*0x1000000));
    }

    public static boolean isUrl(String arg) {
        return arg.startsWith("https://");
    }
}
