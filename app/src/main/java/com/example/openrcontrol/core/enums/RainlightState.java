package com.example.openrcontrol.core.enums;

public enum RainlightState
{
    OFF(1),
    ON(2),
    HAZARD(3);

    private int code;

    private RainlightState(int code)
    {
        this.code = code;
    }

    public int getStateCode()
    {
        return code;
    }
}
