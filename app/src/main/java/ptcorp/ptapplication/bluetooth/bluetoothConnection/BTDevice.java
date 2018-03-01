package ptcorp.ptapplication.bluetooth.bluetoothConnection;

import android.bluetooth.BluetoothDevice;

/**
 * Created by oskarg on 2018-02-24.
 */

public class BTDevice {

    private String mDeviceName;
    private short mDeviceRSSI;
    private BluetoothDevice mBtDevice;

    public BTDevice(String deviceName, short deviceRSSI, BluetoothDevice btDevice) {
        this.mDeviceName = deviceName;
        this.mDeviceRSSI = deviceRSSI;
        this.mBtDevice = btDevice;
    }

    public String getDeviceName() {
        return mDeviceName;
    }

    public short getDeviceRSSI() {
        return mDeviceRSSI;
    }

    public BluetoothDevice getBtDevice() {
        return mBtDevice;
    }
}
