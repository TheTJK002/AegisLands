package net.tjkraft.aegislands.world;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

import java.util.*;

public class ClaimManager extends SavedData {
    private final Map<ChunkPos, UUID> claimedChunks = new HashMap<>();
    private final Map<ChunkPos, Set<String>> trustedPlayers = new HashMap<>();
    private final Map<ChunkPos, Set<String>> trustedEntities = new HashMap<>();
    private final Map<ChunkPos, Set<String>> trustedBlocks = new HashMap<>();

    public ClaimManager() {}

    public static ClaimManager get(LevelAccessor level) {
        if (level.isClientSide()) throw new RuntimeException("Impossibile accedere al ClaimManager dal Client!");
        ServerLevel serverLevel = (ServerLevel) level;
        DimensionDataStorage storage = serverLevel.getServer().overworld().getDataStorage();
        return storage.computeIfAbsent(ClaimManager::load, ClaimManager::new, "custom_claim_manager");
    }

    public boolean isChunkClaimed(ChunkPos pos) {
        return claimedChunks.containsKey(pos);
    }

    public UUID getOwner(ChunkPos pos) {
        return claimedChunks.get(pos);
    }

    // CORREZIONE: Controlla se l'utente è l'owner, o se il suo UUID/Nome è presente nella lista Trusted
    public boolean isTrusted(ChunkPos pos, UUID playerUuid, String playerName) {
        if (!isChunkClaimed(pos)) return true;
        if (getOwner(pos).equals(playerUuid)) return true;

        if (!trustedPlayers.containsKey(pos)) return false;

        Set<String> trusted = trustedPlayers.get(pos);
        return trusted.contains(playerUuid.toString()) || trusted.contains(playerName.toLowerCase());
    }

    public void claimChunk(ChunkPos pos, UUID ownerUuid) {
        claimedChunks.put(pos, ownerUuid);
        trustedPlayers.put(pos, new HashSet<>());
        trustedEntities.put(pos, new HashSet<>());
        trustedBlocks.put(pos, new HashSet<>());
        setDirty();
    }

    public void unclaimChunk(ChunkPos pos) {
        claimedChunks.remove(pos);
        trustedPlayers.remove(pos);
        trustedEntities.remove(pos);
        trustedBlocks.remove(pos);
        setDirty();
    }

    public void addClaimEntry(ChunkPos pos, String entry, int type) {
        if (!isChunkClaimed(pos)) return;

        if (type == 0) {
            trustedPlayers.computeIfAbsent(pos, k -> new HashSet<>()).add(entry.toLowerCase());
            setDirty();
        } else if (type == 1) {
            trustedEntities.computeIfAbsent(pos, k -> new HashSet<>()).add(entry);
            setDirty();
        } else if (type == 2) {
            trustedBlocks.computeIfAbsent(pos, k -> new HashSet<>()).add(entry);
            setDirty();
        }
    }

    public void removeClaimEntry(ChunkPos pos, String entry, int type) {
        if (!isChunkClaimed(pos)) return;

        if (type == 0) {
            if (trustedPlayers.containsKey(pos)) {
                trustedPlayers.get(pos).remove(entry.toLowerCase());
                setDirty();
            }
        } else if (type == 1) {
            if (trustedEntities.containsKey(pos)) {
                trustedEntities.get(pos).remove(entry);
                setDirty();
            }
        } else if (type == 2) {
            if (trustedBlocks.containsKey(pos)) {
                trustedBlocks.get(pos).remove(entry);
                setDirty();
            }
        }
    }

    public List<String> getClaimEntries(ChunkPos pos, int type) {
        List<String> entries = new ArrayList<>();
        if (!isChunkClaimed(pos)) return entries;

        if (type == 0) {
            Set<String> players = trustedPlayers.get(pos);
            if (players != null) entries.addAll(players);
        } else if (type == 1) {
            Set<String> entities = trustedEntities.get(pos);
            if (entities != null) entries.addAll(entities);
        } else if (type == 2) {
            Set<String> blocks = trustedBlocks.get(pos);
            if (blocks != null) entries.addAll(blocks);
        }
        return entries;
    }

    public static ClaimManager load(CompoundTag nbt) {
        ClaimManager manager = new ClaimManager();
        ListTag list = nbt.getList("Claims", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag tag = list.getCompound(i);
            ChunkPos pos = new ChunkPos(tag.getInt("X"), tag.getInt("Z"));
            UUID owner = tag.getUUID("Owner");
            manager.claimedChunks.put(pos, owner);

            Set<String> trusted = new HashSet<>();
            ListTag trustedList = tag.getList("Trusted", Tag.TAG_STRING);
            for (int j = 0; j < trustedList.size(); j++) {
                trusted.add(trustedList.getString(j));
            }
            manager.trustedPlayers.put(pos, trusted);

            Set<String> entities = new HashSet<>();
            ListTag entityList = tag.getList("TrustedEntities", Tag.TAG_STRING);
            for (int j = 0; j < entityList.size(); j++) {
                entities.add(entityList.getString(j));
            }
            manager.trustedEntities.put(pos, entities);

            Set<String> blocks = new HashSet<>();
            ListTag blockList = tag.getList("TrustedBlocks", Tag.TAG_STRING);
            for (int j = 0; j < blockList.size(); j++) {
                blocks.add(blockList.getString(j));
            }
            manager.trustedBlocks.put(pos, blocks);
        }
        return manager;
    }

    @Override
    public CompoundTag save(CompoundTag nbt) {
        ListTag list = new ListTag();
        for (Map.Entry<ChunkPos, UUID> entry : claimedChunks.entrySet()) {
            CompoundTag tag = new CompoundTag();
            tag.putInt("X", entry.getKey().x);
            tag.putInt("Z", entry.getKey().z);
            tag.putUUID("Owner", entry.getValue());

            ListTag trustedList = new ListTag();
            Set<String> trusted = trustedPlayers.get(entry.getKey());
            if (trusted != null) {
                for (String s : trusted) {
                    trustedList.add(net.minecraft.nbt.StringTag.valueOf(s));
                }
            }
            tag.put("Trusted", trustedList);

            ListTag entityList = new ListTag();
            Set<String> entities = trustedEntities.get(entry.getKey());
            if (entities != null) {
                for (String s : entities) {
                    entityList.add(net.minecraft.nbt.StringTag.valueOf(s));
                }
            }
            tag.put("TrustedEntities", entityList);

            ListTag blockList = new ListTag();
            Set<String> blocks = trustedBlocks.get(entry.getKey());
            if (blocks != null) {
                for (String s : blocks) {
                    blockList.add(net.minecraft.nbt.StringTag.valueOf(s));
                }
            }
            tag.put("TrustedBlocks", blockList);

            list.add(tag);
        }
        nbt.put("Claims", list);
        return nbt;
    }
}