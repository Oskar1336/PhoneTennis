package ptcorp.ptapplication.bluetooth.bluetoothConnection;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.UUID;

/**
 * Created by oskarg on 2018-02-26.
 *
 */

public class BluetoothConnectionService extends Service {
    public static final String BT_APP_NAME = "PhoneTennisBT";

    private static final String TAG = "BtServiceConnection";
    private static final UUID mAppUUID = UUID.fromString("030d51ce-ca04-4256-bce7-7b09dbc7d1ad");

    private BtHostThread mHostThread;
    private BtClientThread mClientThread;
    private BtConnectedThread mConnectedThread;
    private BluetoothAdapter mBtAdapter;
    private BluetoothDevice mBtDevice;
    private BluetoothGatt mBtGatt;
    private BtServiceListener mListener;
    private boolean mConnected = false;

    private int[] mRssiAverage = new int[5];
    private int mRssiTotal = 0;
    private int mAverageRssi = 0;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: Starting service");
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (mConnectedThread != null) mConnectedThread.disconnect();
        if (mHostThread != null) mHostThread.stopBtHost();
        if (mClientThread != null) mClientThread.stopBtClient();

        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind: BluetoothConnectionService");
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        return new BtBinder();
    }

    /**
     * BtServiceConnection binder class. It contains an method called getService used to get an instance of the started service.
     */
    public class BtBinder extends Binder {
        public BluetoothConnectionService getService() {
            return BluetoothConnectionService.this;
        }
    }

    /**
     * Writes an object to the connected device.
     * @param obj Any object
     */
    public void writeObject(Object obj) {
        if (mConnected && mConnectedThread != null)
            mConnectedThread.write(obj);
    }

    /**
     * Stops any attempt to connect to a Host as a client and start listening for incoming Bluetooth connections as a Host.
     */
    public void startBtHost() {
        if (mClientThread != null) {
            mClientThread.stopBtClient();
            mClientThread = null;
        }

        if (mHostThread == null) {
            mHostThread = new BtHostThread();
            mHostThread.start();
        }
    }

    /**
     * Stop hosting a Bluetooth server.
     */
    public void stopBtHost() {
        if (mHostThread != null) mHostThread.stopBtHost();
    }

    /**
     * Pair to a found Bluetooth device.
     * @param device BluetoothDevice
     */
    public void pairDevice(BluetoothDevice device) {
        if (!mConnected) {
            device.createBond();
        }
    }

    /**
     * Connect to a paired Bluetooth host device. Call method {@link #pairDevice(BluetoothDevice), pairDevice} before trying to connect to the device.
     * @param device BluetoothDevice
     */
    public void connectToDevice(BluetoothDevice device) {
        mBtDevice = device;
        mBtGatt = mBtDevice.connectGatt(getApplicationContext(), true, new GattListener());

        mClientThread = new BtClientThread(mBtDevice);
        mClientThread.start();
    }

    /**
     * Stop connecting to a device.
     */
    public void stopClient() {
        if (mClientThread != null) mClientThread.stopBtClient();
    }

    /**
     * Disconnect from the Bluetooth connection.
     */
    public void disconnectFromDevice() {
        mConnectedThread.disconnect();
    }

    /**
     * Set a listener for listening to Bluetooth events.
     * @param listener BtServiceListener
     */
    public void setListener(BtServiceListener listener) {
        mListener = listener;
    }

    /**
     * Get the average rssi value. An estimate of the signal strength between two devices.
     * @return int average rssi.
     */
    public int getAverageRssi() {
        return mAverageRssi;
    }

    /**
     * Starts a connected thread which listens for incoming messages and can send outgoing messages.
     * @param mmSocket BluetoothSocket
     */
    private void handleConnectedDevice(BluetoothSocket mmSocket) {
        mConnected = true;
        Log.d(TAG, "handleConnectedDevice:-------------------------HandleConnectedDevice------------------------ ");
        mConnectedThread = new BtConnectedThread(mmSocket);
        mConnectedThread.start();
    }

    /**
     * BluetoothHost listens for incoming connections.
     */
    private class BtHostThread extends Thread {
        private BluetoothServerSocket mmBtServerSocket;
        private boolean mmRunning = true;

        BtHostThread() {
            BluetoothServerSocket tmpSocket = null;
            try {
                tmpSocket = mBtAdapter.listenUsingRfcommWithServiceRecord(BT_APP_NAME, mAppUUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mmBtServerSocket = tmpSocket;
        }

        @Override
        public void run() {
            BluetoothSocket socket;
            while(mmRunning) {
                try {
                    socket = mmBtServerSocket.accept();
                    if (socket != null) {
                        mBtAdapter.cancelDiscovery();
                        stopBtHost();
                        handleConnectedDevice(socket);
                    }
                } catch (IOException e) {
                    Log.w(TAG, "Error occurred when trying to accept client.", e);
                    break;
                }
            }
        }

        private void stopBtHost() {
            try {
                mmRunning = false;
                mHostThread = null;
                if (mmBtServerSocket != null) mmBtServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could'nt close bluetooth socket", e);
            }
        }
    }

    /**
     * BluetoothClientThread used to connect to a bluetooth host device.
     */
    private class BtClientThread extends Thread {
        private final BluetoothDevice mmDevice;
        private final BluetoothSocket mmSocket;
        private boolean closing;

        BtClientThread(BluetoothDevice bTDevice) {
            mmDevice = bTDevice;
            BluetoothSocket tmpSocket = null;
            try {
                tmpSocket = mmDevice.createRfcommSocketToServiceRecord(mAppUUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mmSocket = tmpSocket;
            closing = false;
        }

        @Override
        public void run() {
            if (!mConnected) {
                mBtAdapter.cancelDiscovery();

                try {
                    mmSocket.connect();
                } catch (IOException e) {
                    Log.w(TAG, "Error occurred when trying to connect to bluetooth host", e);
                    if (!closing) {
                        mListener.onHostError();
                    }
                    try {
                        mmSocket.close();
                    } catch (IOException e1) {
                        Log.e(TAG, "Error occurred when trying to close socket in client thread", e);
                    }
                    return;
                }
                handleConnectedDevice(mmSocket);
            }
        }

        void stopBtClient() {
            try {
                closing = true;
                mClientThread = null;
                mmSocket.close();
            } catch (IOException e) {
                Log.w(TAG, "Error occurred trying to close bluetooth socket in client thread", e);
            }
        }
    }

    private class BtConnectedThread extends Thread {
        private final BluetoothSocket mmBtSocket;
        private final ObjectInputStream mBtOIS;
        private final ObjectOutputStream mBtOOS;

        private boolean running = true;

        BtConnectedThread(BluetoothSocket mmBtSocket) {
            this.mmBtSocket = mmBtSocket;
            ObjectInputStream tmpIn = null;
            ObjectOutputStream tmpOut = null;
            Log.d(TAG, "BtConnectedThread: ---------------CONSTRUCTOR-------------------------------");
            try {
                tmpOut = new ObjectOutputStream(mmBtSocket.getOutputStream());
                tmpIn = new ObjectInputStream(mmBtSocket.getInputStream());
            } catch (IOException e) {
                Log.e(TAG, "BtConnectedThread: Error occurred trying to create output/input streams", e);
            }
            mBtOIS = tmpIn;
            mBtOOS = tmpOut;

            // Dont notify if not connected to device.
            if (mBtOOS != null && mBtOIS != null ) {
                Log.d(TAG, "BtConnectedThread:------------------------------ CONNECTED---------------------------------" );
                mListener.onBluetoothConnected();
                running = true;
            } else {
                running = false;
                Log.d(TAG, "BtConnectedThread:------------------------------ DISCONNECTED---------------------------------" );
                disconnect();
            }
        }

        @Override
        public void run() {
            // Can only be used on client phone
            if (mBtGatt != null) {
                new Thread(new RssiReader()).start();
            }

            while(running) {
                try {
                    Log.d(TAG, "Listening for incoming bl");
                    mListener.onMessageReceived(mBtOIS.readObject());
                } catch (ClassNotFoundException | IOException e) {
                    if (!running) {
                        mListener.onBluetoothError();
                        Log.e(TAG, "Error when listening for incoming object.", e);
                    }
                    break;
                }
            }
            disconnect();
        }

        void write(Object obj) {
            try {
                mBtOOS.writeObject(obj);
            } catch (IOException e) {
                Log.e(TAG, "Error when sending object", e);
            }
        }

        void disconnect() {
            Log.i(TAG, "Closing bluetooth connected socket");
            running = false;
            try {
                mBtOOS.close();
                mBtOIS.close();
                mmBtSocket.close();
                if (mBtGatt != null) mBtGatt.disconnect();
                mListener.onBluetoothDisconnected(null);
            } catch (IOException e) {
                mListener.onBluetoothDisconnected(e);
                Log.w(TAG, "Error when closing connected socket", e);
            }
            mConnected = false;
        }
    }

    private class RssiReader implements Runnable {
        @Override
        public void run() {
            while(mConnected) {
                try {
                    mBtGatt.readRemoteRssi();
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class GattListener extends BluetoothGattCallback {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.d(TAG, "onConnectionStateChange: " + status + " -> " + newState);

            if (status != BluetoothGatt.GATT_SUCCESS) {
                gatt.disconnect();
            } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                Log.d(TAG, "onConnectionStateChange: Disconnected from device");
            }
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            mRssiTotal -= mRssiAverage[mRssiAverage.length - 1];
            for (int i = 0; i < mRssiAverage.length - 1; i++) {
                mRssiAverage[i+1] = mRssiAverage[i];
            }
            mRssiAverage[0] = Math.abs(rssi);
            mRssiTotal += rssi;

            mAverageRssi = mRssiTotal / mRssiAverage.length;
        }
    }
}
