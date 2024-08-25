package com.example.metawearproject;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.mbientlab.metawear.MetaWearBoard;

public class ModeSelectorActivity extends AppCompatActivity {

    private static final String EXTRA_BT_DEVICE = "com.mbientlab.metawear.starter.DeviceSetupActivity.EXTRA_BT_DEVICE";
    private BluetoothDevice btDevice;
    private MetaWearBoard metawear;

    private Button graph;
    private Button silhouette;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mode_selector);

        graph = findViewById(R.id.graphMode);
        silhouette = findViewById(R.id.overviewMode);

        graph.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                BluetoothDevice device = getIntent().getParcelableExtra(EXTRA_BT_DEVICE);

                Intent navActivityIntent = new Intent(ModeSelectorActivity.this, DeviceSetupActivity.class);
                navActivityIntent.putExtra(DeviceSetupActivity.EXTRA_BT_DEVICE, device);
                startActivity(navActivityIntent);
            }
        });

        silhouette.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent launchOverview = new Intent(ModeSelectorActivity.this, SilhouetteViewActivity.class);
                startActivity(launchOverview);
            }
        });
    }
}