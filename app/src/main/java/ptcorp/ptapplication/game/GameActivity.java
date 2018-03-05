package ptcorp.ptapplication.game;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.util.Random;

import ptcorp.ptapplication.R;
import ptcorp.ptapplication.bluetooth.bluetoothConnection.BTDevice;
import ptcorp.ptapplication.bluetooth.bluetoothConnection.BluetoothController;
import ptcorp.ptapplication.bluetooth.bluetoothConnection.BtServiceListener;
import ptcorp.ptapplication.bluetooth.bluetoothConnection.DeviceSearchListener;
import ptcorp.ptapplication.game.enums.GameState;
import ptcorp.ptapplication.game.fragments.ConnectFragment;
import ptcorp.ptapplication.game.fragments.GameFragment;
import ptcorp.ptapplication.game.fragments.LoadingFragment;
import ptcorp.ptapplication.game.fragments.ServerConnectFragment;
import ptcorp.ptapplication.game.pojos.GameSettings;

public class GameActivity extends AppCompatActivity implements ConnectFragment.ConnectFragmentListener, DeviceSearchListener, BtServiceListener, ServerConnectFragment.DeviceListListener {
    private static final String TAG = "GameActivity";
    public static final int HOST_STARTS = 1;
    public static final int CLIENT_STARTS = 0;

    private FragmentManager mFragmentManager;
    private FragmentTransaction mFragmentTransaction;
    private ServerConnectFragment serverConnectFragment;
    private ConnectFragment mConnectFragment;
    private LoadingFragment loadingFragment;

    private BluetoothController mBtController;
    private GameFragment mGameFragment;

    private GameState mOtherDeviceState;
    private GameState mThisDeviceState;
    private boolean mIsHost = false;
    private GameSettings mGameSettings;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        mFragmentManager = getSupportFragmentManager();
        setConnectFragment();

        mBtController = new BluetoothController(this);
        mBtController.setSearchListener(this);

        mBtController.bindBluetoothService();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!mBtController.enableBluetooth()) {
            Log.w(TAG, "onResume: No bluetooth module available");
            mConnectFragment.disableButtons();
            Snackbar.make(findViewById(R.id.gameContainer), R.string.bluetooth_not_available, Snackbar.LENGTH_INDEFINITE);
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
            mBtController.startHostThread();
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void setConnectFragment(){
        if (mConnectFragment == null) {
            mConnectFragment = new ConnectFragment();
        }

        mFragmentTransaction = mFragmentManager.beginTransaction();
        mFragmentTransaction.replace(R.id.gameContainer, mConnectFragment, "connectFragment");
        mFragmentTransaction.commit();
    }

    @Override
    public void host() {
        mIsHost = true;
        loadingFragment = new LoadingFragment();
        loadingFragment.setTitle("Waiting for opponent");
        loadingFragment.setCancelable(false);
        loadingFragment.show(mFragmentManager, "loadingFragment");

        mBtController.enableDiscoverable();
    }

    @Override
    public void connect() {
        mIsHost = false;
        mBtController.startSearchingForDevices();

        serverConnectFragment = new ServerConnectFragment();
        serverConnectFragment.show(mFragmentManager, "serverConnectFragment");
        serverConnectFragment.setListener(GameActivity.this);
    }

    @Override
    public void onDeviceFound(BTDevice device) {
        serverConnectFragment.addItemToList(device);
    }

    @Override
    public void onSearchComplete() {
        serverConnectFragment.updateComplete();
    }


    @Override
    public void onBluetoothConnected() {
        // TODO: 2018-03-01 Continue to gamescreen
//        loadingFragment.dismiss();
//        mGameFragment = new GameFragment();
//        mFragmentTransaction.replace(R.id.gameContainer, mGameFragment, "GameFragment").commit();
        this.runOnUiThread(new RunOnUI());



        Log.d(TAG, "onBluetoothConnected: Connected");
    }

    @Override
    public void onBluetoothDisconnected(Exception e) {
        // TODO: 2018-03-01 Notify user and give choice of either reconnect or just go back to MainActivity
        Log.d(TAG, "onBluetoothDisconnected: Disconnected");
    }

    @Override
    public void onMessageReceived(Object obj) {
        if (obj instanceof GameState) {
            mOtherDeviceState = (GameState)obj;

            if(mOtherDeviceState == null){
                Log.d(TAG, "onMessageReceived: otherdeivce null");
            }

            if(mThisDeviceState == null){
                Log.d(TAG, "onMessageReceived:this= null");
            }

//            Log.d(TAG, "onMessageReceived: " + mOtherDeviceState.name() + " THIS: " + mThisDeviceState.name());
            Log.d(TAG, "onMessageReceived: -------------------First IF----------------");


            if (GameState.DEVICE_READY.equals(mOtherDeviceState) &&
                    GameState.DEVICE_READY.equals(mThisDeviceState)) {
                Log.d(TAG, "onMessageReceived: -------------------Second IF----------------");
                if (mIsHost) {
                    Log.d(TAG, "onMessageReceived: -------------------Third IF----------------");
                    startGame();
                    mGameFragment.hideInitGame();
                }
            }
        } else if(obj instanceof GameSettings) {
            Log.d(TAG, "onMessageReceived: -------------------First ELSE-IF----------------");
            mGameSettings = (GameSettings)obj;
            mGameFragment.hideInitGame();
        } else{
            Log.d(TAG, "onMessageReceived: -------------------NONE----------------");
        }
    }

    private void startGame() {
        Random rnd = new Random();
        if (rnd.nextInt(1) == GameActivity.HOST_STARTS) {
            mGameSettings = new GameSettings(GameActivity.HOST_STARTS);
        } else {
            mGameSettings = new GameSettings(GameActivity.CLIENT_STARTS);
        }
        mBtController.write(mGameSettings);
    }

    @Override
    public void onDeviceClick(BluetoothDevice btDevice) {
        mBtController.pairDevice(btDevice);
    }

    private class RunOnUI implements Runnable{
        @Override
        public void run() {
            mFragmentTransaction = mFragmentManager.beginTransaction();
            if(mIsHost){
                loadingFragment.dismiss();
            } else{
                serverConnectFragment.dismiss();
            }
            mGameFragment = new GameFragment();
            mFragmentTransaction.replace(R.id.gameContainer, mGameFragment, "GameFragment").commit();

            mThisDeviceState = GameState.DEVICE_READY;

            Handler loadingTimer = new Handler();
            loadingTimer.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "run: " + mThisDeviceState.name());
                    mBtController.write(GameState.DEVICE_READY);
                }
            }, 4000);
        }
    }
}
