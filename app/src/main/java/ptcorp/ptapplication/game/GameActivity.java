package ptcorp.ptapplication.game;

import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Vibrator;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

import ptcorp.ptapplication.R;
import ptcorp.ptapplication.bluetooth.bluetoothConnection.BTDevice;
import ptcorp.ptapplication.bluetooth.bluetoothConnection.BluetoothController;
import ptcorp.ptapplication.bluetooth.bluetoothConnection.BtServiceListener;
import ptcorp.ptapplication.bluetooth.bluetoothConnection.DeviceSearchListener;
import ptcorp.ptapplication.game.Sensors.SensorListener;
import ptcorp.ptapplication.game.enums.GameState;
import ptcorp.ptapplication.game.enums.GameWinner;
import ptcorp.ptapplication.game.fragments.ConnectFragment;
import ptcorp.ptapplication.game.fragments.GameFragment;
import ptcorp.ptapplication.game.fragments.LoadingFragment;
import ptcorp.ptapplication.game.fragments.ServerConnectFragment;
import ptcorp.ptapplication.game.pojos.GameSettings;
import ptcorp.ptapplication.game.pojos.InitGame;
import ptcorp.ptapplication.game.pojos.PlayerPositions;
import ptcorp.ptapplication.game.pojos.RoundResult;
import ptcorp.ptapplication.game.pojos.StrikeInformation;
import ptcorp.ptapplication.main.MainActivity;
import ptcorp.ptapplication.main.pojos.GameScore;


