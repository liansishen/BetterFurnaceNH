package com.hepdd.betterfurnacenh;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

import com.hepdd.betterfurnacenh.client.gui.GuiBFFurnace;
import com.hepdd.betterfurnacenh.client.overlay.HopperGridOverlay;
import com.hepdd.betterfurnacenh.tileentities.TileEntityBFFurnace;

import cpw.mods.fml.common.event.FMLInitializationEvent;

public class ClientProxy extends CommonProxy {

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
        MinecraftForge.EVENT_BUS.register(new HopperGridOverlay());
    }

    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity te = world.getTileEntity(x, y, z);
        if (te instanceof TileEntityBFFurnace) {
            return new GuiBFFurnace(player.inventory, (TileEntityBFFurnace) te);
        }
        return null;
    }
}
