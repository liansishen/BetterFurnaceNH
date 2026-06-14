package com.hepdd.betterfurnacenh;

import java.io.File;

import net.minecraftforge.common.config.Configuration;

public class Config {

    public static File configFile;

    public static float ironSpeedMultiplier = 1.5f;
    public static float goldSpeedMultiplier = 1.5f;
    public static float diamondSpeedMultiplier = 1.5f;

    public static int fluidTankCapacity = 8000;

    public static int lavaBurnPerBucket = 20000;
    public static int creosoteBurnPerBucket = 6400;

    public static void synchronizeConfiguration(File configFile) {
        Config.configFile = configFile;
        Configuration config = new Configuration(configFile);
        config.load();

        config.getCategory(Configuration.CATEGORY_GENERAL)
            .setLanguageKey("betterfurnacenh.config.category.general");

        ironSpeedMultiplier = config.getFloat(
            "ironSpeedMultiplier",
            Configuration.CATEGORY_GENERAL,
            1.5f,
            0.1f,
            100f,
            "betterfurnacenh.config.ironSpeedMultiplier.tooltip");

        goldSpeedMultiplier = config.getFloat(
            "goldSpeedMultiplier",
            Configuration.CATEGORY_GENERAL,
            1.5f,
            0.1f,
            100f,
            "betterfurnacenh.config.goldSpeedMultiplier.tooltip");

        diamondSpeedMultiplier = config.getFloat(
            "diamondSpeedMultiplier",
            Configuration.CATEGORY_GENERAL,
            1.5f,
            0.1f,
            100f,
            "betterfurnacenh.config.diamondSpeedMultiplier.tooltip");

        fluidTankCapacity = config.getInt(
            "fluidTankCapacity",
            Configuration.CATEGORY_GENERAL,
            8000,
            1000,
            32000,
            "betterfurnacenh.config.fluidTankCapacity.tooltip");

        lavaBurnPerBucket = config.getInt(
            "lavaBurnPerBucket",
            Configuration.CATEGORY_GENERAL,
            20000,
            1,
            1000000,
            "betterfurnacenh.config.lavaBurnPerBucket.tooltip");

        creosoteBurnPerBucket = config.getInt(
            "creosoteBurnPerBucket",
            Configuration.CATEGORY_GENERAL,
            6400,
            1,
            1000000,
            "betterfurnacenh.config.creosoteBurnPerBucket.tooltip");

        if (config.hasChanged()) {
            config.save();
        }
    }
}
