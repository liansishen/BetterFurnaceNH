package com.hepdd.betterfurnacenh.client.overlay;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.common.util.ForgeDirection;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import com.hepdd.betterfurnacenh.BFNH;
import com.hepdd.betterfurnacenh.tileentities.TileEntityBFFurnace;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import gregtech.api.GregTechAPI;
import gregtech.api.util.GTUtility;

public class HopperGridOverlay {

    @SubscribeEvent
    public void onDrawBlockHighlight(DrawBlockHighlightEvent event) {
        MovingObjectPosition target = event.target;
        if (target == null || target.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) return;

        EntityPlayer player = event.player;
        TileEntity te = player.worldObj.getTileEntity(target.blockX, target.blockY, target.blockZ);
        if (!(te instanceof TileEntityBFFurnace)) return;

        ItemStack held = event.currentItem;
        if (held == null) return;
        boolean isHopperUpgrade = held.getItem() == BFNH.hopperUpgrade;
        boolean isCrowbar = GTUtility.isStackInList(held, GregTechAPI.sCrowbarList);
        if (!isHopperUpgrade && !isCrowbar) return;

        TileEntityBFFurnace furnace = (TileEntityBFFurnace) te;
        event.setCanceled(true);

        drawBlockOutline(event, target);
        drawGrid(event, target, furnace);
    }

