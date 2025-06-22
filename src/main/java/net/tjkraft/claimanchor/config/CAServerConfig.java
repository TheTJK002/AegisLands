package net.tjkraft.claimanchor.config;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

public class CAServerConfig {
    public static final ForgeConfigSpec SERVER_CONFIG;

    public static final ForgeConfigSpec.IntValue MIN_ANCHOR_Y;
    public static final ForgeConfigSpec.IntValue MAX_ANCHOR_Y;
    public static final ForgeConfigSpec.IntValue MAX_ANCHOR_PER_PLAYER;
    public static final ForgeConfigSpec.IntValue CLAIM_DURATION_MINUTES;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> PAYMENT_CLAIM;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> CLAIM_ALLOWED_BLOCKS;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> CLAIM_ALLOWED_ENTITIES;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.push("Claim Anchor Settings");
        MIN_ANCHOR_Y = builder
                .comment("Scegli la profondità del Claim Anchor")
                .defineInRange("min_anchor_y", 16, 1, 64);

        MAX_ANCHOR_Y = builder
                .comment("Scegli l'altezza del Claim Anchor")
                .defineInRange("max_anchor_y", 16, 1, 64);

        MAX_ANCHOR_PER_PLAYER = builder
                .comment("Claim Anchor per player")
                .defineInRange("max_claim_anchor_per_player", 3, 1, Integer.MAX_VALUE);

        CLAIM_DURATION_MINUTES = builder
                .comment("Durata in minuti del claim prima che scada (senza ricarica)")
                .defineInRange("claim_anchor_duration_minutes", 300, 1, Integer.MAX_VALUE);

        PAYMENT_CLAIM = builder
                .comment("Oggetti che possono essere inseriti per estendere la durata del claim")
                .defineListAllowEmpty("claim_anchor_fuel_items",
                        List.of("minecraft:clock"),
                        o -> o instanceof String);

        CLAIM_ALLOWED_BLOCKS = builder
                .comment("Blocchi che si può interagire dentro al chunk")
                .defineListAllowEmpty("claim_anchor_allowed_blocks",
                        List.of("minecraft:crafting_table"),
                        o -> o instanceof String);

        CLAIM_ALLOWED_ENTITIES = builder
                .comment("Entità che si possono interagire dentro al chunk")
                .defineListAllowEmpty("claim_anchor_allowed_entities",
                        List.of("minecraft:boats"),
                        o -> o instanceof String);
        builder.pop();

        SERVER_CONFIG = builder.build();
    }
}
