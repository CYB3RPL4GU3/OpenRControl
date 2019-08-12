package com.example.openrcontrol.core.events;

public class CommandSentEvent
{
    private final String command;

    public CommandSentEvent(String command)
    {
        this.command = command;
    }

    public String getCommand()
    {
        return command;
    }
}
