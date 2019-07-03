package com.example.openrcontrol.core;

import com.example.openrcontrol.core.enums.RainlightColor;
import com.example.openrcontrol.core.enums.RainlightState;

public class OpenRControl {
    private boolean light = false;
    private boolean hazard = false;
    private RainlightColor color;

    public OpenRControl()
    {
        light = false;
        hazard = false;
    }

    public boolean isLightHazardModeOn() {
        return hazard;
    }

    public boolean isLightOn() {
        return light;
    }

    public void setRainlightFunction(RainlightState function)
    {
        switch (function)
        {
            case ON:
                //TODO: Enviar "R2" por el transmisor.
                light = true;
                hazard = false;
                break;
            case HAZARD:
                //TODO: Enviar "R3" por el transmisor.
                light = false;
                hazard = true;
                break;
            case OFF:
                //TODO: Enviar "R1" por el transmisor.
                light = false;
                hazard = false;
                break;
        }
    }

    public void setRainlightColor(RainlightColor color)
    {
        //TODO: Enviar "C" mas el código de color al transmisor.
        this.color = color;
    }
}
