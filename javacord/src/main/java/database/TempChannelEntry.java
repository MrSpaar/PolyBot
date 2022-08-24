package database;

import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.entity.user.User;

@SuppressWarnings("unused")
public class TempChannelEntry {
    private long id;
    private long guildId;
    private long vocId;
    private long txtId;

    public TempChannelEntry() {

    }

    public TempChannelEntry(User user, ServerVoiceChannel vChannel, ServerTextChannel tChannel) {
        this.id = user.getId();
        this.guildId = vChannel.getServer().getId();
        this.vocId = vChannel.getId();
        this.txtId = tChannel.getId();
    }

    public long getId() {
        return id;
    }

    public long getGuildId() {
        return guildId;
    }

    public long getVocId() {
        return vocId;
    }

    public long getTxtId() {
        return txtId;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setGuildId(long guildId) {
        this.guildId = guildId;
    }

    public void setVocId(long vocId) {
        this.vocId = vocId;
    }

    public void setTxtId(long txtId) {
        this.txtId = txtId;
    }
}
