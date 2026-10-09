package org.ppsspp.ppsspp;

// Touch- and sensor-enabled SurfaceView.
// Supports simple multitouch and pressure.
// DPI scaling is handled by the native code.
// Used by the Vulkan backend (and EGL, but that path is no longer active)

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.InputDevice;
import android.view.InputEvent;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceControl;
import android.view.SurfaceView;

import java.lang.annotation.Native;

public class NativeSurfaceView extends SurfaceView implements SensorEventListener {
        private static String TAG = "NativeSurfaceView";
        private SensorManager mSensorManager;
        private Sensor mAccelerometer;

        public NativeSurfaceView(NativeActivity activity) {
                super(activity);

                Log.i(TAG, "NativeSurfaceView");

                mSensorManager = (SensorManager) activity.getSystemService(Activity.SENSOR_SERVICE);
                mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }

        @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
        private int getToolType(final MotionEvent ev, int pointer) {
                return ev.getToolType(pointer);
        }

        @TargetApi(Build.VERSION_CODES.N)
        private void processMouseDelta(final MotionEvent ev) {
                if ((ev.getSource() & InputDevice.SOURCE_MOUSE) == InputDevice.SOURCE_MOUSE) {
                        float dx = ev.getAxisValue(MotionEvent.AXIS_RELATIVE_X);
                        float dy = ev.getAxisValue(MotionEvent.AXIS_RELATIVE_Y);
                        Log.i(TAG, "Mouse delta: " + dx + " " + dy);
                        NativeApp.mouseDelta(dx, dy);
                }
        }

        public static boolean isFromSource(final InputEvent ev, int source) {
                return (ev.getSource() & source) == source;
        }

        @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
        private void onMouseEventMotion(final MotionEvent ev) {
                Log.i(TAG, "motion mouse event");
                switch (ev.getActionMasked()) {
                        case MotionEvent.ACTION_DOWN: {
                                if (NativeActivity.useModernMouseEvents) {
                                        return;
                                }
                                //Log.i(TAG, "Surface Action down. button state: " + ev.getButtonState());
                                NativeApp.mouse(ev.getX(), ev.getY(), 1, 1);
                                break;
                        }
                        case MotionEvent.ACTION_UP: {
                                if (NativeActivity.useModernMouseEvents) {
                                        return;
                                }
                                //Log.i(TAG, "Surface Action up. button state: " + ev.getButtonState());
                                NativeApp.mouse(ev.getX(), ev.getY(), 1, 2);
                                break;
                        }
                        case MotionEvent.ACTION_MOVE: {
                                // This still needs handling here, even if new events are used.
                                //Log.i(TAG, "Surface Action move. button state: " + ev.getButtonState());
                                NativeApp.mouse(ev.getX(), ev.getY(), 0, 0);
                                break;
                        }
                        default: {
                                Log.i(TAG, "Unhandled modern mouse action: " + ev.getAction());
                                break;
                        }
                }
        }

        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouchEvent(final MotionEvent ev) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1 && isFromSource(ev, InputDevice.SOURCE_MOUSE)) {
                        // This is where workable mouse support arrived.
                        onMouseEventMotion(ev);
                        return true;
                }

                // Log.i(TAG, "processing touch event");
                boolean canReadToolType = Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH;
                for (int i = 0; i < ev.getPointerCount(); i++) {
                        int pid = ev.getPointerId(i);
                        int code = 0;

                        final int action = ev.getActionMasked();

                        // These code bits are now the same as the constants in input_state.h.
                        switch (action) {
                        case MotionEvent.ACTION_DOWN:
                        case MotionEvent.ACTION_POINTER_DOWN:
                                // Log.i(TAG, "ACTION_DOWN");
                                if (ev.getActionIndex() == i)
                                        code = 2;
                                break;
                        case MotionEvent.ACTION_UP:
                        case MotionEvent.ACTION_POINTER_UP:
                                // Log.i(TAG, "ACTION_UP");
                                if (ev.getActionIndex() == i)
                                        code = 4;
                                break;
                        case MotionEvent.ACTION_MOVE: {
                                code = 1;
                                if (Build.VERSION.SDK_INT >= 24) {
                                        processMouseDelta(ev);
                                }
                                break;
                        }
                        default:
                                break;
                        }

                        if (code != 0) {
                                if (canReadToolType) {
                                        int tool = getToolType(ev, i);
                                        code |= tool << 10; // We use the Android tool type codes
                                }
                                NativeApp.touch(ev.getX(i), ev.getY(i), code, pid);
                        }
                }
                return true;
        }

        // Sensor management
        @Override
        public void onAccuracyChanged(Sensor sensor, int arg1) {
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
                if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER) {
                        return;
                }
                // Can also look at event.timestamp for accuracy magic
                NativeApp.accelerometer(event.values[0], event.values[1], event.values[2]);
        }

        public void onPause() {
                mSensorManager.unregisterListener(this);
        }

        public void onResume() {
                mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
        }

        public void onDestroy() {
        }
}
