package ptcorp.ptapplication.main;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
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

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ptcorp.ptapplication.database.FirebaseDatabaseHandler;
import ptcorp.ptapplication.game.GameActivity;
import ptcorp.ptapplication.game.enums.GameWinner;
import ptcorp.ptapplication.main.adapters.GamesAdapter;
import ptcorp.ptapplication.database.GamesDatabaseHandler;
import ptcorp.ptapplication.main.adapters.LeaderboardAdapter;
import ptcorp.ptapplication.main.fragments.GamesFragment;
import ptcorp.ptapplication.main.fragments.HomeFragment;
import ptcorp.ptapplication.main.fragments.LeaderboardFragment;
import ptcorp.ptapplication.main.fragments.LoginFragment;
import ptcorp.ptapplication.R;
import ptcorp.ptapplication.main.pojos.GameScore;
import ptcorp.ptapplication.main.pojos.LeaderboardScore;

public class MainActivity extends AppCompatActivity implements LoginFragment.LoginListener,
        FirebaseDatabaseHandler.OnDatabaseUpdateListener {
    private static final String TAG = "MainActivity";
    public static final String USERNAME = "username";
    public static final int REQUEST_CODE = 99;

    private final int NAV_LOGIN = 0;
    private final int NAV_HOME = 1;
    private final int NAV_MY_GAMES = 2;
    private final int NAV_LEADERBOARD = 3;

    private String userID;

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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult: Går in i onActivityResult" + requestCode);
        if(resultCode == GameActivity.GAME_RESULT_CODE){
            Log.d(TAG, "onActivityResult: Lägger till i databas");
            GameScore gameScore = data.getParcelableExtra(GameActivity.GAME_RESULT);
            gDB.addGame(gameScore);
            LeaderboardScore score = new LeaderboardScore();

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
                            initCurrentUser();
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
                            initCurrentUser();
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

    private void initCurrentUser(){
        FirebaseUser user = mAuth.getCurrentUser();
        userID = user.getUid();
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
    public void updateAdapter(HashMap<String,LeaderboardScore> map) {
        mLeaderboardList = new ArrayList<>(map.values());
        LeaderboardScore score;
        if(map.containsKey(userID)){
            score = map.get(userID);
            homeFragment.setWins(score.getWonGames());
            homeFragment.setLosses(score.getLostGames());
            homeFragment.setWinrate(winRate(score));
        }
    }

    private String winRate(LeaderboardScore score){
        DecimalFormat df = new DecimalFormat("#.#");
        return String.valueOf(df.format(((float) score.getWonGames()/(score.getLostGames()+score.getWonGames()))*100));
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
            }
            return null;
        }

        @Override
        public int getCount() {
            return 4;
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
            if (position == NAV_LOGIN) {
                nav.setVisibility(View.INVISIBLE);
            } else {
                nav.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onPageSelected(int position) {
            switch (position){
                case NAV_LOGIN:
                    mActionBar.setTitle(R.string.login);
                    break;
                case NAV_HOME:
                    mActionBar.setTitle(R.string.home);
                    break;
                case NAV_MY_GAMES:
                    mActionBar.setTitle(R.string.my_games);
                    break;
                case NAV_LEADERBOARD:
                    mActionBar.setTitle(R.string.leaderboard);
                    break;
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) { }
    }

}
