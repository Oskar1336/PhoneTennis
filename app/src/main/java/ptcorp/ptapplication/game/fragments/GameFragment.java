package ptcorp.ptapplication.game.fragments;


import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.app.Fragment;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.dd.processbutton.iml.ActionProcessButton;

import ptcorp.ptapplication.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class GameFragment extends Fragment{
    private static final String TAG = "GameFragment";
    private LoadingFragment loadingFragment;
    private AlertDialog alertDialogServe, alertDialogLock, alertDialogStrike;
    private ImageView mCompass;
    private TextView hostPoints, clientPoints;
    private LockDirection mLockDirection;


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
        hostPoints = view.findViewById(R.id.tvHostPoints);
        clientPoints = view.findViewById(R.id.tvClintPoints);
        return view;
    }

    public void hideInitGame() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                loadingFragment.dismiss();
            }
        });
    }

    public void showNewDegree(final String message){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast toast = Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 0);
                toast.show();
            }
        });
    }

    public void serveDialog(){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                LayoutInflater inflater = GameFragment.this.getLayoutInflater();
                View v = inflater.inflate(R.layout.serve_dialog, null);
                builder.setView(v);
                ActionProcessButton btnLock = v.findViewById(R.id.btnLockDirection);
                mCompass = v.findViewById(R.id.ivCompass);
                btnLock.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mLockDirection.onStrike();
                        alertDialogServe.dismiss();
                    }
                });
                alertDialogServe = builder.create();
                alertDialogServe.setCanceledOnTouchOutside(false);
                alertDialogServe.setCancelable(false);
                alertDialogServe.show();
            }
        });
    }

    public void strikeDialog(){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                LayoutInflater inflater = GameFragment.this.getLayoutInflater();
                View v = inflater.inflate(R.layout.strike_dialog, null);
                builder.setView(v);
                ActionProcessButton btnLock = v.findViewById(R.id.btnLockDirectionStrike);
                mCompass = v.findViewById(R.id.ivCompassStrike);
                btnLock.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mLockDirection.onStrike();
                        alertDialogStrike.dismiss();
                    }
                });
                alertDialogStrike = builder.create();
                alertDialogStrike.setCanceledOnTouchOutside(false);
                alertDialogStrike.setCancelable(false);
                alertDialogStrike.show();
            }
        });
    }

    public void lockOpponentDirectionDialog(){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                LayoutInflater inflater = GameFragment.this.getLayoutInflater();
                View v = inflater.inflate(R.layout.serve_dialog, null);
                builder.setView(v);
                ActionProcessButton btnLock = v.findViewById(R.id.btnLockDirection);
                TextView tvCompassTitle  = v.findViewById(R.id.tvCompassTitle);
                tvCompassTitle.setText(R.string.point_to_opponent_message);
                mCompass = v.findViewById(R.id.ivCompass);
                btnLock.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mLockDirection.onLock();
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
        });
    }

    public void rotateCompass(RotateAnimation animation){
        if(mCompass != null)
            mCompass.startAnimation(animation);
    }

    public void updateHostPoints(final int points){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                hostPoints.setText(String.valueOf(points));
            }
        });
    }

    public void updateClientPoints(final int points){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                clientPoints.setText(String.valueOf(points));
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mLockDirection = (LockDirection) getActivity();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public interface LockDirection {
        void onLock();
        void onStrike();
    }

}
