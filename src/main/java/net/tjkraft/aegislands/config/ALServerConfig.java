package net.tjkraft.aegislands.config;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

public class ALServerConfig {
    public static final ForgeConfigSpec SERVER_CONFIG;

    public static final ForgeConfigSpec.IntValue MAX_ANCHOR_PER_PLAYER;
    public static final ForgeConfigSpec.IntValue CLAIM_DURATION_MINUTES;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> PAYMENT_CLAIM;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.push("Aegis Anchor Settings");
        MAX_ANCHOR_PER_PLAYER = builder
                .comment("Aegis Anchor per player")
                .defineInRange("max_anchor_per_player", 3, 1, Integer.MAX_VALUE);

        CLAIM_DURATION_MINUTES = builder
                .comment("Durata in minuti del claim prima che scada (senza ricarica)")
                .defineInRange("anchor.duration_minutes", 60, 1, 1440);

        PAYMENT_CLAIM = builder
                .comment("Oggetti che possono essere inseriti per estendere la durata del claim")
                .defineListAllowEmpty("anchor.fuel_items",
                        List.of("minecraft:clock"),
                        o -> o instanceof String);
        builder.pop();

        SERVER_CONFIG = builder.build();
    }
}
