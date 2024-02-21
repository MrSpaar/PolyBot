package api;

import com.google.gson.annotations.SerializedName;

import java.time.Instant;

@SuppressWarnings("ALL")
public class WeatherAPI {
    private Cast[] list;
    private transient String cod;
    private transient String message;
    private transient int cnt;
    private transient Object city;

    public static class Cast {
        private int dt;
        private Main main;
        private Wind wind;
        private Rain rain;
        private Weather[] weather;
        private String dt_txt;
        private transient Object clouds;
        private transient int visibility;
        private transient Object sys;

        private static class Main {
            private double temp;
            private int humidity;
            private transient double feels_like;
            private transient double temp_min;
            private transient double temp_max;
            private transient int pressure;
            private transient int sea_level;
            private transient int grnd_level;
            private transient int temp_kf;
        }

        private static class Weather {
            private String icon;
            private transient int id;
            private transient String main;
            private transient String description;
        }

        private static class Wind {
            private double speed;
            private transient int deg;
            private transient double gust;
        }

        private static class Rain {
            @SerializedName(value = "1h")
            private int _1h;
            private transient int _3h;
        }

        public Instant getDt() {
            return Instant.ofEpochSecond(dt);
        }

        public String getHour() {
            return dt_txt.substring(10);
        }

        public double getTemp() {
            return main.temp;
        }
    }

    public boolean isEmpty() {return list == null;}

    public Cast[] getList() {
        return list;
    }

    public String getIconCode() {
        return list[0].weather[0].icon;
    }

    public String getIcon() {
        return "https://openweathermap.org/img/w/" + getIconCode() + ".png";
    }

    public String getWindSpeed() {
        return list[0].wind.speed + " km/h";
    }

    public String getHumidity() {
        return list[0].main.humidity + "%";
    }

    public String getCurrentTemp() {
        return list[0].main.temp + "°C";
    }
}
