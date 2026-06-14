package com.hepdd.betterfurnacenh.client.gui;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraftforge.fluids.FluidStack;

import org.lwjgl.opengl.GL11;

import com.hepdd.betterfurnacenh.inventory.ContainerBFFurnace;
import com.hepdd.betterfurnacenh.tileentities.TileEntityBFFurnace;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiBFFurnace extends GuiContainer {

    private static final ResourceLocation FURNACE_TEXTURE = new ResourceLocation("textures/gui/container/furnace.png");

    private final TileEntityBFFurnace tileFurnace;

    private static final int FLUID_X = 14;
    private static final int FLUID_Y = 18;
    private static final int FLUID_W = 16;
    private static final int FLUID_H = 40;

    private static final int FLUID_BG_X = 13;
    private static final int FLUID_BG_Y = 17;
    private static final int FLUID_BG_W = 18;
    private static final int FLUID_BG_H = 42;

    public GuiBFFurnace(InventoryPlayer invPlayer, TileEntityBFFurnace tileFurnace) {
        super(new ContainerBFFurnace(invPlayer, tileFurnace));
        this.tileFurnace = tileFurnace;
        this.ySize = 166;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        String title = tileFurnace.hasCustomInventoryName() ? tileFurnace.getInventoryName()
            : StatCollector.translateToLocal(tileFurnace.getInventoryName());
        fontRendererObj.drawString(title, xSize / 2 - fontRendererObj.getStringWidth(title) / 2, 6, 4210752);
        fontRendererObj.drawString(StatCollector.translateToLocal("container.inventory"), 8, ySize - 96 + 2, 4210752);

        int x = mouseX - guiLeft;
        int y = mouseY - guiTop;

        if (x >= FLUID_BG_X && x <= FLUID_BG_X + FLUID_BG_W && y >= FLUID_BG_Y && y <= FLUID_BG_Y + FLUID_BG_H) {
            FluidStack fluid = tileFurnace.fluidTank.getFluid();
            List<String> tooltip = new ArrayList<>();
            if (fluid != null && fluid.amount > 0) {
                tooltip.add(fluid.getLocalizedName());
                tooltip.add(fluid.amount + " / " + tileFurnace.fluidTank.getCapacity() + " L");
            } else {
                tooltip.add(StatCollector.translateToLocal("gui.bfnh.empty"));
            }
            drawHoveringText(tooltip, x, y, fontRendererObj);
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager()
            .bindTexture(FURNACE_TEXTURE);
        int k = (width - xSize) / 2;
        int l = (height - ySize) / 2;
        drawTexturedModalRect(k, l, 0, 0, xSize, ySize);

        if (tileFurnace.isBurning()) {
            int burn = tileFurnace.getBurnTimeRemainingScaled(13);
            drawTexturedModalRect(k + 56, l + 36 + 12 - burn, 176, 12 - burn, 14, burn + 1);
        }

        int progress = tileFurnace.getCookProgressScaled(24);
        drawTexturedModalRect(k + 79, l + 34, 176, 14, progress + 1, 16);

        drawFluidTank(k, l);
    }

    private void drawFluidTank(int guiLeft, int guiTop) {
        drawRect(
            guiLeft + FLUID_BG_X,
            guiTop + FLUID_BG_Y,
            guiLeft + FLUID_BG_X + FLUID_BG_W,
            guiTop + FLUID_BG_Y + FLUID_BG_H,
            0xFF373737);
        drawRect(
            guiLeft + FLUID_BG_X + 1,
            guiTop + FLUID_BG_Y + 1,
            guiLeft + FLUID_BG_X + FLUID_BG_W - 1,
            guiTop + FLUID_BG_Y + FLUID_BG_H - 1,
            0xFF2B2B2B);

        FluidStack fluid = tileFurnace.fluidTank.getFluid();
        if (fluid != null && fluid.amount > 0) {
            int capacity = tileFurnace.fluidTank.getCapacity();
            int fillHeight = (int) ((float) fluid.amount / capacity * FLUID_H);

            IIcon icon = fluid.getFluid()
                .getStillIcon();
            if (icon != null) {
                int color = fluid.getFluid()
                    .getColor(fluid);
                float r = (color >> 16 & 255) / 255.0F;
                float g = (color >> 8 & 255) / 255.0F;
                float b = (color & 255) / 255.0F;

                GL11.glColor4f(r, g, b, 1.0F);
                mc.getTextureManager()
                    .bindTexture(TextureMap.locationBlocksTexture);

                int yPos = guiTop + FLUID_Y + FLUID_H - fillHeight;
                int remaining = fillHeight;
                while (remaining > 0) {
                    int tileH = Math.min(remaining, 16);
                    drawTexturedModelRectFromIcon(guiLeft + FLUID_X, yPos, icon, FLUID_W, tileH);
                    yPos += tileH;
                    remaining -= tileH;
                }
            }

            String amountText = fluid.amount + " L";
            int textW = fontRendererObj.getStringWidth(amountText);
            float scale = 0.65F;
            GL11.glPushMatrix();
            GL11.glScalef(scale, scale, 1.0F);
            fontRendererObj.drawStringWithShadow(
                amountText,
                (int) ((guiLeft + FLUID_BG_X + FLUID_BG_W / 2) / scale - textW / 2),
                (int) ((guiTop + FLUID_BG_Y + FLUID_BG_H + 2) / scale),
                0xFFFFFF);
            GL11.glPopMatrix();
        }

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager()
            .bindTexture(FURNACE_TEXTURE);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        drawRect(
            guiLeft + FLUID_BG_X,
            guiTop + FLUID_BG_Y,
            guiLeft + FLUID_BG_X + FLUID_BG_W,
            guiTop + FLUID_BG_Y + 1,
            0xFF555555);
        drawRect(
            guiLeft + FLUID_BG_X,
            guiTop + FLUID_BG_Y + FLUID_BG_H - 1,
            guiLeft + FLUID_BG_X + FLUID_BG_W,
            guiTop + FLUID_BG_Y + FLUID_BG_H,
            0xFF555555);
        drawRect(
            guiLeft + FLUID_BG_X,
            guiTop + FLUID_BG_Y,
            guiLeft + FLUID_BG_X + 1,
            guiTop + FLUID_BG_Y + FLUID_BG_H,
            0xFF555555);
        drawRect(
            guiLeft + FLUID_BG_X + FLUID_BG_W - 1,
            guiTop + FLUID_BG_Y,
            guiLeft + FLUID_BG_X + FLUID_BG_W,
            guiTop + FLUID_BG_Y + FLUID_BG_H,
            0xFF555555);
    }
}
