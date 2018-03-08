package ptcorp.ptapplication.game;

import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
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
import ptcorp.ptapplication.game.pojos.PlayerPositions;
import ptcorp.ptapplication.game.pojos.RoundResult;
import ptcorp.ptapplication.game.pojos.StrikeInformation;

public class GameActivity extends AppCompatActivity implements ConnectFragment.ConnectFragmentListener, DeviceSearchListener, BtServiceListener,
        ServerConnectFragment.DeviceListListener, SensorListener.SensorResult, GameFragment.GameListener {
    private static final String TAG = "GameActivity";
    public static final int HOST_STARTS = 1;
    public static final int CLIENT_STARTS = 0;
    private final static float ERROR_MARGIN = 20;

    private final static short STRIKE_FORWARD_LIMIT = 10;
    private final static short STRIKE_TILT_LIMIT = 5;
    private final static short STRIKE_BACKWARDS_LIMIT = -2;
    private final static short STRIKE_STRENGTH_LIMIT = 31;

    private final static float COMPASS_ALPHA = 0.97f;


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
    private float mAzimuth = 0f;
    private float mCurrentAzimuth = 0f;

    private long lastUpdateTime;
    private float[] mRotationMatrix = new float[16];
    private float[] mOrientation = new float[3];
    private float mCurrentDegree;

    private PlayerPositions playerPositions;
    private RoundResult mRoundResult = new RoundResult();
    private float degree;
    private ImageView mCompass;
    private boolean mTimeToStrike;
    private Handler uiHandler;
    private float strikeDirection;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        mFragmentManager = getSupportFragmentManager();
        setConnectFragment();

        mBtController = new BluetoothController(this);
        mBtController.setSearchListener(this);
        uiHandler = new Handler();
        mCompass = findViewById(R.id.ivCompass);
        setupSensors();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mBtController.onResume();

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
        mBtController.onPause();
        if (serverConnectFragment != null) serverConnectFragment.dismiss();

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
        if (requestCode == BluetoothController.BLUETOOTH_DISCOVERABLE_REQUEST_CODE &&
                resultCode == 200) {
            mBtController.startHostThread();

            loadingFragment = new LoadingFragment();
            loadingFragment.setListener(new LoadingFragment.LoadingDialogListener() {
                @Override
                public void onLoadingCancel() {
                    mBtController.stopHostThread();
                }
            });
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

        if (mSensorManager != null) {
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
        Random rnd = new Random();
//        int whoStarts = rnd.nextInt(2);
        int whoStarts = 1; // TODO: 2018-03-07 Change to random serve
        Log.d(TAG, "startGame: VALUE:--------------" + whoStarts);
        if (whoStarts == GameActivity.HOST_STARTS) {
            mGameSettings = new GameSettings(GameActivity.HOST_STARTS);
        } else {
            mGameSettings = new GameSettings(GameActivity.CLIENT_STARTS);
        }
        mBtController.write(mGameSettings);
        decideServer(mGameSettings);
    }

    private boolean performStrike(SensorEvent event) {
        float xVal = event.values[0];
        float yVal = event.values[1];
        float zVal = event.values[2];

        if (xVal > 5)
            Log.d(TAG, "performStrike: x: " + xVal + " / y: " + yVal + " / z: " + zVal);

        if (xVal > STRIKE_FORWARD_LIMIT &&
                (yVal < STRIKE_TILT_LIMIT && yVal > STRIKE_BACKWARDS_LIMIT)) {

            Log.d(TAG, "performStrike: x: " + xVal + " / y: " + yVal + " / z: " + zVal);

            if (xVal > STRIKE_STRENGTH_LIMIT) {
                sendLost(RoundResult.RoundLostReason.SHOT_OUT_OF_BOUNDS);
            } else {
                mBtController.write(new StrikeInformation(
                        ((mBtController.getDistanceFromConnectedDevice() / event.values[0]) * 10),
                        strikeDirection));
            }
            return false;
        } else if ((yVal < STRIKE_TILT_LIMIT && yVal > STRIKE_BACKWARDS_LIMIT) &&
                (zVal < STRIKE_BACKWARDS_LIMIT)) {
            // TODO: 2018-03-07 Display to loose message toast maybe
        }
        return true;
    }

    private void showHostNotStartedError() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.host_error);
        builder.setMessage(R.string.host_error_explanation);
        builder.setPositiveButton(R.string.close, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    private void showNotConnectedError() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.bluetooth_error);
        builder.setMessage(R.string.bluetooth_error_explination);
        builder.setPositiveButton(R.string.close, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    private void sendLost(RoundResult.RoundLostReason roundLostReason) {
        mRoundResult.setRoundLostReason(roundLostReason);
        if(mIsHost){
            mRoundResult.setClientPoints();
        } else{
            mRoundResult.setHostPoints();
        }
        mGameFragment.updateClientPoints(mRoundResult.getClientPoints());
        mGameFragment.updateHostPoints(mRoundResult.getHostPoints());



        if (mRoundResult.isGameOver()){

        }

        mBtController.write(mRoundResult);
        mGameFragment.showRoundMessage("You lost the point!");
        uiHandler.postDelayed(new Runnable() {
             @Override
             public void run() {
                 mGameFragment.dismissRoundMessage();
                 mGameFragment.serveDialog();
             }
        }, 2000);

    }

    @Override
    public void host() {
        mIsHost = true;
        mBtController.enableDiscoverable();
    }

    @Override
    public void connect() {
        mIsHost = false;

        serverConnectFragment = new ServerConnectFragment();
        serverConnectFragment.show(mFragmentManager, "serverConnectFragment");
        serverConnectFragment.setListener(GameActivity.this);

        mBtController.startSearchingForDevices();
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
//                    startGame();
                       mGameFragment.hideInitGame();
                       mGameFragment.lockOpponentDirectionDialog();
                }
            }
        } else if(obj instanceof GameSettings) {
            decideServer((GameSettings) obj);
        } else if(obj instanceof PlayerPositions){
                if(mIsHost){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            loadingFragment.dismiss();
                        }
                    });
                    startGame();
                } else{
                    mGameFragment.hideInitGame();
                    mGameFragment.lockOpponentDirectionDialog();
                }
        } else if(obj instanceof StrikeInformation){
            StrikeInformation strikeInformation = (StrikeInformation)obj;
            float opponentStrike = strikeInformation.getDirection();
            float moveToPosition = degree;
            moveToPosition -= opponentStrike;

            if(opponentStrike < 0){
                mGameFragment.showNewDegree("Your opponent shot " + Math.abs(opponentStrike) + " to the left");
            } else if(opponentStrike > 0){
                mGameFragment.showNewDegree("Your opponent shot " + Math.abs(opponentStrike) + " to the right");
            } else{
                mGameFragment.showNewDegree("Your opponent shot right at you!");
            }

            if (mCurrentDegree<=((moveToPosition+ERROR_MARGIN)%360) && mCurrentDegree>=((moveToPosition-ERROR_MARGIN)%360)){

                mGameFragment.strikeDialog();
            } else {
                sendLost(RoundResult.RoundLostReason.MISSED_BALL);
            }
        } else if (obj instanceof RoundResult){
            mGameFragment.showRoundMessage("You won the ball!");
            mRoundResult = (RoundResult)obj;
            mGameFragment.updateClientPoints(mRoundResult.getClientPoints());
            mGameFragment.updateHostPoints(mRoundResult.getHostPoints());
            uiHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mGameFragment.dismissRoundMessage();
                }
            },2000);

            if (mRoundResult.isGameOver()){

            }
        }
    }

    @Override
    public void onHostError() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showHostNotStartedError();
            }
        });
        // TODO: 2018-03-07 update scores before quitting
    }

    @Override
    public void onBluetoothError() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!mIsHost) {
                    showHostNotStartedError();
                } else {
                    showNotConnectedError();
                }
            }
        });
        // TODO: 2018-03-07 update scores before quitting
    }

    @Override
    public void onDeviceClick(BluetoothDevice btDevice) {
        mBtController.pairDevice(btDevice);
    }

    @Override
    public void onDeviceSearchCancel() {
        mBtController.stopSearchingForDevices();
    }

    @Override
    public void onUpdate(SensorEvent event) {
        if (event.sensor == mAccelerometerSensor) {
            System.arraycopy(event.values, 0, mLastAccelerometer, 0, event.values.length);
            mLastAccelerometerSet = true;

            if (mTimeToStrike) {
                mTimeToStrike = performStrike(event);
            }
        } else if (event.sensor == mMagneticSensor) {
            System.arraycopy(event.values, 0, mLastMagnetometer, 0, event.values.length);
            mLastMagnetometerSet = true;
        }

        if (!mTimeToStrike && mLastAccelerometerSet && mLastMagnetometerSet &&
                System.currentTimeMillis() - lastUpdateTime > 250) {

            SensorManager.getRotationMatrix(mRotationMatrix, null,
                    mLastAccelerometer, mLastMagnetometer);
            SensorManager.getOrientation(mRotationMatrix, mOrientation);

            float azimuthInRadians = mOrientation[0];
            float azimuthInDegress = (float)(Math.toDegrees(azimuthInRadians) + 360) % 360;

            RotateAnimation mRotateAnimation = new RotateAnimation(
                    mCurrentDegree, -azimuthInDegress,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f);

            mRotateAnimation.setDuration(250);
            mRotateAnimation.setFillAfter(true);

            if(mGameFragment != null){
                mGameFragment.rotateCompass(mRotateAnimation);
            }
            mCurrentDegree = -azimuthInDegress;
            lastUpdateTime = System.currentTimeMillis();
        }
    }

    @Override
    public void onLock() {
        playerPositions = new PlayerPositions();
        degree = 0;
        for(int i = 0; i<5; i++){
            degree += mCurrentDegree;
        }
        degree = degree / 5;

        if(mIsHost){
            playerPositions.setmHostPosition(degree);
        } else{
            playerPositions.setmClientPosition(degree);
        }
        mBtController.write(playerPositions);
    }

    @Override
    public void onStrike() {
        mTimeToStrike = true;
        strikeDirection = mCurrentDegree - degree;
    }

    @Override
    public void onOutOfTime() {
        sendLost(RoundResult.RoundLostReason.TOOK_TOO_LONG);
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
