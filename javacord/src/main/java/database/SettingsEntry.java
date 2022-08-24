package database;

@SuppressWarnings("unused")
public class SettingsEntry {
    private long id;
    private long announceChannelId;
    private long logsChannelId;
    private long newcomerRoleId;
    private long welcomeChannelId;
    private String welcomeText;

    public SettingsEntry() {}

    public long getId() {
        return id;
    }

    public long getAnnounceChannelId() {
        return announceChannelId;
    }

    public long getLogsChannelId() {
        return logsChannelId;
    }

    public long getNewcomerRoleId() {
        return newcomerRoleId;
    }

    public long getWelcomeChannelId() {
        return welcomeChannelId;
    }

    public String getWelcomeText() {
        return welcomeText;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setAnnounceChannelId(final long announceChannelId) {
        this.announceChannelId = announceChannelId;
    }

    public void setLogsChannelId(final long logsChannelId) {
        this.logsChannelId = logsChannelId;
    }

    public void  setNewcomerRoleId(final long newcomerRoleId) {
        this.newcomerRoleId = newcomerRoleId;
    }

    public void setWelcomeChannelId(final long welcomeChannelId) {
        this.welcomeChannelId = welcomeChannelId;
    }

    public void setWelcomeText(final String welcomeText) {
        this.welcomeText = welcomeText;
    }

    public void set(String fieldName, long fieldValue) {
        try {
            this.getClass().getDeclaredField(fieldName).set(this, fieldValue);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public void setWelcome(long id, String message) {
        this.welcomeText = message;
        this.welcomeChannelId = id;
    }
}
