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


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {
    private info.hoang8f.widget.FButton mPlayButton;
    private TextView tvPlayer, tvWins, tvLosses, tvWinrate;

    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_home, container, false);
        mPlayButton = v.findViewById(R.id.btnPlay);
        mPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(getActivity(), GameActivity.class), MainActivity.REQUEST_CODE);
            }
        });
        return v;
    }
}
