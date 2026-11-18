package com.hepdd.betterfurnacenh.tileentities;

import com.hepdd.betterfurnacenh.Config;

public enum EnumFurnaceTier {

    IRON("container.bfnh.iron_furnace", "Iron Furnace"),
    GOLD("container.bfnh.gold_furnace", "Gold Furnace"),
    DIAMOND("container.bfnh.diamond_furnace", "Diamond Furnace");

    public static final EnumFurnaceTier[] VALUES = values();

    private final String containerName;
    private final String displayName;
    private float cachedSpeedMultiplier = -1f;

    EnumFurnaceTier(String containerName, String displayName) {
        this.containerName = containerName;
        this.displayName = displayName;
    }

    public String getContainerName() {
        return containerName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public float getBaseSpeed() {
        switch (this) {
            case IRON:
                return Config.ironSpeedMultiplier;
            case GOLD:
                return Config.goldSpeedMultiplier;
            case DIAMOND:
                return Config.diamondSpeedMultiplier;
            default:
                return 1.0f;
        }
    }

    public static void refreshCachedMultipliers() {
        float mult = 1.0f;
        for (EnumFurnaceTier tier : VALUES) {
            mult *= tier.getBaseSpeed();
            tier.cachedSpeedMultiplier = mult;
        }
    }

    public float getSpeedMultiplier() {
        if (cachedSpeedMultiplier < 0f) {
            refreshCachedMultipliers();
        }
        return cachedSpeedMultiplier;
    }
}
