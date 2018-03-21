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
import android.util.Log;

import ptcorp.ptapplication.game.GameActivity;

/**
 * Created by oskarg on 2018-03-01.
 *
 */

public class BluetoothController{
    private static final int BLUETOOTH_ENABLE_REQUEST_CODE = 5;
    public static final int BLUETOOTH_DISCOVERABLE_REQUEST_CODE = 6;

    private static final String TAG = "BluetoothController";

    private BluetoothAdapter mBtAdapter;

    private BluetoothConnectionService mConnectionService;
    private Intent mBtServiceConnIntent;
    private BtServiceConnection mBtServiceConnection;
    private boolean mBtServiceBound = false;

    private final BroadcastReceiver mBtSearchReciever;

    private DeviceSearchListener mListener;

    private GameActivity mActivity;
    private BroadcastReceiver mBtBondReceiver;

    public BluetoothController(GameActivity activity) {
        this.mActivity = activity;

        checkBTPermissions();
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        mBtSearchReciever = new SearchReceiver();
        mBtBondReceiver = new BondStateReceiver();

        mBtServiceConnIntent = new Intent(mActivity, BluetoothConnectionService.class);
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
        stopSearchingForDevices();
    }

    public void onDestroy() {
        mActivity.stopService(mBtServiceConnIntent);

    }

    public boolean isBluetoothAvailable() {
        return mBtAdapter.isEnabled();
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
            Log.w(TAG, "Bluetooth not available on device");
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
     * The host device must be discoverable. Make the host device discoverable for 200s.
     * starts BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE activity for result with request code
     * BluetoothController.BLUETOOTH_DISCOVERABLE_REQUEST_CODE
     */
    public void enableDiscoverable() {
        checkBTPermissions();

        Intent discIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 200);
        mActivity.startActivityForResult(discIntent, BLUETOOTH_DISCOVERABLE_REQUEST_CODE);
    }

    /**
     * Starts searching for devices and registers a broadcast receiver. Should be called by the client device.
     */
    public void startSearchingForDevices() {
        if (mBtAdapter != null) {
            stopSearchingForDevices();

            checkBTPermissions();

            mBtAdapter.startDiscovery();
            IntentFilter filter = new IntentFilter();
            filter.addAction(BluetoothDevice.ACTION_FOUND);
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
            mActivity.registerReceiver(mBtSearchReciever, filter);
        }
    }

    public void stopSearchingForDevices() {
        Log.i(TAG, "stopSearchingForDevices: Stopped to search for bluetooth devices");
        if (mBtAdapter != null) {
            if (mBtAdapter.isDiscovering())
                mBtAdapter.cancelDiscovery();
        }

        try {
            mActivity.unregisterReceiver(mBtSearchReciever);
        } catch (Exception e) {
            Log.i(TAG, "onDestroy: SearchReceiver not registered before");
        }
    }

    private void checkBTPermissions() {
        if (mActivity.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) +
                mActivity.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            mActivity.requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION}, 89);
        }
    }

    public void startHostThread() {
        mConnectionService.startBtHost();
    }

    public void stopHostThread() {
        mConnectionService.stopBtHost();
    }

    public void write(Object obj) {
        mConnectionService.writeObject(obj);
    }

    public void pairDevice(BluetoothDevice device) {
        stopSearchingForDevices();

        // Check if device is already bound.
        if (mBtAdapter.getBondedDevices().contains(device)) {
            mConnectionService.connectToDevice(device);
        } else {
            IntentFilter filter = new IntentFilter();
            filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
            mActivity.registerReceiver(mBtBondReceiver, filter);
            mConnectionService.pairDevice(device);
        }
    }

    /**
     * Get a relative distance to the connected bluetooth device.
     * @return Returns the distance between two bluetooth devices in meters.
     */
    public short getDistanceFromConnectedDevice() {
        int rssi = mConnectionService.getAverageRssi();
        if (rssi < 15) {
            return 5;
        } else if (rssi < 25) {
            return 10;
        } else if (rssi < 35) {
            return 15;
        }
        return 20;
    }

    private class SearchReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (BluetoothDevice.ACTION_FOUND.equals(intent.getAction())) {
                BluetoothDevice dev = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                boolean isPhone = true;
                if (dev.getBluetoothClass() != null) {
                    if (dev.getBluetoothClass().getMajorDeviceClass() != BluetoothClass.Device.Major.PHONE) {
                        isPhone = false;
                    }
                }
                if (isPhone) {
                    BTDevice btDevice = new BTDevice(
                            intent.getStringExtra(BluetoothDevice.EXTRA_NAME),
                            dev
                    );
                    mListener.onDeviceFound(btDevice);
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(intent.getAction())) {
                mListener.onSearchComplete();
            }
        }
    }

    private class BondStateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(intent.getAction())) {
                BluetoothDevice btDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if (btDevice.getAddress().equals(mConnectionService.getSelectedDevice().getAddress())) {
                    if (btDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
                        mConnectionService.connectToDevice(btDevice);
                        mActivity.unregisterReceiver(mBtBondReceiver);
                    }
                }
            }
        }
    }

    private class BtServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mConnectionService = ((BluetoothConnectionService.BtBinder)service).getService();
            mBtServiceBound = true;
            mConnectionService.setListener(mActivity);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBtServiceBound = false;
        }
    }
}