public class GameActivity extends AppCompatActivity implements ConnectFragment.ConnectFragmentListener, DeviceSearchListener, BtServiceListener,
        ServerConnectFragment.DeviceListListener, SensorListener.SensorResult, GameFragment.GameListener {
    private static final String TAG = "GameActivity";
    public static final int HOST_STARTS = 1;
    public static final int CLIENT_STARTS = 0;
    private final static float ERROR_MARGIN = 20;
    public final static String GAME_RESULT = "GameActivity.GAME_RESULT";
    public final static int GAME_RESULT_CODE = 1;

    private final static short STRIKE_FORWARD_LIMIT = 10;
    private final static short STRIKE_TILT_LIMIT = 5;
    private final static short STRIKE_BACKWARDS_LIMIT = -2;
    private final static short STRIKE_STRENGTH_LIMIT = 31;

    private String username, opponentUsername;


    private FragmentManager mFragmentManager;
    private FragmentTransaction mFragmentTransaction;
    private ServerConnectFragment serverConnectFragment;
    private ConnectFragment mConnectFragment;
    private LoadingFragment loadingFragment;

    private BluetoothController mBtController;
    private GameFragment mGameFragment;

    private GameState mThisDeviceState;
    private boolean mIsHost = false;
    private GameSettings mGameSettings;
    private GameWinner mGameWinner;

    private SensorListener mSensorListener;
    private SensorManager mSensorManager;
    private Sensor mAccelerometerSensor, mMagneticSensor;
    private boolean hasAccelerometerSensor, hasMagneticSensor;
    private Vibrator v;
    private long[] vibratePattern = {200,100,200,100,200,100};

    private float[] mLastAccelerometer = new float[3];
    private boolean mLastAccelerometerSet;
    private float[] mLastMagnetometer = new float[3];
    private boolean mLastMagnetometerSet;

    private long lastUpdateTime;
    private float[] mRotationMatrix = new float[16];
    private float[] mOrientation = new float[3];
    private float mCurrentDegree;

    private RoundResult mRoundResult;
    private float degree;
    private boolean mTimeToStrike;
    private Handler uiHandler;
    private float strikeDirection;
    private float moveToPosition;
    private boolean mGameStarted;

    private Runnable handlerRunnable;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        mFragmentManager = getSupportFragmentManager();
        setConnectFragment();

        mGameStarted = false;
        mRoundResult = new RoundResult();

        mBtController = new BluetoothController(this);
        mBtController.setSearchListener(this);
        uiHandler = new Handler();
        v = (Vibrator) getSystemService(VIBRATOR_SERVICE);
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
        uiHandler.removeCallbacks(handlerRunnable);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (mGameStarted) {
            if(mIsHost && !mRoundResult.isGameOver()){
                mRoundResult.setClientWinner();
                mGameWinner = GameWinner.CLIENTWON;
            } else if(!mIsHost && !mRoundResult.isGameOver()){
                mRoundResult.setHostWinner();
                mGameWinner = GameWinner.HOSTWON;
            }
            mBtController.write(mRoundResult);
            this.onGameFinished();
        }
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == BluetoothController.BLUETOOTH_DISCOVERABLE_REQUEST_CODE && resultCode == 200) {
            mBtController.startHostThread();

            loadingFragment = new LoadingFragment();
            loadingFragment.setListener(new LoadingFragment.LoadingDialogListener() {
                @Override
                public void onLoadingCancel() {
                    mBtController.stopHostThread();
                }
            });
            loadingFragment.setTitle(getString(R.string.waiting_for_opponent));
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

                if (mIsHost && mGameSettings.getmPlayerStarting() == GameActivity.HOST_STARTS) {
                    mGameFragment.serveDialog();
                } else if (!mIsHost && mGameSettings.getmPlayerStarting() == GameActivity.CLIENT_STARTS) {
                    mGameFragment.serveDialog();
                }
                if(mIsHost && mGameSettings.getmPlayerStarting() == CLIENT_STARTS){
                    mGameFragment.showWaitingForServeDialog();
                } else if(!mIsHost && mGameSettings.getmPlayerStarting() == HOST_STARTS){
                    mGameFragment.showWaitingForServeDialog();
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
        int whoStarts = rnd.nextInt(2);
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

        if (xVal > STRIKE_FORWARD_LIMIT && (yVal < STRIKE_TILT_LIMIT && yVal > STRIKE_BACKWARDS_LIMIT)) {

            if (xVal > STRIKE_STRENGTH_LIMIT) {
                sendLost(RoundResult.RoundLostReason.SHOT_OUT_OF_BOUNDS);
            } else {
                // Vibrate for 500 milliseconds
                v.vibrate(500);
                mBtController.write(new StrikeInformation(
                        ((mBtController.getDistanceFromConnectedDevice() / event.values[0]) * 10) + 1,
                        strikeDirection));
            }
            return false;
        }
        return true;
    }

    private void showHostNotStartedError() {
        if(!mRoundResult.isGameOver()){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.host_error);
            builder.setMessage(R.string.host_error_explanation);
            builder.setPositiveButton(R.string.close, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    finish();
                }
            });
            builder.create().show();
        }
    }

    private void showNotConnectedError() {
        if(!mRoundResult.isGameOver()){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.bluetooth_error);
            builder.setMessage(R.string.bluetooth_error_explination);
            builder.setPositiveButton(R.string.close, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    finish();
                }
            });
            builder.create().show();
        }
    }

    private void showBluetoothNotAvailableError() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.bluetooth_error);
        builder.setMessage(R.string.bluetooth_not_activated);
        builder.setPositiveButton(R.string.close, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
            }
        });
        builder.create().show();
    }

    //Loser sends this
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
            String message;
            if(mRoundResult.getClientPoints() == RoundResult.GAME_POINTS){
                message = getText(R.string.client_is_winner).toString();
                mGameWinner = GameWinner.CLIENTWON;
            } else  {
                message = getText(R.string.host_is_winner).toString();
                mGameWinner = GameWinner.HOSTWON;
            }
            mGameFragment.showMatchResult(message);
        } else{
            mGameFragment.showRoundMessage(getString(R.string.you_lost_the_point));
            uiHandler.postDelayed((handlerRunnable =new Runnable() {
                 @Override
                 public void run() {
                     mGameFragment.dismissRoundMessage();
                     mGameFragment.serveDialog();
                 }
            }), 2500);
        }
        mBtController.write(mRoundResult);
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
        this.runOnUiThread(new RunOnUI());
    }

    @Override
    public void onBluetoothDisconnected(Exception e) {
        // TODO: 2018-03-01 Notify user and give choice of either reconnect or just go back to MainActivity
        Log.i(TAG, "onBluetoothDisconnected: Disconnected");
    }

    @Override
    public void onMessageReceived(Object obj) {
        if (obj instanceof InitGame) {
            GameState mOtherDeviceState = ((InitGame) obj).getGameState();
            opponentUsername = ((InitGame) obj).getOpponentName();
            if (GameState.DEVICE_READY.equals(mOtherDeviceState) &&
                    GameState.DEVICE_READY.equals(mThisDeviceState)) {
                if (mIsHost) {
                       mGameFragment.hideInitGame();
                       mGameFragment.lockOpponentDirectionDialog();
                    mGameFragment.setHostname(username);
                    mGameFragment.setClientname(opponentUsername);
                } else{
                    mGameFragment.setHostname(opponentUsername);
                    mGameFragment.setClientname(username);
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
            mGameStarted = true;
        } else if(obj instanceof StrikeInformation){
            mGameFragment.hideWaitingForServe();

            StrikeInformation strikeInformation = (StrikeInformation)obj;
            float opponentStrike = strikeInformation.getDirection();
            moveToPosition = degree;
            moveToPosition -= opponentStrike;

            v.vibrate(vibratePattern,-1);

            if(opponentStrike < 0) {
                mGameFragment.showNewDegree(getString(R.string.shot_to_your_left, Math.abs(opponentStrike)));
            } else if(opponentStrike > 0) {
                mGameFragment.showNewDegree(getString(R.string.shot_to_your_right, Math.abs(opponentStrike)));
            } else {
                mGameFragment.showNewDegree(getString(R.string.shot_at_you));
            }
            uiHandler.postDelayed((handlerRunnable = new Runnable() {
                @Override
                public void run() {
                    if (mCurrentDegree<=((moveToPosition+ERROR_MARGIN)%360) && mCurrentDegree>=((moveToPosition-ERROR_MARGIN)%360)){
                        mGameFragment.strikeDialog();
                    } else {
                        sendLost(RoundResult.RoundLostReason.MISSED_BALL);
                    }
                }
            }), (long) (strikeInformation.getTimeInAir() * 1000));
        } else if (obj instanceof RoundResult){
            mRoundResult = (RoundResult)obj;
            mGameFragment.updateClientPoints(mRoundResult.getClientPoints());
            mGameFragment.updateHostPoints(mRoundResult.getHostPoints());

            if (mRoundResult.isGameOver()){
                String message;
                if(mRoundResult.getClientPoints() == RoundResult.GAME_POINTS){
                    message = getText(R.string.client_is_winner).toString();
                    mGameWinner = GameWinner.CLIENTWON;
                } else  {
                    message = getText(R.string.host_is_winner).toString();
                    mGameWinner = GameWinner.HOSTWON;
                }
                mGameFragment.showMatchResult(message);
            }else{
                mGameFragment.showRoundMessage(getString(R.string.you_won_the_ball));
                uiHandler.postDelayed((handlerRunnable = new Runnable() {
                    @Override
                    public void run() {
                        mGameFragment.dismissRoundMessage();
                        mGameFragment.showWaitingForServeDialog();
                    }
                }),1500);
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
    }

    @Override
    public void onBluetoothError() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mGameFragment.hideWaitingForServe();
                if (mBtController.isBluetoothAvailable()) {
                    if (!mIsHost) {
                        showHostNotStartedError();
                    } else {
                        showNotConnectedError();
                    }
                } else {
                    showBluetoothNotAvailableError();
                }
            }
        });
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
    public void onReSearch() {
        mBtController.startSearchingForDevices();
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
            mCurrentDegree = -azimuthInDegress;

            if(mGameFragment != null){
                mGameFragment.rotateCompass(mRotateAnimation);
                mGameFragment.setCurrentDegree(mCurrentDegree);
            }
            lastUpdateTime = System.currentTimeMillis();
        }
    }

    @Override
    public void onLock() {
        PlayerPositions playerPositions = new PlayerPositions();
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
        mGameFragment.setStartDegree(degree);
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

    @Override
    public void onGameFinished() {
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
        Date today = Calendar.getInstance().getTime();
        String stringDate = df.format(today);
        GameScore gameScore;

        if (mIsHost) {
            gameScore = new GameScore(username, opponentUsername, stringDate, mRoundResult.getHostPoints(), mRoundResult.getClientPoints());
        }else{
            gameScore = new GameScore(opponentUsername, username, stringDate, mRoundResult.getHostPoints(), mRoundResult.getClientPoints());
        }
        gameScore.setGameWinner(mGameWinner);
        Intent resultIntent = getIntent();
        resultIntent.putExtra(GAME_RESULT, gameScore);
        setResult(GAME_RESULT_CODE, resultIntent);
        finish();
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
            username = getIntent().getStringExtra(MainActivity.USERNAME);
            final InitGame initGame = new InitGame(GameState.DEVICE_READY, username);

            Handler loadingTimer = new Handler();
            loadingTimer.postDelayed((handlerRunnable = new Runnable() {
                @Override
                public void run() {
                    mBtController.write(initGame);
                }
            }), 4000);
        }
    }
}
