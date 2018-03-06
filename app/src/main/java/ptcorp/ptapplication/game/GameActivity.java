package ptcorp.ptapplication.game;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

import java.util.Random;

import ptcorp.ptapplication.R;
import ptcorp.ptapplication.bluetooth.bluetoothConnection.BTDevice;
import ptcorp.ptapplication.bluetooth.bluetoothConnection.BluetoothController;
import ptcorp.ptapplication.bluetooth.bluetoothConnection.BtServiceListener;
import ptcorp.ptapplication.bluetooth.bluetoothConnection.DeviceSearchListener;
import ptcorp.ptapplication.game.Sensors.SensorListener;
import ptcorp.ptapplication.game.enums.GameState;
import ptcorp.ptapplication.game.fragments.ConnectFragment;
import ptcorp.ptapplication.game.fragments.GameFragment;
import ptcorp.ptapplication.game.fragments.LoadingFragment;
import ptcorp.ptapplication.game.fragments.ServerConnectFragment;
import ptcorp.ptapplication.game.pojos.GameSettings;

public class GameActivity extends AppCompatActivity implements ConnectFragment.ConnectFragmentListener, DeviceSearchListener, BtServiceListener,
        ServerConnectFragment.DeviceListListener, SensorListener.SensorResult {
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

    private SensorListener mSensorListener;
    private SensorManager mSensorManager;
    private Sensor mAccelerometerSensor, mMagneticSensor;
    private boolean isStriking, hasAccelerometerSensor, hasMagneticSensor;
    private float[] mLastAccelerometer = new float[3];
    private boolean mLastAccelerometerSet;
    private float[] mLastMagnetometer = new float[3];
    private boolean mLastMagnetometerSet;
    private long lastUpdateTime;
    private float[] mRotationMatrix = new float[16];
    private float[] mOrientation = new float[3];
    private float mCurrentDegree;
    private ImageView mCompass;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        mFragmentManager = getSupportFragmentManager();
        setConnectFragment();

        mBtController = new BluetoothController(this);
        mBtController.setSearchListener(this);

        mBtController.bindBluetoothService();
        mCompass = findViewById(R.id.ivCompass);
        setupSensors();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!mBtController.enableBluetooth()) {
            Log.w(TAG, "onResume: No bluetooth module available");
            mConnectFragment.disableButtons();
            Snackbar.make(findViewById(R.id.gameContainer), R.string.bluetooth_not_available, Snackbar.LENGTH_INDEFINITE);
        }
        if(hasMagneticSensor && hasAccelerometerSensor){
            mSensorManager.registerListener(mSensorListener, mAccelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
            mSensorManager.registerListener(mSensorListener, mMagneticSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onPause() {
        mBtController.unBindBluetoothService();
        if(hasAccelerometerSensor && hasMagneticSensor){
            mSensorManager.unregisterListener(mSensorListener);
        }
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
            mBtController.startHostThread();

            loadingFragment = new LoadingFragment();
            loadingFragment.setTitle("Waiting for opponent");
            loadingFragment.setCancelable(false);
            loadingFragment.show(mFragmentManager, "loadingFragment");
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }


    private void decideServer(GameSettings gameSettings){
        mGameSettings = gameSettings;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mGameFragment.hideInitGame();
                Log.d(TAG, "run: " + mGameSettings.getmPlayerStarting());
                if (mIsHost && mGameSettings.getmPlayerStarting() == GameActivity.HOST_STARTS) {
                    mGameFragment.serveDialog();
                } else if (!mIsHost && mGameSettings.getmPlayerStarting() == GameActivity.CLIENT_STARTS) {
                    mGameFragment.serveDialog();
                }
            }
        });
    }

    private void setupSensors(){
        mSensorListener = new SensorListener(this);
        mSensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);

        if(mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null){
            mAccelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            hasAccelerometerSensor = true;
        } else{
            hasAccelerometerSensor = false;
        }
        if(mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null){
            mMagneticSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
            hasMagneticSensor = true;
        } else{
            hasMagneticSensor = false;
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

    private void startGame() {
        Log.d(TAG, "startGame: HEJ");
        Random rnd = new Random();
        int whoStarts = rnd.nextInt(2);
        Log.d(TAG, "startGame: VALUE:--------------" + whoStarts);
        if (whoStarts == GameActivity.HOST_STARTS) {
            mGameSettings = new GameSettings(GameActivity.HOST_STARTS);
        } else {
            mGameSettings = new GameSettings(GameActivity.CLIENT_STARTS);
        }
        mBtController.write(mGameSettings);
        decideServer(mGameSettings);
    }


    @Override
    public void host() {
        mIsHost = true;
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
        Log.d(TAG, "onMessageReceived: " + obj.getClass().getName());
        if (obj instanceof GameState) {
            mOtherDeviceState = (GameState)obj;
            Log.d(TAG, "onMessageReceived: FIRST IF -------------------------");
            if (GameState.DEVICE_READY.equals(mOtherDeviceState) &&
                    GameState.DEVICE_READY.equals(mThisDeviceState)) {
                Log.d(TAG, "onMessageReceived: SECOND IF---------------------- ");
                if (mIsHost) {
                    Log.d(TAG, "onMessageReceived: Start gmae about to be called-..........................------------------");
                    startGame();
                    runOnUiThread(new Runnable() {
                       @Override
                       public void run() {
                           mGameFragment.hideInitGame();
                       }
                   });
                }
            }
        } else if(obj instanceof GameSettings) {
            decideServer((GameSettings) obj);
        }
    }

    @Override
    public void onHostError() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mConnectFragment != null) mConnectFragment.showHostNotStartedError();
            }
        });
    }

    @Override
    public void onBluetoothError() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!mIsHost) {
                    if (mConnectFragment != null)
                        mConnectFragment.showHostNotStartedError();
                }
                else {
                    if (mConnectFragment != null)
                        mConnectFragment.showNotConnectedError();
                }
            }
        });
    }

    @Override
    public void onDeviceClick(BluetoothDevice btDevice) {
        mBtController.pairDevice(btDevice);
    }

    @Override
    public void onUpdate(SensorEvent event) {
        if (event.sensor == mAccelerometerSensor) {
            System.arraycopy(event.values, 0, mLastAccelerometer, 0,
                    event.values.length);
            mLastAccelerometerSet = true;
        } else if (event.sensor == mMagneticSensor) {
            System.arraycopy(event.values, 0, mLastMagnetometer, 0,
                    event.values.length);
            mLastMagnetometerSet = true;
        }//only 4 times in 1 second
        if (mLastAccelerometerSet && mLastMagnetometerSet &&
                System.currentTimeMillis() - lastUpdateTime > 250) {
            SensorManager.getRotationMatrix(mRotationMatrix, null,
                    mLastAccelerometer, mLastMagnetometer);
            SensorManager.getOrientation(mRotationMatrix, mOrientation);
            float azimuthInRadians = mOrientation[0];
            float azimuthInDegress = (float)
                    (Math.toDegrees(azimuthInRadians) + 360) % 360;
            RotateAnimation mRotateAnimation = new RotateAnimation(
                    mCurrentDegree, -azimuthInDegress,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f);
            mRotateAnimation.setDuration(250);
            mRotateAnimation.setFillAfter(true);
//            mCompass.startAnimation(mRotateAnimation);
            if(mGameFragment != null){
                mGameFragment.rotateCompass(mRotateAnimation);
            }
            mCurrentDegree = -azimuthInDegress;
            lastUpdateTime = System.currentTimeMillis();
        }
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
