package ptcorp.ptapplication.bluetooth.bluetoothConnection;

import java.util.List;

/**
 * Created by oskarg on 2018-03-01.
 */

public interface DeviceSearchListener {
    void onSearchComplete(List<BTDevice> devices);
}
