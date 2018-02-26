package ptcorp.ptapplication;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class LeaderboardFragment extends Fragment implements FirebaseDatabaseHandler.OnDatabaseUpdateListener {

    private RecyclerView rvLeaderboard;

    public LeaderboardFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_leaderboard, container, false);

        rvLeaderboard = v.findViewById(R.id.rvLeaderboard);

        return v;
    }

    @Override
    public void updateAdapter(ArrayList<Score> list) {
        //TODO: Update adapter
    }

    public void setAdapter(RecyclerView.Adapter adapter){
        rvLeaderboard.setHasFixedSize(true);
        rvLeaderboard.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvLeaderboard.setAdapter(adapter);
    }
}
