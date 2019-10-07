package com.example.openrcontrol.core.services;

import com.example.openrcontrol.core.enums.RainlightColor;
import com.example.openrcontrol.core.enums.RainlightState;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import com.example.openrcontrol.R;
import com.example.openrcontrol.core.Consts;
import com.example.openrcontrol.core.USBUtils;
import com.example.openrcontrol.core.events.CommandSentEvent;
import com.example.openrcontrol.core.events.HeartbeatAcknowledgeEvent;
import com.example.openrcontrol.core.events.HeartbeatEvent;
import com.example.openrcontrol.core.events.LogMessageEvent;
import com.example.openrcontrol.core.events.QuantityCommandSentEvent;
import com.example.openrcontrol.core.events.USBDataReceiveEvent;
import com.example.openrcontrol.core.events.USBDataSendEvent;

import java.util.Locale;

public class OpenRControl extends AbstractUSBHIDService
{
    private String delimiter;
    private String receiveDataFormat;
    private boolean light = false;
    private boolean hazard = false;
    private RainlightColor color;
    private RainlightState function;
    private boolean ignoreHeartbeat = false;

    private StringBuilder messageBuilder;
    private boolean receivedStartChar;
    private boolean validMessage;

    @Override
    public void onCreate()
    {
        super.onCreate();
        light = false;
        hazard = false;
        delimiter = "";
        receiveDataFormat = Consts.TEXT;
        messageBuilder = new StringBuilder();
        receivedStartChar = false;
        validMessage = false;
    }

    @Override
    public void onCommand(Intent intent, String action, int flags, int startId)
    {
        if (Consts.RECEIVE_DATA_FORMAT.equals(action)) {
            receiveDataFormat = intent.getStringExtra(Consts.RECEIVE_DATA_FORMAT);
            delimiter = intent.getStringExtra(Consts.DELIMITER);
        }
        super.onCommand(intent, action, flags, startId);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
    }

    @Override
    public void onDeviceConnected(UsbDevice device)
    {
        mLog("device connected");
    }

    @Override
    public void onDeviceDisconnected(UsbDevice device)
    {
        mLog("device disconnected");
    }

    @Override
    public void onDeviceSelected(UsbDevice device)
    {
        mLog("Selected device VID:" + Integer.toHexString(device.getVendorId()) + " PID:" + Integer.toHexString(device.getProductId()));
    }

    @Override
    public CharSequence onBuildingDevicesList(UsbDevice usbDevice)
    {
        return "devID:" + usbDevice.getDeviceId() + " VID:" + Integer.toHexString(usbDevice.getVendorId()) + " PID:" + Integer.toHexString(usbDevice.getProductId()) + " " + usbDevice.getDeviceName();
    }

    @Override
    public void onUSBDataSending(String data)
    {
        mLog("Sending: " + data);
    }

    @Override
    public void onUSBDataSent(int status, byte[] out)
    {
        mLog("Sent " + status + " bytes");
        for (int i = 0; i < out.length && out[i] != 0; i++) {
            mLog(Consts.SPACE + USBUtils.toInt(out[i]));
        }
    }

    public void onEvent(HeartbeatEvent event)
    {
        ignoreHeartbeat = event.isIgnored();
    }

    public void onEvent(CommandSentEvent event)
    {
        switch (event.getCommand())
        {
            case Consts.COMMAND_TOGGLE_RAINLIGHT:
            {
                toggleRainlight();
                eventBus.post(new USBDataSendEvent(getString(R.string.rainlightFunction) + function.getStateCode()));
            }
            case Consts.COMMAND_TOGGLE_HAZARD:
            {
                toggleHazard();
                eventBus.post(new USBDataSendEvent(getString(R.string.rainlightFunction) + function.getStateCode()));
            }
            case Consts.COMMAND_NEXT_RAINLIGHT_COLOR:
            {
                setRainlightColor();
                eventBus.post(new USBDataSendEvent(getString(R.string.rainlightColor) + color.getColorCode()));
            }
        }
    }

    public void onEvent(QuantityCommandSentEvent event)
    {
        switch (event.getCommand())
        {
            case Consts.COMMAND_SET_THROTTLE:
            {
                eventBus.post(new USBDataSendEvent(getString(R.string.throttle) + String.format(Locale.US, "%d", (int)(event.getQuantity()))));
                break;
            }
            case Consts.COMMAND_SET_STEERING:
            {
                eventBus.post(new USBDataSendEvent(getString(R.string.steering) + String.format(Locale.US, "%d", (int)(event.getQuantity()))));
                break;
            }
        }
    }

