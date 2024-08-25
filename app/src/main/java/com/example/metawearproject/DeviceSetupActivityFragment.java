package com.example.metawearproject;

// Graph library used: https://github.com/jjoe64/GraphView

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import androidx.fragment.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.mbientlab.metawear.Data;
import com.mbientlab.metawear.MetaWearBoard;
import com.mbientlab.metawear.Route;
import com.mbientlab.metawear.Subscriber;
import com.mbientlab.metawear.android.BtleService;
import com.mbientlab.metawear.builder.RouteBuilder;
import com.mbientlab.metawear.builder.RouteComponent;
import com.mbientlab.metawear.data.Acceleration;
import com.mbientlab.metawear.module.Accelerometer;

import bolts.Continuation;
import bolts.Task;

/**
 * A placeholder fragment containing a simple view.
 */
public class DeviceSetupActivityFragment extends Fragment implements ServiceConnection {
    public interface FragmentSettings {
        BluetoothDevice getBtDevice();
    }

    private MetaWearBoard metawear = null;
    private FragmentSettings settings;
    private Accelerometer accelerometer; // Accelerometer object declaration
    private double accelCurrentVal;
    private int pointsPlotted = 0;
    private int graphIntervalCounter = 0;
    TextView accelVal;
    private Viewport viewport;
    private LineGraphSeries<DataPoint> accelSeries = new LineGraphSeries<>(new DataPoint[]{});

    float startTime;
    float endTime;

    public DeviceSetupActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Activity owner= getActivity();
        if (!(owner instanceof FragmentSettings)) {
            throw new ClassCastException("Owning activity must implement the FragmentSettings interface");
        }
        settings= (FragmentSettings) owner;
        owner.getApplicationContext().bindService(new Intent(owner, BtleService.class), this, Context.BIND_AUTO_CREATE);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        ///< Unbind the service when the activity is destroyed
        getActivity().getApplicationContext().unbindService(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setRetainInstance(true);
        return inflater.inflate(R.layout.fragment_device_setup, container, false);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        metawear = ((BtleService.LocalBinder) service).getMetaWearBoard(settings.getBtDevice());

        accelerometer= metawear.getModule(Accelerometer.class);
        accelerometer.configure()
                .odr(25f)       // Set sampling frequency to 25Hz, or closest valid ODR
                .commit();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }

    /**
     * Called when the app has reconnected to the board
     */
    public void reconnected() { }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        accelVal = (TextView) getView().findViewById(R.id.accelVal);
        GraphView accelGraph = (GraphView) getView().findViewById(R.id.graph);
        accelGraph.addSeries(accelSeries);
        viewport = accelGraph.getViewport();
        viewport.setScrollable(false);
        viewport.setXAxisBoundsManual(true);
        viewport.setMaxX(pointsPlotted);
        viewport.setMinX(pointsPlotted - 200);

        view.findViewById(R.id.acc_start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                accelerometer.acceleration().addRouteAsync(new RouteBuilder() {
                    @Override
                    public void configure(RouteComponent source) {
                        source.stream(new Subscriber() {
                            @Override
                            public void apply(Data data, Object... env) {

                                float xAccel = data.value(Acceleration.class).x();
                                float yAccel = data.value(Acceleration.class).y();
                                float zAccel = data.value(Acceleration.class).z();

                                pointsPlotted++;
                                accelCurrentVal = Math.sqrt((xAccel*xAccel + yAccel*yAccel + zAccel*zAccel));
                                accelVal.setText("Acceleration: "+accelCurrentVal);
                                updateGraph(pointsPlotted, accelCurrentVal);

                                if(accelCurrentVal<0.6){
                                    startTime = System.currentTimeMillis();
                                    if(accelCurrentVal>1){
                                        endTime = System.currentTimeMillis();
                                        if(endTime - startTime>200){
                                            Toast fallDetected = Toast.makeText(getActivity().getApplicationContext(), "FALL DETECTED",Toast.LENGTH_LONG);
                                            Log.i("FALL","Fall Detected");
                                        }
                                    }
                                }
                            }
                        });
                    }

                }).continueWith(new Continuation<Route, Void>() {
                    @Override
                    public Void then(Task<Route> task) throws Exception {
                        accelerometer.acceleration().start();
                        accelerometer.start();
                        return null;
                    }
                });
            }
        });
        view.findViewById(R.id.acc_stop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                accelerometer.stop();
                accelerometer.acceleration().stop();
                metawear.tearDown();
            }
        });
    }

    public void updateGraph(int pointsPlotted, double accelCurrentVal){
        accelSeries.appendData(new DataPoint(pointsPlotted,accelCurrentVal), true,pointsPlotted);
    }
}
