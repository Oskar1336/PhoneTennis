package ptcorp.ptapplication.bluetooth.bluetoothConnection;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by oskarg on 2018-03-01.
 *
 */

public class BluetoothController implements BtServiceListener {

    private static final int BLUETOOTH_ENABLE_REQUEST_CODE = 5;
    private static final int BLUETOOTH_DISCOVERABLE_REQUEST_CODE = 6;
    private static final String TAG = "BluetoothController";

    private BluetoothAdapter mBtAdapter;
    private ArrayList<BTDevice> mBtDeviceList;

    private BluetoothConnectionService mConnectionService;
    private Intent mBtServiceConnIntent;
    private BtServiceConnection mBtServiceConnection;
    private boolean mBtServiceBound = false;

    private final BroadcastReceiver mBtSearchReciever;

    private AppCompatActivity mActivity;

    public BluetoothController(AppCompatActivity activity) {
        this.mActivity = activity;

        checkBTPermissions();
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        mBtDeviceList = new ArrayList<>();

        mBtSearchReciever = new SearchReceiver();

        mBtServiceConnIntent = new Intent(mActivity, BtServiceConnection.class);
        mActivity.startService(mBtServiceConnIntent);
    }

    public void onResume() {
        if (!mBtServiceBound) {
            mBtServiceConnection = new BtServiceConnection();
            mActivity.bindService(mBtServiceConnIntent, mBtServiceConnection, Context.BIND_AUTO_CREATE);
        }
    }

    public void onPause() {
        if (mBtServiceBound) {
            mActivity.unbindService(mBtServiceConnection);
            mBtServiceBound = false;
        }
    }

    public void onDestroy() {
        mActivity.stopService(mBtServiceConnIntent);

        if (mBtAdapter != null) {
            mBtAdapter.cancelDiscovery();
        }
        mActivity.unregisterReceiver(mBtSearchReciever);
    }

    /**
     * Enables bluetooth on the device.
     * @return Returns false if bluetooth is'nt available, otherwise it returns true.
     */
    public boolean enableBluetooth() {
        if (mBtAdapter == null) {
            Log.d(TAG, "enableBT: Bluetooth not available on device");
            return false;
        }

        if (!mBtAdapter.isEnabled()) {
            Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            mActivity.startActivityForResult(enableBT, BLUETOOTH_ENABLE_REQUEST_CODE);
        }
        return true;
    }

    /**
     * The host device must be discoverable. Make the host device discoverable for 500s.
     * starts BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE activity for result with request code
     * BluetoothController.BLUETOOTH_DISCOVERABLE_REQUEST_CODE
     */
    public void enableDiscoverable() {
        Intent discIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 500);
        mActivity.startActivityForResult(discIntent, BLUETOOTH_DISCOVERABLE_REQUEST_CODE);
    }

    /**
     * Starts searching for devices and registers a broadcast receiver. Should be called by the client device.
     */
    public void startSearchingForDevices() {
        if (mBtAdapter != null) {
            if (mBtAdapter.isDiscovering()) {
                mBtAdapter.cancelDiscovery();
                checkBTPermissions();

                mBtAdapter.startDiscovery();
                IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                mActivity.registerReceiver(mBtSearchReciever, filter);
            }

            if (!mBtAdapter.isDiscovering()) {
                checkBTPermissions();

                mBtAdapter.startDiscovery();
                IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                mActivity.registerReceiver(mBtSearchReciever, filter);
            }
        }
    }

    private void checkBTPermissions() {
        if (mActivity.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) +
                mActivity.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            mActivity.requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION}, 89);
        }
    }

    @Override
    public void onBluetoothConnected() {

    }

    @Override
    public void onBluetoothDisconnected() {

    }

    private class SearchReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (BluetoothDevice.ACTION_FOUND.equals(intent.getAction())) {
                BluetoothDevice dev = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if (dev.getBluetoothClass() != null) {
                    Log.d(TAG, "onReceive: " + dev.getBluetoothClass().getMajorDeviceClass());
                }

                if (dev.getBondState() != BluetoothDevice.BOND_BONDED) {
                    BTDevice btDevice = new BTDevice(
                            intent.getStringExtra(BluetoothDevice.EXTRA_NAME),
                            intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE),
                            dev
                    );
                    mBtDeviceList.add(btDevice);
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(intent.getAction())) {
                // TODO: 2018-03-01 Check UUID  https://stackoverflow.com/questions/14812326/android-bluetooth-get-uuids-of-discovered-devices/15373239
            }
        }
    };

    private class BtServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mConnectionService = ((BluetoothConnectionService.BtBinder)service).getService();
            mBtServiceBound = true;
            mConnectionService.setListener(BluetoothController.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBtServiceBound = false;
        }
    }
}
