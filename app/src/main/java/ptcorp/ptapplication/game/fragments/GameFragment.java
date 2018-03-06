package ptcorp.ptapplication.game.fragments;


import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.app.Fragment;

import com.dd.processbutton.iml.ActionProcessButton;

import ptcorp.ptapplication.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class GameFragment extends Fragment{
    private static final String TAG = "GameFragment";
    private LoadingFragment loadingFragment;
    private AlertDialog alertDialog;


    public GameFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_game, container, false);
        loadingFragment = new LoadingFragment();
        loadingFragment.setTitle("Setting up game..");
        loadingFragment.enableButton(false);
        loadingFragment.show(getActivity().getSupportFragmentManager(), "loadingFragment");
        return view;
    }

    public void hideInitGame() {
        loadingFragment.dismiss();
    }

    public void serveDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = this.getLayoutInflater();
        View v = inflater.inflate(R.layout.compass_dialog, null);
        builder.setView(v);
        ActionProcessButton btnLock = v.findViewById(R.id.btnLockDirection);
        btnLock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setCancelable(false);
        alertDialog.show();
    }
}
