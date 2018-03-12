package ptcorp.ptapplication.main.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ptcorp.ptapplication.R;
import ptcorp.ptapplication.game.GameActivity;
import ptcorp.ptapplication.main.MainActivity;

import static ptcorp.ptapplication.main.MainActivity.REQUEST_CODE;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {
    private info.hoang8f.widget.FButton mPlayButton;
    private static final String TAG = "HomeFragment";
    private String username, wins, losses, winrate;
    private TextView tvPlayer, tvWins, tvLosses, tvWinrate;

    public HomeFragment() {
        // Required empty public constructor
    }

    public void setUsername(String username){
        if(tvPlayer != null){
            this.username = username;
            tvPlayer.setText(username);
        }
    }

    public void setWins(long wins){
        this.wins = String.valueOf(wins);
    }

    public void setLosses(long losses) {
        this.losses = String.valueOf(losses);
    }

    public void setWinrate(String winrate) {
        this.winrate = winrate;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: ");
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_home, container, false);
        mPlayButton = v.findViewById(R.id.btnPlay);
        tvPlayer = v.findViewById(R.id.tvPlayerName);
        tvWinrate = v.findViewById(R.id.tvMyWinrate);
        tvWins = v.findViewById(R.id.tvMyWins);
        tvLosses = v.findViewById(R.id.tvMyLosses);
        tvPlayer.setText(username);
        tvWins.setText(wins);
        tvLosses.setText(losses);
        tvWinrate.setText(winrate);
        mPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), GameActivity.class);
                intent.putExtra(MainActivity.USERNAME, username);
                startActivityForResult(intent, MainActivity.REQUEST_CODE);
            }
        });
        return v;
    }
}
