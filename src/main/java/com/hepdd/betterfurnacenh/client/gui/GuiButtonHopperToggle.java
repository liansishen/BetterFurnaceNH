package com.hepdd.betterfurnacenh.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import com.hepdd.betterfurnacenh.tileentities.TileEntityBFFurnace;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiButtonHopperToggle extends GuiButton {

    public static final int BUTTON_SIZE = 16;
    private static final int TEXTURE_SIZE = 16;

    private static final ResourceLocation INPUT_ON = new ResourceLocation(
        "betterfurnacenh",
        "textures/gui/buttons/input_on.png");
    private static final ResourceLocation INPUT_OFF = new ResourceLocation(
        "betterfurnacenh",
        "textures/gui/buttons/input_off.png");
    private static final ResourceLocation OUTPUT_ON = new ResourceLocation(
        "betterfurnacenh",
        "textures/gui/buttons/output_on.png");
    private static final ResourceLocation OUTPUT_OFF = new ResourceLocation(
        "betterfurnacenh",
        "textures/gui/buttons/output_off.png");

    private final boolean isInput;
    private boolean enabled2;
    private final TileEntityBFFurnace furnace;

    public GuiButtonHopperToggle(int id, int x, int y, boolean isInput, boolean enabled, TileEntityBFFurnace furnace) {
        super(id, x, y, BUTTON_SIZE, BUTTON_SIZE, "");
        this.isInput = isInput;
        this.enabled2 = enabled;
        this.furnace = furnace;
    }

    public void setEnabled(boolean enabled) {
        this.enabled2 = enabled;
    }

    public boolean isToggleEnabled() {
        return enabled2;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        if (!this.visible) return;

        ResourceLocation tex;
        if (isInput) {
            tex = enabled2 ? INPUT_ON : INPUT_OFF;
        } else {
            tex = enabled2 ? OUTPUT_ON : OUTPUT_OFF;
        }

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager()
            .bindTexture(tex);

        boolean hover = mouseX >= this.xPosition && mouseY >= this.yPosition
            && mouseX < this.xPosition + this.width
            && mouseY < this.yPosition + this.height;
        if (hover) {
            GL11.glColor4f(1.2F, 1.2F, 1.2F, 1.0F);
        }

        drawScaledTexturedRect(this.xPosition, this.yPosition, BUTTON_SIZE, BUTTON_SIZE, TEXTURE_SIZE);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private void drawScaledTexturedRect(int x, int y, int drawW, int drawH, int texSize) {
        float uScale = 1.0F / (float) texSize;
        Tessellator tess = Tessellator.instance;
        tess.startDrawingQuads();
        tess.addVertexWithUV(
            (double) (x + 0),
            (double) (y + drawH),
            (double) this.zLevel,
            0.0D,
            (double) (drawH * uScale));
        tess.addVertexWithUV(
            (double) (x + drawW),
            (double) (y + drawH),
            (double) this.zLevel,
            (double) (drawW * uScale),
            (double) (drawH * uScale));
        tess.addVertexWithUV(
            (double) (x + drawW),
            (double) (y + 0),
            (double) this.zLevel,
            (double) (drawW * uScale),
            0.0D);
        tess.addVertexWithUV((double) (x + 0), (double) (y + 0), (double) this.zLevel, 0.0D, 0.0D);
        tess.draw();
    }

    public boolean isInput() {
        return isInput;
    }
}
