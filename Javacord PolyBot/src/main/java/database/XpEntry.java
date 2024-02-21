package database;

import org.bson.codecs.pojo.annotations.BsonProperty;
import org.javacord.api.entity.server.Server;

import java.time.Instant;

@SuppressWarnings("unused")
public class XpEntry {
    @BsonProperty("id")
    private long id;
    private int level;
    private long xp;
    private Instant cooldown;

    public XpEntry() {}

    public XpEntry(Server server) {
        this.id = server.getId();
        this.level = 0;
        this.xp = 0;
    }

    public long getId() {
        return id;
    }

    public int getLevel() {
        return level;
    }

    public long getXp() {
        return xp;
    }

    public Instant getCooldown() {
        return cooldown;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void setXp(long xp) {
        this.xp = xp;
    }

    public void setCooldown(Instant cooldown) {
        this.cooldown = cooldown;
    }
}
