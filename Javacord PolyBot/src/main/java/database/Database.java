package database;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.*;
import com.mongodb.client.model.UpdateOptions;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import ressources.Global;

import java.util.HashMap;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.set;
import static com.mongodb.client.model.Updates.inc;
import static com.mongodb.client.model.Updates.pull;
import static com.mongodb.client.model.Updates.combine;
import static com.mongodb.client.model.Updates.addToSet;
import static com.mongodb.client.model.Sorts.descending;
import static com.mongodb.client.model.Projections.include;

public class Database {
    private static MongoDatabase database;
    private static final HashMap<Long, SettingsEntry> cachedSettings = new HashMap<>();

    public static void initialize() {
        CodecRegistry registry = CodecRegistries.fromRegistries(
                MongoClientSettings.getDefaultCodecRegistry(),
                CodecRegistries.fromProviders(PojoCodecProvider.builder()
                        .register(MemberEntry.class, XpEntry.class, SettingsEntry.class, TempChannelEntry.class)
                        .build()
                )
        );

        MongoClientSettings settings = MongoClientSettings.builder()
                .codecRegistry(registry)
                .retryWrites(true)
                .applyConnectionString(new ConnectionString(Global.ENV.get("DB_URI")))
                .build();

        MongoClient client = MongoClients.create(settings);
        database = client.getDatabase("data");

        database.getCollection("setup", SettingsEntry.class)
                .find()
                .forEach(entry -> cachedSettings.put(entry.getId(), entry));
    }

    public static SettingsEntry getSettings(Server server) {
        return cachedSettings.get(server.getId());
    }

    public static void updateSettings(Server server, String key, long id) {
        cachedSettings.get(server.getId()).set(key, id);

        database.getCollection("setup", SettingsEntry.class)
                .updateOne(
                        eq("_id", server.getId()),
                        set(key, id)
                );
    }

    public static void updateWelcomeMessage(Server server, long id, String message) {
        cachedSettings.get(server.getId()).setWelcome(id, message);

        database.getCollection("setup", SettingsEntry.class)
                .updateOne(
                        eq("_id", server.getId()),
                        combine(
                                set("welcomeChannelId", id),
                                set("welcomeMessage", message)
                        )
                );
    }

    public static ServerTextChannel getLogsChannel(Server server) {
        SettingsEntry settings = getSettings(server);
        if (settings == null) return null;
        if (settings.getLogsChannelId() == 0) return null;

        return server.getTextChannelById(settings.getLogsChannelId()).orElse(null);
    }

    public static ServerTextChannel getAnnounceChannel(Server server) {
        SettingsEntry settings = getSettings(server);
        if (settings == null) return null;
        if (settings.getAnnounceChannelId() == 0) return null;

        return server.getTextChannelById(settings.getAnnounceChannelId()).orElse(null);
    }

    public static TempChannelEntry getTempChannel(ServerVoiceChannel channel) {
        return database.getCollection("pending", TempChannelEntry.class)
                .find(eq("vocId", channel.getId()))
                .first();
    }

    public static void insertTempChannel(User user, ServerVoiceChannel vChannel, ServerTextChannel tChannel) {
        database.getCollection("pending", TempChannelEntry.class)
                .insertOne(new TempChannelEntry(user, vChannel, tChannel));
    }

    public static void deleteTempChannel(TempChannelEntry entry) {
        database.getCollection("pending", TempChannelEntry.class)
                .deleteOne(new Document("_id", entry.getId()));
    }

    public static MemberEntry getUserServerEntry(Server server, User user) {
        return database.getCollection("members", MemberEntry.class)
                .find(and(
                        eq("_id", user.getId()),
                        eq("guilds.id", server.getId())
                ))
                .projection(include("guilds.$"))
                .first();
    }

    public static FindIterable<MemberEntry> getLeaderboard(Server server) {
        return database.getCollection("members", MemberEntry.class)
                .find(eq("guilds.id", server.getId()))
                .sort(descending("guilds.xp"));
    }

    public static void addServerToUser(Server server, User user) {
        database.getCollection("members", MemberEntry.class)
                .updateOne(
                        eq("_id", user.getId()),
                        addToSet("guilds", new XpEntry(server)),
                        new UpdateOptions().upsert(true)
                );
    }

    public static void removeServerFromUser(Server server, User user) {
        database.getCollection("members", MemberEntry.class)
                .updateOne(
                        eq("id", user.getId()),
                        pull("guilds.id", server.getId())
                );
    }

    public static void addXpToUser(Server server, User user, int amount, boolean shouldLevelUp) {
        database.getCollection("members", MemberEntry.class)
                .updateOne(
                        and(
                                eq("_id", user.getId()),
                                eq("guilds.id", server.getId())
                        ),
                        combine(
                                inc("guilds.$.xp", amount),
                                inc("guilds.$.level", shouldLevelUp ? 1: 0)
                        )
                );
    }
}