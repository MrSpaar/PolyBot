package database;

import java.util.List;

@SuppressWarnings("unused")
public class MemberEntry {
    private long id;
    private List<XpEntry> guilds;

    public MemberEntry() {}

    public long getId() {
        return id;
    }

    public List<XpEntry> getGuilds() {
        return guilds;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setGuilds(List<XpEntry> guilds) {
        this.guilds = guilds;
    }
}
