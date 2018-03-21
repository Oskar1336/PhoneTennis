package ptcorp.ptapplication.bluetooth.bluetoothConnection;

/**
 * Created by oskarg on 2018-03-01.
 *
 */

public interface DeviceSearchListener {
    void onDeviceFound(BTDevice device);
    void onSearchComplete();
}
