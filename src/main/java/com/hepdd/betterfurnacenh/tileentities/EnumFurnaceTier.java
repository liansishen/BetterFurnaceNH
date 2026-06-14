package com.hepdd.betterfurnacenh.tileentities;

import com.hepdd.betterfurnacenh.Config;

public enum EnumFurnaceTier {

    IRON("container.bfnh.iron_furnace", "Iron Furnace"),
    GOLD("container.bfnh.gold_furnace", "Gold Furnace"),
    DIAMOND("container.bfnh.diamond_furnace", "Diamond Furnace");

    private final String containerName;
    private final String displayName;

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

    public float getSpeedMultiplier() {
        float mult = 1.0f;
        EnumFurnaceTier[] tiers = values();
        for (int i = 0; i <= ordinal(); i++) {
            mult *= tiers[i].getBaseSpeed();
        }
        return mult;
    }
}
