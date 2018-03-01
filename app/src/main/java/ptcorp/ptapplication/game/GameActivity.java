package ptcorp.ptapplication.game;

import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.util.List;

import ptcorp.ptapplication.R;
import ptcorp.ptapplication.bluetooth.bluetoothConnection.BTDevice;
import ptcorp.ptapplication.bluetooth.bluetoothConnection.BluetoothController;
import ptcorp.ptapplication.bluetooth.bluetoothConnection.DeviceSearchListener;

public class GameActivity extends AppCompatActivity implements ConnectFragment.ConnectFragmentListener, DeviceSearchListener {
    private static final String TAG = "GameActivity";
    private FragmentManager mFragmentManager;
    private FragmentTransaction mFragmentTransaction;

    private BluetoothController mBtController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        mFragmentManager = getSupportFragmentManager();
        setConnectFragment();

        mBtController = new BluetoothController(this);
        mBtController.setSearchListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mBtController.bindBluetoothService();

        if (!mBtController.enableBluetooth()) {
            Log.w(TAG, "onResume: No bluetooth module available");
            // TODO: 2018-03-01 Show no bluetooth available error.
        }
    }

    @Override
    protected void onPause() {
        mBtController.unBindBluetoothService();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mBtController.onDestroy();
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == BluetoothController.BLUETOOTH_DISCOVERABLE_REQUEST_CODE) {
            // TODO: 2018-03-01 Show hostloading fragment here maybe
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void setConnectFragment(){
        mFragmentTransaction = mFragmentManager.beginTransaction();
        mFragmentTransaction.replace(R.id.gameContainer, new ConnectFragment(), "connectFragment");
        mFragmentTransaction.commit();
    }

    @Override
    public void host() {
        HostLoadingFragment hostLoadingFragment = new HostLoadingFragment();
        hostLoadingFragment.show(mFragmentManager, "hostLoadingFragment");

        mBtController.enableDiscoverable();
    }

    @Override
    public void connect() {
        mBtController.startSearchingForDevices();

        ServerConnectFragment serverConnectFragment = new ServerConnectFragment();
        serverConnectFragment.show(mFragmentManager, "serverConnectFragment");
    }

    @Override
    public void onSearchComplete(List<BTDevice> devices) {

    }
}