    @Override
    public void onSendingError(Exception e)
    {
        mLog("Please check your bytes, sent as text");
    }

    @Override
    public void onUSBDataReceive(byte[] buffer) {

        StringBuilder stringBuilder = new StringBuilder();
        int i = 0;
        if (receiveDataFormat.equals(Consts.INTEGER))
        {
            for (; i < buffer.length && buffer[i] != 0; i++)
            {
                stringBuilder.append(delimiter).append(USBUtils.toInt(buffer[i]));
            }
            validMessage = true;
            messageBuilder = stringBuilder;
        }
        else if (receiveDataFormat.equals(Consts.HEXADECIMAL))
        {
            for (; i < buffer.length && buffer[i] != 0; i++)
            {
                stringBuilder.append(delimiter).append(Integer.toHexString(buffer[i]));
            }
            validMessage = true;
            messageBuilder = stringBuilder;
        }
        else if (receiveDataFormat.equals(Consts.TEXT))
        {
            for (; i < buffer.length && buffer[i] != 0; i++)
            {
                stringBuilder.append((char) buffer[i]);
            }

            int startCharIndex = stringBuilder.indexOf(getString(R.string.startChar));
            if (startCharIndex >= 0 ^ receivedStartChar)
            {
                receivedStartChar = true;
                int terminatorIndex = stringBuilder.indexOf(getString(R.string.terminator));
                if (terminatorIndex >= 0)
                {
                    messageBuilder.append(stringBuilder.toString().substring(startCharIndex < 0 ? 0 : startCharIndex, terminatorIndex + 1));
                    validMessage = true;
                }
                else
                {
                    messageBuilder.append(stringBuilder.toString().substring(startCharIndex < 0 ? 0 : startCharIndex));
                }
                if (messageBuilder.length() > Consts.MAX_RECEIVED_STRING_SIZE)
                {
                    messageBuilder = new StringBuilder();
                    receivedStartChar = false;
                    validMessage = false;
                }
            }
            else
            {
                messageBuilder = new StringBuilder();
                receivedStartChar = false;
                validMessage = false;
            }
        }
        else if (receiveDataFormat.equals(Consts.BINARY))
        {
            for (; i < buffer.length && buffer[i] != 0; i++)
            {
                stringBuilder.append(delimiter).append("0b").append(Integer.toBinaryString(Integer.valueOf(buffer[i])));
            }
            validMessage = true;
            messageBuilder = stringBuilder;
        }
        if (validMessage)
        {
            String message = messageBuilder.toString();
            messageBuilder = new StringBuilder();
            receivedStartChar = false;
            validMessage = false;
            eventBus.post(new USBDataReceiveEvent(message, message.length()));
        }
    }

    public void onEvent(HeartbeatAcknowledgeEvent event)
    {
        if (!ignoreHeartbeat)
        {
            eventBus.post(new USBDataSendEvent(getString(R.string.heartbeatResponse)));
        }
    }

    private void mLog(String log)
    {
        eventBus.post(new LogMessageEvent(log));
    }

    public boolean isLightHazardModeOn()
    {
        return hazard;
    }

    public boolean isLightOn()
    {
        return light;
    }

    private void setRainlightFunction(RainlightState function)
    {
        switch (function)
        {
            case ON:
                light = true;
                hazard = false;
                break;
            case HAZARD:
                light = false;
                hazard = true;
                break;
            case OFF:
                light = false;
                hazard = false;
                break;
        }
        this.function = function;
    }

    public void setRainlightColor()
    {
        int actualColor = this.color.getColorCode();
        int lowestColor =  RainlightColor.getLowestColorCode();
        int highestColor =  RainlightColor.getHighestColorCode();
        if (actualColor + 1 > highestColor)
        {
            actualColor = lowestColor;
        }
        else
        {
            actualColor++;
        }
        this.color = RainlightColor.getColorByCode(actualColor);
    }

    public void toggleRainlight()
    {
        if (this.isLightOn())
        {
            this.setRainlightFunction(RainlightState.OFF);
        }
        else
        {
            this.setRainlightFunction(RainlightState.ON);
        }
    }

    public void toggleHazard()
    {
        if (this.isLightHazardModeOn())
        {
            this.setRainlightFunction(RainlightState.OFF);
        }
        else
        {
            this.setRainlightFunction(RainlightState.HAZARD);
        }
    }
}
