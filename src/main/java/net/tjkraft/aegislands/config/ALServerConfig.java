package net.tjkraft.aegislands.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class ALServerConfig {
    public static final ForgeConfigSpec SERVER_CONFIG;
    public static final ForgeConfigSpec.IntValue CLAIM_EXTENSION_TIME;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.push("claim");
        CLAIM_EXTENSION_TIME = builder
                .comment("Seconds of protection added when inserting an extension item")
                .defineInRange("claimExtensionTime", 3600, 60, 86400);

        builder.pop();
        SERVER_CONFIG = builder.build();
    }
}
