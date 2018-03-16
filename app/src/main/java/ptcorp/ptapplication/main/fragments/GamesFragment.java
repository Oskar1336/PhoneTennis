package ptcorp.ptapplication.main.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ptcorp.ptapplication.R;
import ptcorp.ptapplication.main.adapters.GamesAdapter;


/**
 * A simple {@link Fragment} subclass.
 */
public class GamesFragment extends Fragment {

    private RecyclerView rvGames;
    private  ItemTouchHelper itemTouchHelper;

    public GamesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_my_games, container, false);
        rvGames = v.findViewById(R.id.rvGames);
        itemTouchHelper.attachToRecyclerView(rvGames);

        return v;
    }

    public void setOnItemTouchHelper(ItemTouchHelper.SimpleCallback simpleItemTouchCallback){
        itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
    }

    public void setAdapter(RecyclerView.Adapter adapter){
        rvGames.setHasFixedSize(true);
        rvGames.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvGames.setAdapter(adapter);
    }

}
