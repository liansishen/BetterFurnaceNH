package com.hepdd.betterfurnacenh.network;

import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;

public class BFNHNet {

    public static SimpleNetworkWrapper channel;

    public static void init() {
        channel = NetworkRegistry.INSTANCE.newSimpleChannel("bfnh");
        channel.registerMessage(PacketToggleHopper.Handler.class, PacketToggleHopper.class, 0, Side.SERVER);
    }
}
