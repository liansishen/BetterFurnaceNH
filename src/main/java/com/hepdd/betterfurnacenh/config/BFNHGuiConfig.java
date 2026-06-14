package com.hepdd.betterfurnacenh.config;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;

import com.hepdd.betterfurnacenh.BFNH;
import com.hepdd.betterfurnacenh.Config;

import cpw.mods.fml.client.config.GuiConfig;
import cpw.mods.fml.client.config.IConfigElement;

public class BFNHGuiConfig extends GuiConfig {

    private static Configuration config;

    @SuppressWarnings("rawtypes")
    public BFNHGuiConfig(GuiScreen parent) {
        super(parent, getConfigElements(), BFNH.MODID, false, false, "Better Furnace NH Configuration");
    }

    @SuppressWarnings("rawtypes")
    private static List<IConfigElement> getConfigElements() {
        List<IConfigElement> list = new ArrayList<>();
        config = new Configuration(Config.configFile);
        config.load();
        list.add(new ConfigElement(config.getCategory(Configuration.CATEGORY_GENERAL)));
        return list;
    }

    @Override
    public void onGuiClosed() {
        if (config.hasChanged()) {
            config.save();
        }
        Config.synchronizeConfiguration(Config.configFile);
        super.onGuiClosed();
    }
}
