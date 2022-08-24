package api;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@SuppressWarnings("unused")
public class AnimeApi {
    private Kitsu[] data;
    private transient Object meta;
    private transient Object links;

    public static class Kitsu {
        private Attributes attributes;
        private transient String id;
        private transient String type;
        private transient Object links;
        private transient Object relationships;

        private static class Attributes {
            private String synopsis;
            private String canonicalTitle;
            private String startDate;
            private String endDate;
            private Image posterImage;
            private double averageRating;
            private int episodeCount;
            private int totalLength;
            private transient String createdAt;
            private transient String updatedAt;
            private transient String slug;
            private transient String description;
            private transient int coverImageOffset;
            private transient Object titles;
            private transient String[] abbreviatedTitles;
            private transient int[] ratingFrequencies;
            private transient int userCount;
            private transient int favoritesCount;
            private transient int popularityRank;
            private transient int ratingRank;
            private transient String ageRating;
            private transient String ageRatingGuide;
            private transient String nextRelease;
            private transient String subtype;
            private transient String status;
            private transient String tba;
            private transient Image coverImage;
            private transient int episodeLength;
            private transient String youtubeVideoId;
            private transient boolean nsfw;
        }

        private static class Image {
            private String tiny;
            private transient String large;
            private transient String small;
            private transient String medium;
            private transient String original;
            private transient Object meta;
        }

        private String getSynopsis() {return attributes.synopsis.substring(0, attributes.synopsis.length()-24);}
        private String getCanonicalTitle() {return attributes.canonicalTitle;}
        private String getPosterImage() {return attributes.posterImage.tiny;}
        private String getStartDate() {return attributes.endDate;}
        private String getEndDate() {return attributes.endDate;}
        private double getAverageRating() {return attributes.averageRating;}
        private int getEpisodeCount() {return attributes.episodeCount;}
        private int getTotalLength() {return attributes.totalLength;}
    }

    public boolean isEmpty() {
        return data.length == 0;
    }

    public String getName() {
        return data[0].getCanonicalTitle();
    }

    public String getSynopsis() {return data[0].getSynopsis();}

    public String getPosterImage() {return data[0].getPosterImage();}

    private String formatDate(String stringDate) {
        LocalDate date = LocalDate.parse(stringDate);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return formatter.format(date);
    }

    public String getDiffusionPeriod() {
        String endDate = data[0].getEndDate().equals("") ? "En cours": formatDate(data[0].getEndDate());
        String startDate = formatDate(data[0].getStartDate());

        return startDate + " → " + endDate;
    }

    public int getEpisodeCount() {
        return data[0].getEpisodeCount();
    }

    public double getAverageRating() {
        return data[0].getAverageRating();
    }

    public String getTotalLength() {
        int hours = data[0].getTotalLength()/3600;

        if (hours > 24) {
            int days = hours/24;
            return days + "j" + (hours-days*24) + "h";
        }

        return hours + "h";
    }
}
