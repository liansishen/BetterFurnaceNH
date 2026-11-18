package com.hepdd.betterfurnacenh.client.overlay;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class GridMath {

    private GridMath() {}

    public static void applyFaceRotation(int sideHit) {
        switch (sideHit) {
            case 0:
                break;
            case 1:
                GL11.glRotatef(180, 1, 0, 0);
                break;
            case 2:
                GL11.glRotatef(90, 1, 0, 0);
                break;
            case 3:
                GL11.glRotatef(-90, 1, 0, 0);
                break;
            case 4:
                GL11.glRotatef(-90, 0, 0, 1);
                break;
            case 5:
                GL11.glRotatef(90, 0, 0, 1);
                break;
        }
    }
}
