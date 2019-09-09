package com.example.openrcontrol.core.events;

public class QuantityCommandSentEvent
{
    private final String command;
    private final double quantity;

    public QuantityCommandSentEvent(String command, double quantity)
    {
        this.command = command;
        this.quantity = quantity;
    }

    public String getCommand()
    {
        return command;
    }

    public double getQuantity()
    {
        return quantity;
    }
}
