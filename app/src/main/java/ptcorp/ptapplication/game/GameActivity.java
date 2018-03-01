package ptcorp.ptapplication.game;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import ptcorp.ptapplication.R;

public class GameActivity extends AppCompatActivity implements ConnectFragment.ConnectFragmentListener {
    private static final String TAG = "GameActivity";
    private FragmentManager mFragmentManager;
    private FragmentTransaction mFragmentTransaction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        mFragmentManager = getSupportFragmentManager();
        setConnectFragment();
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
    }

    @Override
    public void connect() {
        ServerConnectFragment serverConnectFragment = new ServerConnectFragment();
        serverConnectFragment.show(mFragmentManager, "serverConnectFragment");
    }
}
