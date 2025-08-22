package net.tjkraft.aegislands.config;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

public class ALServerConfig {
    public static final ForgeConfigSpec SERVER_CONFIG;

    public static final ForgeConfigSpec.IntValue MIN_ANCHOR_Y;
    public static final ForgeConfigSpec.IntValue MAX_ANCHOR_Y;
    public static final ForgeConfigSpec.IntValue LIMIT_CLAIM_ANCHOR_PER_PLAYER;
    public static final ForgeConfigSpec.IntValue MIN_CLAIM_CHUNK_DISTANCE;
    public static final ForgeConfigSpec.IntValue CLAIM_TIME;
    public static final ForgeConfigSpec.ConfigValue<String> PAYMENT_CLAIM;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> CLAIM_ALLOWED_BLOCKS;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> CLAIM_ALLOWED_ENTITIES;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.push("Aegis Lands Settings");
        MIN_ANCHOR_Y = builder
                .comment("Choose the depth of the Claim Anchor")
                .defineInRange("min_anchor_y", 16, 1, 64);

        MAX_ANCHOR_Y = builder
                .comment("Choose the height of the Claim Anchor")
                .defineInRange("max_anchor_y", 16, 1, 64);

        LIMIT_CLAIM_ANCHOR_PER_PLAYER = builder
                .comment("Limit of Claim Anchor that the player can place")
                .defineInRange("limit_claim_anchor_per_player", 4, 1, Integer.MAX_VALUE);

        MIN_CLAIM_CHUNK_DISTANCE = builder
                .comment("Distance between claims between different owners (in chunks)")
                .defineInRange("min_claim_chunk_distance", 3, 1, 64);

        PAYMENT_CLAIM = builder
                .comment("Choose which object should be used to add time to the claim")
                .define("payment_claim", "minecraft:clock");

        CLAIM_TIME = builder
                .comment("Choose the time of payment for the item (20 tick = 1 seconds)")
                .comment("600 = 10 minutes")
                .defineInRange("claim_time", 600, 1, Integer.MAX_VALUE);


        CLAIM_ALLOWED_BLOCKS = builder
                .comment("Blocks that can be interacted with in the claim")
                .defineListAllowEmpty("claim_anchor_allowed_blocks",
                        List.of("minecraft:crafting_table"),
                        o -> o instanceof String);

        CLAIM_ALLOWED_ENTITIES = builder
                .comment("Entities that can be interacted with in the claim")
                .defineListAllowEmpty("claim_anchor_allowed_entities",
                        List.of("minecraft:boat", "minecraft:minecart"),
                        o -> o instanceof String);
        builder.pop();

        SERVER_CONFIG = builder.build();
    }
}
