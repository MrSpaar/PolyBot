package audio;

import org.javacord.api.entity.channel.ServerVoiceChannel;

import java.util.HashMap;

public class AudioHandler {
    private static final HashMap<Long, ServerNode> nodeMap = new HashMap<>();

    public static ServerNode getOrCreateNode(ServerVoiceChannel channel) {
        return nodeMap.computeIfAbsent(channel.getServer().getId(), nm -> new ServerNode(channel));
    }

    public static ServerNode getNode(ServerVoiceChannel channel) {
        return nodeMap.get(channel.getId());
    }

    public static void destroyNode(ServerNode node) {
        node.disconnect();
        nodeMap.remove(node.getId());
    }
}
