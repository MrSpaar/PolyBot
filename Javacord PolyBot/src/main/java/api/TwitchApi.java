package api;

@SuppressWarnings("unused")
public class TwitchApi {
    private Stream[] data;
    private transient Object pagination;

    public Stream[] getData() {
        return data;
    }

    public static class Stream {
        private transient String broadcaster_language;
        private String broadcaster_login;
        private String display_name;
        private transient int game_id;
        private String game_name;
        private transient int id;
        private transient boolean is_live;
        private transient String[] tags_id;
        private transient String thumbnail_url;
        private String title;
        private transient String started_at;

        public String getDisplayName() {
            return display_name;
        }

        public String getTitle() {
            return title;
        }

        public String getGameName() {
            return game_name;
        }

        public String getStreamUrl() {
            return "https://www.twitch.tv/" + broadcaster_login;
        }
    }

    public static class Oauth {
        private double expires_in;
        private String access_token;
        private transient String token_type;

        public double getExpiresIn() {
            return expires_in;
        }

        public String getAccessToken() {
            return access_token;
        }
    }

    public boolean isEmpty() {
        return data.length == 0;
    }
}
