package lyn.e.jo.wiggl;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class MainActivity extends Activity implements SensorEventListener {
    String tag = "HomeActivity";

    long msSinceLastDegreeChange;
    int tiltnum;
    int direction; // 1 is increasing, -1 is decreasing
    int startDeg;
    int prevDeg;
    int diff;

    SensorManager sensorManager;
    Sensor accelerometer;
    Sensor magnetometer;
    float[] mGravity;
    float[] mGeomagnetic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accelerometer == null) {
            Log.i(tag, "accelerometer is null");
        }
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if (magnetometer == null) {
            Log.i(tag, "magnetometer is null");
        }

        initListeners();
        tiltnum = 0;
        direction = 1;
        startDeg = 0;
        prevDeg = 0;
    }

    /**
     *
     * @param deg
     *
     * reset tilt tracking if switched directions outside of
     * acceptable tilt range
     * or
     * tilt speed is too slow TODO
     */
    public void checkShyModeReset(int deg) {
        diff = Math.abs(deg - startDeg);
        if (diff < 15) {
            Log.i(tag, "natural movement");
            return;
        }
        if (diff <= 25) {
            // switched direction before entering tilt range (> 20)
            if (direction == 1 && deg < prevDeg) {
                Log.i(tag, "tilt too small");
                direction = -1;
                startDeg = deg;
                tiltnum = 0;
                return;
            }
            else if (direction == -1 && deg > prevDeg) {
                Log.i(tag, "tilt too small");
                startDeg = deg;
                direction = 1;
                tiltnum = 0;
                return;
            }
        }
        else if (diff > 25) {
            if (direction == 1 && deg < prevDeg) {
                direction = -1;
                startDeg = deg;
                tiltnum += 1;
                Log.i(tag, "POINT OF CHANGE: " + deg);
                Log.i(tag, "OKAY TILT. tiltnum: " + tiltnum);
            }
            else if (direction == -1 && deg > prevDeg) {
                startDeg = deg;
                direction = 1;
                tiltnum += 1;
                Log.i(tag, "POINT OF CHANGE: " + deg);
                Log.i(tag, "OKAY TILT. tiltnum: " + tiltnum);
            }
        }
        prevDeg = deg;
        ((TextView)findViewById(R.id.tbox)).setText(String.valueOf(tiltnum));
    }

    public void onSensorChanged(SensorEvent event) {
        int sensorType = event.sensor.getType();
        switch (sensorType) {
            case Sensor.TYPE_ACCELEROMETER:
                mGravity = event.values;
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                mGeomagnetic = event.values;
                break;
            default:
                Log.w(tag, "Unknown sensor type " + sensorType);
                return;
        }
        if (mGravity == null) {
            Log.w(tag, "mGravity is null");
            return;
        }
        if (mGeomagnetic == null) {
            Log.w(tag, "mGeomagnetic is null");
            return;
        }
        float rot[] = new float[9];
        if (! SensorManager.getRotationMatrix(rot, null, mGravity, mGeomagnetic)) {
            Log.w(tag, "getRotationMatrix() failed");
            return;
        }

        float orientation[] = new float[9];
        SensorManager.getOrientation(rot, orientation);
        // azimuth, pitch and roll - use pitch
        float roll = orientation[1];
        int pitchDeg = (int) Math.round(Math.toDegrees(roll));
        Log.d(tag, "change");
        checkShyModeReset(pitchDeg);
    }


    // SENSOR

    public void initListeners()
    {
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_FASTEST);
    }


    protected void onResume() {
        super.onResume();
//        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
//        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
    }

    protected void onPause() {
        super.onPause();
//        sensorManager.unregisterListener(this);
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }


    @Override
    public void onDestroy() {
        sensorManager.unregisterListener(this);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
