package ptcorp.ptapplication.main.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
    private String username;
    private TextView tvPlayer, tvWins, tvLosses, tvWinrate;

    public HomeFragment() {
        // Required empty public constructor
    }

    public void setUsername(String username){
        this.username = username;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_home, container, false);
        mPlayButton = v.findViewById(R.id.btnPlay);
        tvPlayer = v.findViewById(R.id.tvPlayerName);
        tvPlayer.setText(username);
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