    private void drawBlockOutline(DrawBlockHighlightEvent event, MovingObjectPosition target) {
        EntityPlayer player = event.player;
        double camX = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double) event.partialTicks;
        double camY = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double) event.partialTicks;
        double camZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double) event.partialTicks;

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glLineWidth(2.5f * (Minecraft.getMinecraft().displayHeight / 1080f));
        int program = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM);
        GL20.glUseProgram(0);

        double ox = target.blockX - camX;
        double oy = target.blockY - camY;
        double oz = target.blockZ - camZ;

        Tessellator t = Tessellator.instance;
        t.startDrawing(GL11.GL_LINE_STRIP);
        t.setColorRGBA(0, 0, 0, 164);
        t.addVertex(ox, oy, oz);
        t.addVertex(ox + 1, oy, oz);
        t.addVertex(ox + 1, oy, oz + 1);
        t.addVertex(ox, oy, oz + 1);
        t.addVertex(ox, oy, oz);
        t.addVertex(ox, oy + 1, oz);
        t.addVertex(ox + 1, oy + 1, oz);
        t.addVertex(ox + 1, oy + 1, oz + 1);
        t.addVertex(ox, oy + 1, oz + 1);
        t.addVertex(ox, oy + 1, oz);
        t.addVertex(ox, oy + 1, oz + 1);
        t.addVertex(ox, oy, oz + 1);
        t.addVertex(ox + 1, oy, oz + 1);
        t.addVertex(ox + 1, oy + 1, oz + 1);
        t.addVertex(ox + 1, oy + 1, oz);
        t.addVertex(ox + 1, oy, oz);
        t.draw();

        GL20.glUseProgram(program);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
    }

    private void drawGrid(DrawBlockHighlightEvent event, MovingObjectPosition target, TileEntityBFFurnace furnace) {
        EntityPlayer player = event.player;
        double camX = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double) event.partialTicks;
        double camY = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double) event.partialTicks;
        double camZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double) event.partialTicks;

        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glLineWidth(2.5f * (Minecraft.getMinecraft().displayHeight / 1080f));
        int program = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM);
        GL20.glUseProgram(0);

        GL11.glTranslated(target.blockX - (int) camX, target.blockY - (int) camY, target.blockZ - (int) camZ);
        GL11.glTranslated(0.5D - (camX - (int) camX), 0.5D - (camY - (int) camY), 0.5D - (camZ - (int) camZ));

        int sideHit = target.sideHit;
        GridMath.applyFaceRotation(sideHit);
        GL11.glTranslated(0.0D, -0.502D, 0.0D);

        Tessellator tess = Tessellator.instance;
        tess.startDrawing(GL11.GL_LINES);
        tess.setColorRGBA(0, 0, 0, 127);

        tess.addVertex(+.50D, .0D, -.25D);
        tess.addVertex(-.50D, .0D, -.25D);
        tess.addVertex(+.50D, .0D, +.25D);
        tess.addVertex(-.50D, .0D, +.25D);
        tess.addVertex(+.25D, .0D, -.50D);
        tess.addVertex(+.25D, .0D, +.50D);
        tess.addVertex(-.25D, .0D, -.50D);
        tess.addVertex(-.25D, .0D, +.50D);

        int upCell = GridMath.GRID_SWITCH_TABLE[sideHit][ForgeDirection.UP.ordinal()];
        int downCell = GridMath.GRID_SWITCH_TABLE[sideHit][ForgeDirection.DOWN.ordinal()];

        for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
            if (dir == ForgeDirection.UNKNOWN) continue;
            boolean shouldMark;
            if (dir == ForgeDirection.UP || dir == ForgeDirection.DOWN) {
                shouldMark = furnace.isHopperInstalled(dir);
            } else {
                shouldMark = true;
            }
            if (shouldMark) {
                int cell = GridMath.GRID_SWITCH_TABLE[sideHit][dir.ordinal()];
                drawCellX(tess, cell);
            }
        }

        tess.draw();

        GL20.glUseProgram(program);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
    }

    private void drawCellX(Tessellator tess, int cell) {
        tess.setColorRGBA(0, 0, 0, 127);
        switch (cell) {
            case 0:
                tess.addVertex(+.25D, .0D, +.25D);
                tess.addVertex(-.25D, .0D, -.25D);
                tess.addVertex(-.25D, .0D, +.25D);
                tess.addVertex(+.25D, .0D, -.25D);
                break;
            case 1:
                tess.addVertex(-.25D, .0D, +.50D);
                tess.addVertex(+.25D, .0D, +.25D);
                tess.addVertex(-.25D, .0D, +.25D);
                tess.addVertex(+.25D, .0D, +.50D);
                break;
            case 2:
                tess.addVertex(-.50D, .0D, -.25D);
                tess.addVertex(-.25D, .0D, +.25D);
                tess.addVertex(-.50D, .0D, +.25D);
                tess.addVertex(-.25D, .0D, -.25D);
                break;
            case 3:
                tess.addVertex(-.25D, .0D, -.50D);
                tess.addVertex(+.25D, .0D, -.25D);
                tess.addVertex(-.25D, .0D, -.25D);
                tess.addVertex(+.25D, .0D, -.50D);
                break;
            case 4:
                tess.addVertex(+.50D, .0D, -.25D);
                tess.addVertex(+.25D, .0D, +.25D);
                tess.addVertex(+.50D, .0D, +.25D);
                tess.addVertex(+.25D, .0D, -.25D);
                break;
            case 5:
                tess.addVertex(-.50D, .0D, -.50D);
                tess.addVertex(-.25D, .0D, -.25D);
                tess.addVertex(-.50D, .0D, -.25D);
                tess.addVertex(-.25D, .0D, -.50D);

                tess.addVertex(-.50D, .0D, +.50D);
                tess.addVertex(-.25D, .0D, +.25D);
                tess.addVertex(-.50D, .0D, +.25D);
                tess.addVertex(-.25D, .0D, +.50D);

                tess.addVertex(+.50D, .0D, -.50D);
                tess.addVertex(+.25D, .0D, -.25D);
                tess.addVertex(+.50D, .0D, -.25D);
                tess.addVertex(+.25D, .0D, -.50D);

                tess.addVertex(+.50D, .0D, +.50D);
                tess.addVertex(+.25D, .0D, +.25D);
                tess.addVertex(+.50D, .0D, +.25D);
                tess.addVertex(+.25D, .0D, +.50D);
                break;
        }
        tess.setColorRGBA(0, 0, 0, 127);
    }
}
