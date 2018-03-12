package ptcorp.ptapplication.main;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.widget.Toast;

import com.facebook.stetho.Stetho;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import ptcorp.ptapplication.database.FirebaseDatabaseHandler;
import ptcorp.ptapplication.game.GameActivity;
import ptcorp.ptapplication.game.Sensors.SensorListener;
import ptcorp.ptapplication.game.enums.GameWinner;
import ptcorp.ptapplication.main.adapters.GamesAdapter;
import ptcorp.ptapplication.database.GamesDatabaseHandler;
import ptcorp.ptapplication.main.adapters.LeaderboardAdapter;
import ptcorp.ptapplication.main.fragments.CalibrateDialogFragment;
import ptcorp.ptapplication.main.fragments.CalibrateStrikeFragment;
import ptcorp.ptapplication.main.fragments.GamesFragment;
import ptcorp.ptapplication.main.fragments.HomeFragment;
import ptcorp.ptapplication.main.fragments.LeaderboardFragment;
import ptcorp.ptapplication.main.fragments.LoginFragment;
import ptcorp.ptapplication.R;
import ptcorp.ptapplication.main.pojos.GameScore;
import ptcorp.ptapplication.main.pojos.LeaderboardScore;

public class MainActivity extends AppCompatActivity implements LoginFragment.LoginListener,
        CalibrateStrikeFragment.CalibrateButtonListener, SensorListener.SensorResult, FirebaseDatabaseHandler.OnDatabaseUpdateListener,
        CalibrateDialogFragment.CalibratingListener {
    private static final String TAG = "MainActivity";
    public static final String USERNAME = "username";
    public static final int REQUEST_CODE = 99;

    private final int NAV_LOGIN = 0;
    private final int NAV_HOME = 1;
    private final int NAV_MY_GAMES = 2;
    private final int NAV_LEADERBOARD = 3;
    private final int NAV_CALIBRATE_STRIKE = 4;

    private final int CALIBRATION_MAX_TRIES = 5;

    private FirebaseAuth mAuth;
    private FirebaseDatabaseHandler mHandlerDB;
    private BottomNavigationView nav;
    private ViewPager fragmentHolder;
    private LeaderboardFragment leaderboardFragment;
    private HomeFragment homeFragment;
    private LoginFragment loginFragment;
    private GamesFragment myGameFragment;
    private GamesDatabaseHandler gDB;
    private ArrayList<LeaderboardScore> mLeaderboardList;

    private ActionBar mActionBar;
    private Menu mOptMenu;

    private CalibrateDialogFragment mCalibrateDialog;

    private SensorManager mSensorManager;
    private SensorListener mSensorListener;
    private Sensor mAccelerometerSensor;
    private boolean mHasAccelerometer;
    private boolean mAccelerometerActive;
    private boolean mAccBackStarted;
    private boolean mStrikeOver;
    private float mCurHardestStrikeX = 0f;
    private short mCurTry = 0;
    private float[] mStrikeXArr = new float[CALIBRATION_MAX_TRIES];
    private final Object lockStrikeReader = new Object();

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    fragmentHolder.setCurrentItem(NAV_HOME);
                    return true;
                case R.id.navigation_myGames:
                    fragmentHolder.setCurrentItem(NAV_MY_GAMES);
                    myGameFragment.setAdapter(new GamesAdapter(gDB.getGames()));
                    return true;
                case R.id.navigation_leaderboard:
                    fragmentHolder.setCurrentItem(NAV_LEADERBOARD);
                    leaderboardFragment.setAdapter(new LeaderboardAdapter(mLeaderboardList));
                    return true;
            }
            return false;
        }
    };


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initFirebase();
        Stetho.initializeWithDefaults(this);
        gDB = new GamesDatabaseHandler(this);
        loginFragment = new LoginFragment();
        loginFragment.setListener(this);
        homeFragment = new HomeFragment();
        myGameFragment = new GamesFragment();
        leaderboardFragment = new LeaderboardFragment();

        mActionBar = getSupportActionBar();

        nav = findViewById(R.id.navigation);
        nav.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        fragmentHolder = findViewById(R.id.vpPager);
        fragmentHolder.setAdapter(new FragmentPageAdapter(getSupportFragmentManager()));
        fragmentHolder.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true;
            }
        });
        fragmentHolder.addOnPageChangeListener(new NavListener());

        fragmentHolder.setCurrentItem(NAV_LOGIN);

        mSensorListener = new SensorListener(MainActivity.this);
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);

        if (mSensorManager != null) {
            if ((mAccelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)) != null)
                mHasAccelerometer = true;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mOptMenu = menu;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu, menu);
        mOptMenu.setGroupVisible(R.id.opt_menu, false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.opt_calibrate_strike) {
            fragmentHolder.setCurrentItem(NAV_CALIBRATE_STRIKE);
        }
        return true;
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
//        FirebaseUser currentUser = mAuth.getCurrentUser(); // null if not signed in
    }

    @Override
    protected void onDestroy() {
        FirebaseUser user = mAuth.getCurrentUser();
        if(user != null){
            mAuth.signOut();
        }

        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (fragmentHolder.getCurrentItem() == NAV_CALIBRATE_STRIKE) {
            fragmentHolder.setCurrentItem(NAV_HOME);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult: Går in i onActivityResult" + requestCode);
        if(resultCode == GameActivity.GAME_RESULT_CODE){
            Log.d(TAG, "onActivityResult: Lägger till i databas");
            GameScore gameScore = data.getParcelableExtra(GameActivity.GAME_RESULT);
            gDB.addGame(gameScore);
            LeaderboardScore score = new LeaderboardScore();
            Log.d(TAG, "onActivityResult: " + mHandlerDB.getUsername());
            Log.d(TAG, "onActivityResult: " + gameScore.getPlayer1());
            Log.d(TAG, "onActivityResult: " + gameScore.getPlayer2());

            if(mHandlerDB.getUsername().equals(gameScore.getPlayer1())){// HOST
                score.setUsername(gameScore.getPlayer1());
                if(gameScore.getGameWinner().equals(GameWinner.HOSTWON)){
                    score.setWonGames(1);
                }else{
                    score.setWonGames(0);
                }
            } else{
                score.setUsername(gameScore.getPlayer2());
                if(gameScore.getGameWinner().equals(GameWinner.CLIENTWON)){
                    score.setWonGames(1);
                }else{
                    score.setWonGames(0);
                }
            }
            mHandlerDB.updateScoreForUser(score);
        }
    }

    private void createUser(String email, String password){
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            loginFragment.setCreateBtnProgress(100);
                            // Sign in success, update UI with the signed-in user's information
                            mHandlerDB = new FirebaseDatabaseHandler(mAuth);
                            mHandlerDB.addOnUpdateListener(MainActivity.this);
                            fragmentHolder.setCurrentItem(NAV_HOME);
                            homeFragment.setUsername(mHandlerDB.getUsername());
                            nav.setVisibility(View.VISIBLE);
                            displayToast("Account created and logged in!");
                        } else {
                            loginFragment.setCreateBtnProgress(-1);
                            new ButtonHandler().execute();
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void signInUser(String email, String password){
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            loginFragment.setLoginBtnProgress(100);
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");

                            mHandlerDB = new FirebaseDatabaseHandler(mAuth);
                            mHandlerDB.addOnUpdateListener(MainActivity.this);
                            fragmentHolder.setCurrentItem(NAV_HOME);
                            homeFragment.setUsername(mHandlerDB.getUsername());
                            nav.setVisibility(View.VISIBLE);
                            displayToast("Logged in!");
                        } else {
                            loginFragment.setLoginBtnProgress(-1);
                            new ButtonHandler().execute();
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void displayToast(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void initFirebase(){
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void login(String user, String pass) {
        signInUser(user,pass);
    }

    @Override
    public void create(String user, String pass) {
        createUser(user, pass);
    }

    @Override
    public void onStartCalibrate() {
        mAccBackStarted = false;
        mCurHardestStrikeX = 0;
        mCurTry = 0;
        mStrikeXArr = new float[CALIBRATION_MAX_TRIES];

        if (mHasAccelerometer) {
            mSensorManager.registerListener(mSensorListener, mAccelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
            mAccelerometerActive = true;
        }

        mCalibrateDialog = new CalibrateDialogFragment();
        mCalibrateDialog.setListener(MainActivity.this);
        mCalibrateDialog.setCancelable(false);
        mCalibrateDialog.show(getSupportFragmentManager(), "CalibrateDialog");
    }

    @Override
    public void onCancel(boolean cancelWithResult) {
        if (cancelWithResult) {
            // TODO: 2018-03-09 save to shared preferences
        }

        if (mAccelerometerActive) {
            mSensorManager.unregisterListener(mSensorListener);
        }
        fragmentHolder.setCurrentItem(1);
    }

    @Override
    public void onUpdate(SensorEvent event) {
        float x = event.values[0];
        float y = event.values[1]; // rm
        float z = event.values[2];

        Log.d(TAG, "onUpdate: X: " + x + " / Y: " + y + " / Z: " + z);

        // Measure strike start
        if (!mAccBackStarted && (z > 8 && x < -0.5)) {
            Log.d(TAG, "onUpdate: ---- Backwards X: " + x + " / Y: " + y + " / Z: " + z);
            mAccBackStarted = true;
        } else if (mAccBackStarted && (z < -2) && x > mCurHardestStrikeX) {
            mCurHardestStrikeX = x;
            Log.d(TAG, "onUpdate: new hardest X: " + x + " / Y: " + y + " / Z: " + z);
        }

        if (mCurHardestStrikeX > x && (z < 5 && z > 0)) {
            mAccBackStarted = false;

            mStrikeXArr[mCurTry] = mCurHardestStrikeX;
            mCurHardestStrikeX = 0;

            Log.d(TAG, "onUpdate: saving hardest X: " + x + " / Y: " + y + " / Z: " + z);

//            mCurTry++;
            mCalibrateDialog.updateProgress();
        }

        if (mCurTry == CALIBRATION_MAX_TRIES) {

            // TODO: 2018-03-09 Save values
            onCancel(true);
        }
    }

    @Override
    public void onCalibrateCancel() {
        onCancel(false);
    }

    @Override
    public void updateAdapter(List<LeaderboardScore> list) {
        mLeaderboardList = (ArrayList<LeaderboardScore>) list;
    }

    private class FragmentPageAdapter extends FragmentPagerAdapter {
        FragmentPageAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position){
                case NAV_LOGIN:
                    return loginFragment;
                case NAV_HOME:
                    return homeFragment;
                case NAV_MY_GAMES:
                    return myGameFragment;
                case NAV_LEADERBOARD:
                    return leaderboardFragment;
                case NAV_CALIBRATE_STRIKE:
                    return new CalibrateStrikeFragment();
            }
            return null;
        }

        @Override
        public int getCount() {
            return 5;
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class ButtonHandler extends AsyncTask<Void, Void, Void>{
        @Override
        protected Void doInBackground(Void... voids) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            loginFragment.setLoginBtnProgress(0);
            loginFragment.setCreateBtnProgress(0);
        }
    }

    private class NavListener implements ViewPager.OnPageChangeListener {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            if (position == NAV_LOGIN || position == NAV_CALIBRATE_STRIKE) {
                nav.setVisibility(View.INVISIBLE);
            } else {
                nav.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onPageSelected(int position) {
            switch (position){
                case NAV_LOGIN:
                    if (mOptMenu != null)
                        mOptMenu.setGroupVisible(R.id.opt_menu, false);
                    mActionBar.setTitle(R.string.login);

                    break;
                case NAV_HOME:
                    mActionBar.setTitle(R.string.home);
                    if (mOptMenu != null)
                        mOptMenu.setGroupVisible(R.id.opt_menu, true);

                    break;
                case NAV_MY_GAMES:
                    mActionBar.setTitle(R.string.my_games);
                    if (mOptMenu != null)
                        mOptMenu.setGroupVisible(R.id.opt_menu, true);

                    break;
                case NAV_LEADERBOARD:
                    mActionBar.setTitle(R.string.leaderboard);
                    if (mOptMenu != null)
                        mOptMenu.setGroupVisible(R.id.opt_menu, true);

                    break;
                case NAV_CALIBRATE_STRIKE:
                    mActionBar.setTitle(R.string.calibrate_strike_sensor);
                    if (mOptMenu != null)
                        mOptMenu.setGroupVisible(R.id.opt_menu, false);

                    break;
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) { }
    }

}
