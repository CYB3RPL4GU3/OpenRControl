package com.example.openrcontrol.core.enums;

public enum RainlightColor
{
    RED(1),
    GREEN(2),
    BLUE(3),
    CYAN(4),
    MAGENTA(5),
    YELLOW(6),
    WHITE(7);

    private int code;

    private RainlightColor(int code)
    {
        this.code = code;
    }

    public int getColorCode() {
        return code;
    }

    public static int getLowestColorCode(){
        int min = Integer.MAX_VALUE;
        for (RainlightColor color: RainlightColor.values()) {
            if (color.getColorCode() < min)
            {
                min = color.getColorCode();
            }
        }
        return min;
    }

    public static int getHighestColorCode(){
        int max = Integer.MIN_VALUE;
        for (RainlightColor color: RainlightColor.values()) {
            if (color.getColorCode() > max)
            {
                max = color.getColorCode();
            }
        }
        return max;
    }
}
