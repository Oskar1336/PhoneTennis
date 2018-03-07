package ptcorp.ptapplication.game.Sensors;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

/**
 * Created by LinusHakansson on 2018-03-06.
 */

public class SensorListener implements SensorEventListener {
    private SensorResult mListener;

    public SensorListener(SensorResult sensorResult) {
        this.mListener = sensorResult;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        mListener.onUpdate(event);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public interface SensorResult{
        void onUpdate(SensorEvent event);
    }
}
