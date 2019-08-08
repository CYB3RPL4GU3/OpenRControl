package com.example.openrcontrol;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.openrcontrol.core.enums.RainlightState;
import com.example.openrcontrol.core.services.OpenRControl;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.EventBusException;

public class MainActivity extends AppCompatActivity
{
    private OpenRControl control;
    private Intent usbService;
    protected EventBus eventBus;
    private SharedPreferences sharedPreferences;

    public static final String EXTRA_MESSAGE = "com.example.openrcontrol.MESSAGE";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //TODO: Falta importar las clases del servicio HID e inicializarlo.
        control = new OpenRControl();
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
        usbService = new Intent(this, OpenRControl.class);
        startService(usbService);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event)
    {
        //TODO: Falta configurar los comandos que van por el transmisor.
        boolean handled = false;
        if ((event.getSource() & InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD) {
            int keyCode = event.getKeyCode();
            int action = event.getAction();
            if (event.getRepeatCount() == 0 && action == KeyEvent.ACTION_DOWN) {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_BUTTON_Y:
                        if (control.isLightHazardModeOn())
                        {
                            control.setRainlightFunction(RainlightState.OFF);
                        }
                        else
                        {
                            control.setRainlightFunction(RainlightState.HAZARD);
                        }
                        handled = true;
                        break;
                    case KeyEvent.KEYCODE_BUTTON_X:
                        if (control.isLightOn())
                        {
                            control.setRainlightFunction(RainlightState.OFF);
                        }
                        else
                        {
                            control.setRainlightFunction(RainlightState.ON);
                        }
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
        //TODO: Falta configurar los comandos que van por el transmisor.
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


    private void processJoystickInput(MotionEvent event, int historyPos) {

        InputDevice inputDevice = event.getDevice();

        float steering = getCenteredAxis(event, inputDevice, MotionEvent.AXIS_X, historyPos) * 100f;

        float forward = getCenteredAxis(event, inputDevice, MotionEvent.AXIS_RZ, historyPos);
        float backward = getCenteredAxis(event, inputDevice, MotionEvent.AXIS_Z, historyPos);

        float throttle = (forward - backward) * 50f;

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
}
