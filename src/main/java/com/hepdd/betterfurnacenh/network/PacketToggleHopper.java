package com.hepdd.betterfurnacenh.network;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.util.ForgeDirection;

import com.gtnewhorizon.gtnhlib.util.ServerThreadUtil;
import com.hepdd.betterfurnacenh.inventory.ContainerBFFurnace;
import com.hepdd.betterfurnacenh.tileentities.TileEntityBFFurnace;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

public class PacketToggleHopper implements IMessage {

    private boolean top;

    public PacketToggleHopper() {}

    public PacketToggleHopper(boolean top) {
        this.top = top;
    }

    public PacketToggleHopper(int x, int y, int z, boolean top) {
        this(top);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(top);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        top = buf.readBoolean();
    }

    public static class Handler implements IMessageHandler<PacketToggleHopper, IMessage> {

        @Override
        public IMessage onMessage(PacketToggleHopper message, MessageContext ctx) {
            final EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            final boolean toggleTop = message.top;
            ServerThreadUtil.addScheduledTask(() -> {
                if (player == null || player.worldObj == null) return;
                if (!(player.openContainer instanceof ContainerBFFurnace)) return;
                ContainerBFFurnace container = (ContainerBFFurnace) player.openContainer;
                if (!container.canInteractWith(player)) return;

                TileEntityBFFurnace furnace = container.getTileFurnace();
                if (furnace == null || furnace.isInvalid()) return;

                ForgeDirection dir = toggleTop ? ForgeDirection.UP : ForgeDirection.DOWN;
                if (furnace.isHopperInstalled(dir)) {
                    furnace.setHopperEnabled(dir, !furnace.isHopperEnabled(dir));
                    furnace.markDirty();
                    container.detectAndSendChanges();
                }
            });
            return null;
        }
    }
}
