package commands.games;

import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.interaction.callback.InteractionOriginalResponseUpdater;
import org.javacord.api.listener.message.MessageCreateListener;
import ressources.Global;

import java.text.Normalizer;
import java.util.ArrayList;

public class HangmanListener implements MessageCreateListener {
    private int lives;
    private final String word;
    private final String normalized;
    private final boolean[] discovered;
    private final ArrayList<String> errors;
    private final SlashCommandInteraction interaction;
    private final InteractionOriginalResponseUpdater updater;

    public HangmanListener(SlashCommandInteraction interaction, InteractionOriginalResponseUpdater updater, String word) {
        this.interaction = interaction;
        this.updater = updater;
        this.word = word;

        this.lives = 5;
        this.errors = new ArrayList<>();
        this.discovered = new boolean[word.length()];
        this.normalized = Normalizer.normalize(word, Normalizer.Form.NFKD);
    }

    @Override
    public void onMessageCreate(MessageCreateEvent event) {
        if (!event.getMessageAuthor().isUser() || event.getMessageAuthor().getId() != interaction.getUser().getId()) return;

        String guess = event.getMessageContent();
        event.deleteMessage();

        if (errors.contains(guess)) return;
        if (lives == 1) {
            event.getApi().removeListener(this);
            updater.removeAllEmbeds().addEmbed(
                    new EmbedBuilder()
                            .setColor(Global.RED)
                            .setDescription("Perdu ! Le mot était `" + word + "`")
            ).update();
            return;
        }

        if (!guess.equals(normalized) && !normalized.contains(guess)) {
            lives--;
            errors.add(guess);
            updateEmbed();
            return;
        }

        String updated = updateWord(guess);

        if (guess.length() > 1 || updated.equals(normalized)) {
            event.getApi().removeListener(this);
            updater.removeAllEmbeds().addEmbed(
                    new EmbedBuilder()
                            .setColor(Global.GOLD)
                            .setDescription("Bravo, tu as gagné ! :)")
            ).update();
            return;
        }

        updateEmbed();
    }

    private String updateWord(String guess) {
        StringBuilder builder = new StringBuilder();

        for (int i=0; i<word.length(); i++) {
            if (discovered[i]) {
                builder.append(word.charAt(i));
            } else if (guess.charAt(0) == word.charAt(i)) {
                discovered[i] = true;
                builder.append(word.charAt(0));
            } else {
                builder.append("-");
            }
        }

        return builder.toString();
    }

    private void updateEmbed() {
        StringBuilder builder = new StringBuilder();

        for (int i=0; i<word.length(); i++) {
            if (discovered[i]) builder.append(word.charAt(i));
            else builder.append("-");
        }

        updater.removeAllEmbeds().addEmbed(
                new EmbedBuilder()
                        .setTitle("Partie de pendu")
                        .addField("Mot : ", "```" + builder + "```")
                        .addField("Erreurs", "```" + String.join(", ", errors) + "```")
                        .setFooter("Vies : " + lives)
                        .setColor(Global.randColor())
        ).update();
    }
}
