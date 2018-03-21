package ptcorp.ptapplication.bluetooth.bluetoothConnection;

import android.bluetooth.BluetoothDevice;

/**
 * Created by oskarg on 2018-02-24.
 *
 */

public class BTDevice {

    private String mDeviceName;
    private BluetoothDevice mBtDevice;

    BTDevice(String deviceName, BluetoothDevice btDevice) {
        this.mDeviceName = deviceName;
        this.mBtDevice = btDevice;
    }

    public String getDeviceName() {
        return mDeviceName;
    }

    public BluetoothDevice getBtDevice() {
        return mBtDevice;
    }
}
