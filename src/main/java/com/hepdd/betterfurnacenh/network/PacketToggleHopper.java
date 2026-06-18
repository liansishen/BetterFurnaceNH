package com.hepdd.betterfurnacenh.network;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import com.hepdd.betterfurnacenh.tileentities.TileEntityBFFurnace;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

public class PacketToggleHopper implements IMessage {

    private int x;
    private int y;
    private int z;
    private boolean top;

    public PacketToggleHopper() {}

    public PacketToggleHopper(int x, int y, int z, boolean top) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.top = top;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeBoolean(top);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        top = buf.readBoolean();
    }

    public static class Handler implements IMessageHandler<PacketToggleHopper, IMessage> {

        @Override
        public IMessage onMessage(PacketToggleHopper message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            World world = player.worldObj;
            TileEntity te = world.getTileEntity(message.x, message.y, message.z);
            if (te instanceof TileEntityBFFurnace) {
                TileEntityBFFurnace furnace = (TileEntityBFFurnace) te;
                if (message.top) {
                    if (furnace.isHopperInstalled(ForgeDirection.UP)) {
                        furnace.setHopperEnabled(ForgeDirection.UP, !furnace.isHopperEnabled(ForgeDirection.UP));
                    }
                } else {
                    if (furnace.isHopperInstalled(ForgeDirection.DOWN)) {
                        furnace.setHopperEnabled(ForgeDirection.DOWN, !furnace.isHopperEnabled(ForgeDirection.DOWN));
                    }
                }
                furnace.markDirty();
                world.markBlockForUpdate(message.x, message.y, message.z);
            }
            return null;
        }
    }
}
