package com.hepdd.betterfurnacenh.client.overlay;

import net.minecraftforge.common.util.ForgeDirection;

import org.lwjgl.opengl.GL11;

public final class GridMath {

    public static final int[][] GRID_SWITCH_TABLE = { { 0, 5, 3, 1, 2, 4 }, { 5, 0, 1, 3, 2, 4 }, { 1, 3, 0, 5, 2, 4 },
        { 3, 1, 5, 0, 2, 4 }, { 4, 2, 3, 1, 0, 5 }, { 2, 4, 3, 1, 5, 0 } };

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

    public static ForgeDirection determineWrenchingSide(ForgeDirection side, float aX, float aY, float aZ) {
        float modX = (aX % 1.0f + 1.0f) % 1.0f;
        float modY = (aY % 1.0f + 1.0f) % 1.0f;
        float modZ = (aZ % 1.0f + 1.0f) % 1.0f;
        ForgeDirection tBack = side.getOpposite();
        switch (side) {
            case DOWN:
            case UP:
                if (modX < 0.25) {
                    if (modZ < 0.25) return tBack;
                    if (modZ >= 0.75) return tBack;
                    return ForgeDirection.WEST;
                }
                if (modX >= 0.75) {
                    if (modZ < 0.25) return tBack;
                    if (modZ >= 0.75) return tBack;
                    return ForgeDirection.EAST;
                }
                if (modZ < 0.25) return ForgeDirection.NORTH;
                if (modZ >= 0.75) return ForgeDirection.SOUTH;
                return side;
            case NORTH:
            case SOUTH:
                if (modX < 0.25) {
                    if (modY < 0.25) return tBack;
                    if (modY >= 0.75) return tBack;
                    return ForgeDirection.WEST;
                }
                if (modX >= 0.75) {
                    if (modY < 0.25) return tBack;
                    if (modY >= 0.75) return tBack;
                    return ForgeDirection.EAST;
                }
                if (modY < 0.25) return ForgeDirection.DOWN;
                if (modY >= 0.75) return ForgeDirection.UP;
                return side;
            case WEST:
            case EAST:
                if (modZ < 0.25) {
                    if (modY < 0.25) return tBack;
                    if (modY >= 0.75) return tBack;
                    return ForgeDirection.NORTH;
                }
                if (modZ >= 0.75) {
                    if (modY < 0.25) return tBack;
                    if (modY >= 0.75) return tBack;
                    return ForgeDirection.SOUTH;
                }
                if (modY < 0.25) return ForgeDirection.DOWN;
                if (modY >= 0.75) return ForgeDirection.UP;
                return side;
            default:
                return ForgeDirection.UNKNOWN;
        }
    }
}
