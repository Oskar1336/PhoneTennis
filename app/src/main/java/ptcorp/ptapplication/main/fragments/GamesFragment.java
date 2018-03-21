package ptcorp.ptapplication.main.fragments;


import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
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
    private FloatingActionButton mHowBtn;
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
        mHowBtn = v.findViewById(R.id.fbHow);
        mHowBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                instructionsDialog();
            }
        });
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

    private void instructionsDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.instructions_dialog_title)
                .setMessage(R.string.instructions_dialog_message)
                .setNegativeButton(getText(R.string.dismiss), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
