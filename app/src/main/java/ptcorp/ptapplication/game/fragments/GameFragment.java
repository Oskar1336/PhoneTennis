package ptcorp.ptapplication.game.fragments;


import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.app.Fragment;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.dd.processbutton.iml.ActionProcessButton;

import ptcorp.ptapplication.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class GameFragment extends Fragment{
    private static final String TAG = "GameFragment";
    private LoadingFragment loadingFragment;
    private AlertDialog alertDialogServe, alertDialogLock;
    private ImageView mCompass;
    private LockOpponentDirection mLockOpponentDirection;


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
        mCompass = v.findViewById(R.id.ivCompass);
        btnLock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: 2018-03-07 Implement this next
            }
        });
        alertDialogServe = builder.create();
        alertDialogServe.setCanceledOnTouchOutside(false);
        alertDialogServe.setCancelable(false);
        alertDialogServe.show();
    }

    public void lockOpponentDirectionDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = this.getLayoutInflater();
        View v = inflater.inflate(R.layout.compass_dialog, null);
        builder.setView(v);
        ActionProcessButton btnLock = v.findViewById(R.id.btnLockDirection);
        TextView tvCompassTitle  = v.findViewById(R.id.tvCompassTitle);
        tvCompassTitle.setText(R.string.point_to_opponent_message);
        mCompass = v.findViewById(R.id.ivCompass);
        btnLock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLockOpponentDirection.onLock();
                loadingFragment = new LoadingFragment();
                loadingFragment.setTitle(getText(R.string.waiting_for_position).toString());
                loadingFragment.show(getActivity().getSupportFragmentManager(), "loadingFragment");
                alertDialogLock.dismiss();
            }
        });
        alertDialogLock = builder.create();
        alertDialogLock.setCanceledOnTouchOutside(false);
        alertDialogLock.setCancelable(false);
        alertDialogLock.show();
    }

    public void rotateCompass(RotateAnimation animation){
        if(mCompass != null)
            mCompass.startAnimation(animation);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mLockOpponentDirection = (LockOpponentDirection) getActivity();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public interface LockOpponentDirection{
        void onLock();
    }
}
