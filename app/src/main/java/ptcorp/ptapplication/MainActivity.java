package ptcorp.ptapplication;

import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView nav;
    private FrameLayout fragmentHolder;
    private FragmentManager fragmentManager;
    private Fragment loginFragment, homeFragment, myGameFragment, leaderboardFragment;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    fragmentManager.beginTransaction().add(R.id.fragmentHolder, homeFragment).commit();
                    return true;
                case R.id.navigation_myGames:
                    fragmentManager.beginTransaction().add(R.id.fragmentHolder, myGameFragment).commit();
                    return true;
                case R.id.navigation_leaderboard:
                    fragmentManager.beginTransaction().add(R.id.fragmentHolder, leaderboardFragment).commit();
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loginFragment = new LoginFragment();
        homeFragment = new HomeFragment();
        myGameFragment = new MyGamesFragment();
        leaderboardFragment = new LeaderboardFragment();

        fragmentHolder = findViewById(R.id.fragmentHolder);
        fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().add(R.id.fragmentHolder, loginFragment).commit();

        nav = findViewById(R.id.navigation);
        nav.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        nav.setVisibility(View.INVISIBLE);
    }
}
