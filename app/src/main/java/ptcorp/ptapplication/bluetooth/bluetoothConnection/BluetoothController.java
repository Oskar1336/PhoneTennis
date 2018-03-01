package ptcorp.ptapplication.bluetooth.bluetoothConnection;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
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
    public static final int BLUETOOTH_ENABLE_REQUEST_CODE = 5;
    public static final int BLUETOOTH_DISCOVERABLE_REQUEST_CODE = 6;

    private static final String TAG = "BluetoothController";

    private BluetoothAdapter mBtAdapter;
    private ArrayList<BTDevice> mBtDeviceList;

    private BluetoothConnectionService mConnectionService;
    private Intent mBtServiceConnIntent;
    private BtServiceConnection mBtServiceConnection;
    private boolean mBtServiceBound = false;

    private final BroadcastReceiver mBtSearchReciever;
    private DeviceSearchListener mListener;

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

    public void bindBluetoothService() {
        if (!mBtServiceBound) {
            mBtServiceConnection = new BtServiceConnection();
            mActivity.bindService(mBtServiceConnIntent, mBtServiceConnection, Context.BIND_AUTO_CREATE);
        }
    }

    public void unBindBluetoothService() {
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

    public void setSearchListener(DeviceSearchListener listener) {
        mListener = listener;
    }

    /**
     * Enables bluetooth on the device. If bluetooth is'nt turned on the activity
     * BluetoothAdapter.ACTION_REQUEST_ENABLE will be called with the result code
     * BluetoothController.BLUETOOTH_ENABLE_REQUEST_CODE
     * @return Returns false if bluetooth is'nt available, otherwise it returns true.
     */
    public boolean enableBluetooth() {
        if (mBtAdapter == null) {
            Log.d(TAG, "enableBT: Bluetooth not available on device");
            return false;
        }

        checkBTPermissions();

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
        checkBTPermissions();

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
            }

            checkBTPermissions();

            mBtAdapter.startDiscovery();
            IntentFilter filter = new IntentFilter();
            filter.addAction(BluetoothDevice.ACTION_FOUND);
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            mActivity.registerReceiver(mBtSearchReciever, filter);
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
        // TODO: 2018-03-01 Continue to gamescreen
        Log.d(TAG, "onBluetoothConnected: Connected");
    }

    @Override
    public void onBluetoothDisconnected() {
        // TODO: 2018-03-01 Notify user and give choise of either reconnect or just go back to MainActivity
        Log.d(TAG, "onBluetoothDisconnected: Disconnected");
    }

    private class SearchReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (BluetoothDevice.ACTION_FOUND.equals(intent.getAction())) {
                BluetoothDevice dev = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                boolean isPhone = true;
                if (dev.getBluetoothClass() != null) {
                    Log.d(TAG, "onReceive: " + dev.getBluetoothClass().getMajorDeviceClass());
                    if (dev.getBluetoothClass().getMajorDeviceClass() != BluetoothClass.Device.Major.PHONE) {
                        isPhone = false;
                    }
                }
                if (isPhone && dev.getBondState() != BluetoothDevice.BOND_BONDED) {
                    BTDevice btDevice = new BTDevice(
                            intent.getStringExtra(BluetoothDevice.EXTRA_NAME),
                            intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE),
                            dev
                    );
                    mBtDeviceList.add(btDevice);
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(intent.getAction())) {
                // TODO: 2018-03-01 Check UUID  https://stackoverflow.com/questions/14812326/android-bluetooth-get-uuids-of-discovered-devices/15373239
                mListener.onSearchComplete(mBtDeviceList);
            }
        }
    }

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
