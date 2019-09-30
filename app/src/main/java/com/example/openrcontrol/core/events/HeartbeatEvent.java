package com.example.openrcontrol.core.events;

public class HeartbeatEvent
{
    private boolean ignore;

    public HeartbeatEvent(boolean ignore)
    {
        this.ignore = ignore;
    }

    public boolean isIgnored()
    {
        return ignore;
    }
}
