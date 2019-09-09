package com.example.openrcontrol;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.openrcontrol.core.Consts;
import com.example.openrcontrol.core.events.CommandSentEvent;
import com.example.openrcontrol.core.events.DeviceAttachedEvent;
import com.example.openrcontrol.core.events.DeviceDetachedEvent;
import com.example.openrcontrol.core.events.LogMessageEvent;
import com.example.openrcontrol.core.events.QuantityCommandSentEvent;
import com.example.openrcontrol.core.events.SelectDeviceEvent;
import com.example.openrcontrol.core.events.ShowDevicesListEvent;
import com.example.openrcontrol.core.events.USBDataReceiveEvent;
import com.example.openrcontrol.core.services.OpenRControl;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.EventBusException;

public class MainActivity extends Activity
{
    private Intent control;
    protected EventBus eventBus;
    //TODO: Falta incluir la actividad de preferencias
    private SharedPreferences sharedPreferences;

    public static final String EXTRA_MESSAGE = "com.example.openrcontrol.MESSAGE";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {
            eventBus = EventBus.builder().logNoSubscriberMessages(false).sendNoSubscriberEvent(false).installDefaultEventBus();
        } catch (EventBusException e) {
            eventBus = EventBus.getDefault();
        }
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        control = new Intent(this, OpenRControl.class);
        startService(control);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event)
    {
        boolean handled = false;
        if ((event.getSource() & InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD) {
            int keyCode = event.getKeyCode();
            int action = event.getAction();
            if (event.getRepeatCount() == 0 && action == KeyEvent.ACTION_DOWN) {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_BUTTON_Y:
                        eventBus.post(new CommandSentEvent(Consts.COMMAND_TOGGLE_HAZARD));
                        handled = true;
                        break;
                    case KeyEvent.KEYCODE_BUTTON_X:
                        eventBus.post(new CommandSentEvent(Consts.COMMAND_TOGGLE_RAINLIGHT));
                        handled = true;
                        break;
                    case KeyEvent.KEYCODE_BUTTON_A:
                        eventBus.post(new CommandSentEvent(Consts.COMMAND_NEXT_RAINLIGHT_COLOR));
                        handled = true;
                        break;
                    default:
                        handled = false;
                        break;
                }
            }
            if (handled) {
                return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public boolean dispatchGenericMotionEvent(MotionEvent event)
    {
        if ((event.getSource() & InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK && event.getAction() == MotionEvent.ACTION_MOVE)
        {
            // Process all historical movement samples in the batch
            final int historySize = event.getHistorySize();

            // Process the movements starting from the
            // earliest historical position in the batch
            for (int i = 0; i < historySize; i++) {
                // Process the event at historical position i
                processJoystickInput(event, i);
            }

            // Process the current movement sample in the batch (position -1)
            processJoystickInput(event, -1);
            return true;
        }

        return super.dispatchGenericMotionEvent(event);
    }

    private static float getCenteredAxis(MotionEvent event, InputDevice device, int axis, int historyPos) {
        final InputDevice.MotionRange range = device.getMotionRange(axis, event.getSource());

        // A joystick at rest does not always report an absolute position of
        // (0,0). Use the getFlat() method to determine the range of values
        // bounding the joystick axis center.
        if (range != null) {
            final float flat = range.getFlat();
            final float value =
                    historyPos < 0 ? event.getAxisValue(axis):
                            event.getHistoricalAxisValue(axis, historyPos);

            // Ignore axis values that are within the 'flat' region of the
            // joystick axis center.
            if (Math.abs(value) > flat) {
                return value;
            }
        }
        return 0;
    }


    private void processJoystickInput(MotionEvent event, int historyPos)
    {
        //FIXME: Hay un error que aún no se puede controlar, que sucede cuando el control se desconecta.
        InputDevice inputDevice = event.getDevice();

        float steering = getCenteredAxis(event, inputDevice, MotionEvent.AXIS_X, historyPos) * 100f;

        float forward = getCenteredAxis(event, inputDevice, MotionEvent.AXIS_RZ, historyPos);
        float backward = getCenteredAxis(event, inputDevice, MotionEvent.AXIS_Z, historyPos);

        float throttle = (forward - backward) * 50f;

        eventBus.post(new QuantityCommandSentEvent(Consts.COMMAND_SET_THROTTLE, throttle));
        eventBus.post(new QuantityCommandSentEvent(Consts.COMMAND_SET_STEERING, steering));

        TextView txtThrottle = findViewById(R.id.txtThrottle);

        ProgressBar pbRightSteer = findViewById(R.id.rightSteering);
        ProgressBar pbLeftSteer = findViewById(R.id.leftSteering);

        TextView txtDirection = findViewById(R.id.txtDirection);
        ProgressBar pbThrottle = findViewById(R.id.throttle);

        txtThrottle.setText(String.valueOf(throttle));

        if (steering >= 0f)
        {
            pbRightSteer.setProgress((int)Math.floor(steering) + 5);
            pbLeftSteer.setProgress(0);
        }
        else if (steering <= 0f)
        {
            pbRightSteer.setProgress(0);
            pbLeftSteer.setProgress((int)Math.floor(-steering) + 5);
        }
        else
        {
            pbRightSteer.setProgress(5);
            pbLeftSteer.setProgress(5);
        }

        pbThrottle.setProgress((int) ((Math.abs(throttle * 2.7f) + 5)));

        if (throttle > 0f)
        {
            txtDirection.setText(R.string.forward);
        }
        else if (throttle < 0f)
        {
            txtDirection.setText(R.string.reverse);
        }
        else
        {
            txtDirection.setText(R.string.neutral);
        }
    }

    private void mLog(String log, boolean newLine) {
        //TODO: falta implementar el guardado del log de comunicaciones en un archivo plano, y la configuración para activarlo en la actividad de preferencias
//        if (newLine) {
//            edtlogText.append(Consts.NEW_LINE);
//        }
//        edtlogText.append(log);
//        if(edtlogText.getLineCount()>200) {
//            edtlogText.setText("cleared");
//        }
    }

    void showListOfDevices(CharSequence devicesName[]) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        if (devicesName.length == 0) {
            builder.setTitle(R.string.MESSAGE_CONNECT_YOUR_USB_HID_DEVICE);
        } else {
            builder.setTitle(R.string.MESSAGE_SELECT_YOUR_USB_HID_DEVICE);
        }

        builder.setItems(devicesName, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                eventBus.post(new SelectDeviceEvent(which));
            }
        });
        builder.setCancelable(true);
        builder.show();
    }


    public void onEvent(USBDataReceiveEvent event) {
        mLog(event.getData() + " \nReceived " + event.getBytesCount() + " bytes", true);
    }

    public void onEvent(LogMessageEvent event) {
        mLog(event.getData(), true);
    }

    public void onEvent(ShowDevicesListEvent event) {
        showListOfDevices(event.getCharSequenceArray());
    }

    public void onEvent(DeviceAttachedEvent event) {

    }

    public void onEvent(DeviceDetachedEvent event) {

    }
}
