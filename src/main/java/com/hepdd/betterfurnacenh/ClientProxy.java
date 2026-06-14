package com.hepdd.betterfurnacenh;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import com.hepdd.betterfurnacenh.client.gui.GuiBFFurnace;
import com.hepdd.betterfurnacenh.tileentities.TileEntityBFFurnace;

public class ClientProxy extends CommonProxy {

    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity te = world.getTileEntity(x, y, z);
        if (te instanceof TileEntityBFFurnace) {
            return new GuiBFFurnace(player.inventory, (TileEntityBFFurnace) te);
        }
        return null;
    }
}
