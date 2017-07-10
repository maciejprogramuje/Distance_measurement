package commaciejprogramuje.facebook.drogomierz;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private OdometerService odometer;
    private boolean bound = false;
    private int distance = 0;
    private boolean measurementInRun = false;

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            OdometerService.OdometerBinder odometerBinder = (OdometerService.OdometerBinder) service;
            odometer = odometerBinder.getOdometer();
            bound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(savedInstanceState != null) {
            distance = savedInstanceState.getInt("distance");
            measurementInRun = savedInstanceState.getBoolean("measurementInRun");
        }

        watchMileage();
    }

    private void watchMileage() {
        final TextView distanceView = (TextView) findViewById(R.id.distance);
        final Handler handler = new Handler();
        
        handler.post(new Runnable() {
            @Override
            public void run() {
                if(odometer != null && measurementInRun) {
                    distance = odometer.getDistance();
                }
                distanceView.setText(distance + " m");
                handler.postDelayed(this, 1000);

                /*distanceView.setText(distance + " secs");
                if(measurementInRun) {
                    distance++;
                }
                handler.postDelayed(this, 1000);*/
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, OdometerService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (bound) {
            unbindService(connection);
            bound = false;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt("distance", distance);
        outState.putBoolean("measurementInRun", measurementInRun);
    }

    public void startMeasurement(View view) {
        measurementInRun = true;
    }

    public void stopMeasurement(View view) {
        measurementInRun = false;
    }

    public void resetMeasurement(View view) {
        measurementInRun = false;
        OdometerService.setDistanceInMeters(0);
        OdometerService.setLastLocation(null);
    }
}
