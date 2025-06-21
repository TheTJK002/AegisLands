package net.tjkraft.aegislands.block.blockEntity.custom;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AnchorTracker {
    private static final Map<UUID, Integer> anchorMap = new HashMap<>();

    public static int getAnchors(UUID playerId) {
        return anchorMap.getOrDefault(playerId, 0);
    }

    public static void increment(UUID playerId) {
        anchorMap.put(playerId, getAnchors(playerId) + 1);
    }

    public static void decrement(UUID playerId) {
        int current = getAnchors(playerId);
        if (current > 1) {
            anchorMap.put(playerId, current - 1);
        } else {
            anchorMap.remove(playerId);
        }
    }

    public static void reset(UUID playerId) {
        anchorMap.remove(playerId);
    }

    public static void clear() {
        anchorMap.clear();
    }
}
