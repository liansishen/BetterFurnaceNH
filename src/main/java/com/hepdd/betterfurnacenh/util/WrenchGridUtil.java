package com.hepdd.betterfurnacenh.util;

import net.minecraftforge.common.util.ForgeDirection;

public final class WrenchGridUtil {

    public static final int[][] GRID_SWITCH_TABLE = { { 0, 5, 3, 1, 2, 4 }, { 5, 0, 1, 3, 2, 4 }, { 1, 3, 0, 5, 2, 4 },
        { 3, 1, 5, 0, 2, 4 }, { 4, 2, 3, 1, 0, 5 }, { 2, 4, 3, 1, 5, 0 } };

    private WrenchGridUtil() {}

    public static ForgeDirection determineWrenchingSide(ForgeDirection side, float aX, float aY, float aZ) {
        if (side == null) {
            return ForgeDirection.UNKNOWN;
        }
        float modX = (aX % 1.0f + 1.0f) % 1.0f;
        float modY = (aY % 1.0f + 1.0f) % 1.0f;
        float modZ = (aZ % 1.0f + 1.0f) % 1.0f;
        ForgeDirection tBack = side.getOpposite();
        switch (side) {
            case DOWN:
            case UP:
                if (modX < 0.25f) {
                    if (modZ < 0.25f) return tBack;
                    if (modZ >= 0.75f) return tBack;
                    return ForgeDirection.WEST;
                }
                if (modX >= 0.75f) {
                    if (modZ < 0.25f) return tBack;
                    if (modZ >= 0.75f) return tBack;
                    return ForgeDirection.EAST;
                }
                if (modZ < 0.25f) return ForgeDirection.NORTH;
                if (modZ >= 0.75f) return ForgeDirection.SOUTH;
                return side;
            case NORTH:
            case SOUTH:
                if (modX < 0.25f) {
                    if (modY < 0.25f) return tBack;
                    if (modY >= 0.75f) return tBack;
                    return ForgeDirection.WEST;
                }
                if (modX >= 0.75f) {
                    if (modY < 0.25f) return tBack;
                    if (modY >= 0.75f) return tBack;
                    return ForgeDirection.EAST;
                }
                if (modY < 0.25f) return ForgeDirection.DOWN;
                if (modY >= 0.75f) return ForgeDirection.UP;
                return side;
            case WEST:
            case EAST:
                if (modZ < 0.25f) {
                    if (modY < 0.25f) return tBack;
                    if (modY >= 0.75f) return tBack;
                    return ForgeDirection.NORTH;
                }
                if (modZ >= 0.75f) {
                    if (modY < 0.25f) return tBack;
                    if (modY >= 0.75f) return tBack;
                    return ForgeDirection.SOUTH;
                }
                if (modY < 0.25f) return ForgeDirection.DOWN;
                if (modY >= 0.75f) return ForgeDirection.UP;
                return side;
            default:
                return ForgeDirection.UNKNOWN;
        }
    }
}
