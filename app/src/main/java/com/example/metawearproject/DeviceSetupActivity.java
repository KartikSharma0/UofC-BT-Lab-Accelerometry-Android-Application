package com.example.metawearproject;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.mbientlab.metawear.MetaWearBoard;
import com.mbientlab.metawear.android.BtleService;
import com.mbientlab.metawear.module.Accelerometer;
import com.example.metawearproject.DeviceSetupActivityFragment.FragmentSettings;

import bolts.Continuation;
import bolts.Task;

import static android.content.DialogInterface.*;

public class DeviceSetupActivity extends AppCompatActivity implements ServiceConnection, FragmentSettings {
    public final static String EXTRA_BT_DEVICE= "com.mbientlab.metawear.starter.DeviceSetupActivity.EXTRA_BT_DEVICE";

    public static class ReconnectDialogFragment extends DialogFragment implements  ServiceConnection {
        private static final String KEY_BLUETOOTH_DEVICE = "com.mbientlab.metawear.starter.DeviceSetupActivity.ReconnectDialogFragment.KEY_BLUETOOTH_DEVICE";

        private ProgressDialog reconnectDialog = null;
        private BluetoothDevice btDevice = null;
        private MetaWearBoard currentMwBoard = null;
        private Accelerometer accelerometer;

        public static ReconnectDialogFragment newInstance(BluetoothDevice btDevice) {
            Bundle args = new Bundle();
            args.putParcelable(KEY_BLUETOOTH_DEVICE, btDevice);

            ReconnectDialogFragment newFragment = new ReconnectDialogFragment();
            newFragment.setArguments(args);

            return newFragment;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            btDevice = getArguments().getParcelable(KEY_BLUETOOTH_DEVICE);
            getActivity().getApplicationContext().bindService(new Intent(getActivity(), BtleService.class), this, BIND_AUTO_CREATE);

            reconnectDialog = new ProgressDialog(getActivity());
            reconnectDialog.setTitle(getString(R.string.title_reconnect_attempt));
            reconnectDialog.setMessage(getString(R.string.message_wait));
            reconnectDialog.setCancelable(false);
            reconnectDialog.setCanceledOnTouchOutside(false);
            reconnectDialog.setIndeterminate(true);
            reconnectDialog.setButton(BUTTON_NEGATIVE, getString(android.R.string.cancel), (dialogInterface, i) -> {
                currentMwBoard.disconnectAsync();
                getActivity().finish();
            });

            return reconnectDialog;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            currentMwBoard= ((BtleService.LocalBinder) service).getMetaWearBoard(btDevice);

            accelerometer= currentMwBoard.getModule(Accelerometer.class);
            accelerometer.configure()
                    .odr(25f)       // Set sampling frequency to 25Hz, or closest valid ODR
                    .commit();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) { }
    }

    private BluetoothDevice btDevice;
    private MetaWearBoard metawear;

    private final String RECONNECT_DIALOG_TAG= "reconnect_dialog_tag";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_setup);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        btDevice= getIntent().getParcelableExtra(EXTRA_BT_DEVICE);
        getApplicationContext().bindService(new Intent(this, BtleService.class), this, BIND_AUTO_CREATE);

        Toast message = Toast.makeText(getApplicationContext(),"Hit 'start' to begin data stream", Toast.LENGTH_LONG);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_device_setup, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_disconnect:
                metawear.disconnectAsync();
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        metawear.disconnectAsync();
        super.onBackPressed();
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        metawear = ((BtleService.LocalBinder) service).getMetaWearBoard(btDevice);
        metawear.onUnexpectedDisconnect(status -> {
            ReconnectDialogFragment dialogFragment= ReconnectDialogFragment.newInstance(btDevice);
            dialogFragment.show(getSupportFragmentManager(), RECONNECT_DIALOG_TAG);

            metawear.connectAsync().continueWithTask(task -> task.isCancelled() || !task.isFaulted() ? task : MainActivity.reconnect(metawear))
                    .continueWith((Continuation<Void, Void>) task -> {
                        if (!task.isCancelled()) {
                            runOnUiThread(() -> {
                                ((DialogFragment) getSupportFragmentManager().findFragmentByTag(RECONNECT_DIALOG_TAG)).dismiss();
                                ((DeviceSetupActivityFragment) getSupportFragmentManager().findFragmentById(R.id.device_setup_fragment)).reconnected();
                            });
                        } else {
                            finish();
                        }

                        return null;
                    });
        });
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }

    @Override
    public BluetoothDevice getBtDevice() {
        return btDevice;
    }
}

