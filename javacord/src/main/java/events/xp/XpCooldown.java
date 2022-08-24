package events.xp;

import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;

import java.time.Instant;
import java.util.HashMap;

public class XpCooldown {
    private static final HashMap<Long, HashMap<Long, Instant>> userMap = new HashMap<>();

    public static boolean isOnCooldown(Server server, User user) {
        HashMap<Long, Instant> serverMap = userMap.computeIfAbsent(user.getId(), m -> new HashMap<>());

        Instant now = Instant.now();
        Instant cooldown = serverMap.get(server.getId());

        if (cooldown == null) {
            serverMap.put(server.getId(), now.plusSeconds(60));
            return false;
        }

        if (cooldown.isBefore(now)) {
            serverMap.remove(server.getId());
            return false;
        }

        return true;
    }
}
