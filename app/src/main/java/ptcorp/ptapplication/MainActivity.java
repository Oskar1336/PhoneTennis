package ptcorp.ptapplication;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Toast;
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

public class MainActivity extends AppCompatActivity implements LoginFragment.LoginListener{
    private static final String TAG = "MainActivity";
    private FirebaseAuth mAuth;
    private FirebaseDatabaseHandler mHandlerDB;
    private BottomNavigationView nav;
    private ViewPager fragmentHolder;
    private Fragment homeFragment, leaderboardFragment;
    private LoginFragment loginFragment;
    private GamesFragment myGameFragment;
    private GamesDatabaseHandler gDB;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    fragmentHolder.setCurrentItem(1);
                    return true;
                case R.id.navigation_myGames:
                    fragmentHolder.setCurrentItem(2);
                    myGameFragment.setAdapter(new GamesAdapter(gDB.getGames()));
                    return true;
                case R.id.navigation_leaderboard:
                    fragmentHolder.setCurrentItem(3);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        gDB = new GamesDatabaseHandler(this);
        loginFragment = new LoginFragment();
        loginFragment.setListener(this);
        homeFragment = new HomeFragment();
        myGameFragment = new GamesFragment();
        leaderboardFragment = new LeaderboardFragment();

        fragmentHolder = findViewById(R.id.vpPager);
        fragmentHolder.setAdapter(new FragmentPageAdapter(getSupportFragmentManager()));
        fragmentHolder.setCurrentItem(0);
        fragmentHolder.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true;
            }
        });

        mAuth = FirebaseAuth.getInstance();
        mHandlerDB = new FirebaseDatabaseHandler(mAuth);

        nav = findViewById(R.id.navigation);
        nav.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        nav.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser(); // null if not signed in
    }

    @Override
    protected void onDestroy() {
        FirebaseUser user = mAuth.getCurrentUser();
        if(user != null){
            mAuth.signOut();
        }

        super.onDestroy();
    }

    private void createUser(String email, String password){
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            displayToast("Account created and logged in!");
                        } else {
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
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
//                            FirebaseUser user = mAuth.getCurrentUser();
                            fragmentHolder.setCurrentItem(1);
                            nav.setVisibility(View.VISIBLE);
                            displayToast("Logged in!");
                        } else {
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

    @Override
    public void login(String user, String pass) {
        signInUser(user,pass);
    }

    @Override
    public void create(String user, String pass) {
        createUser(user, pass);
    }

    private class FragmentPageAdapter extends FragmentPagerAdapter {



        public FragmentPageAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position){
                case 0:
                    return loginFragment;
                case 1:
                    return homeFragment;
                case 2:
                    return myGameFragment;
                case 3:
                    return leaderboardFragment;
            }
            return null;
        }

        @Override
        public int getCount() {
            return 4;
        }
    }
}
