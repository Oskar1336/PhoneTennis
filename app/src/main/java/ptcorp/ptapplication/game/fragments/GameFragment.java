package ptcorp.ptapplication.game.fragments;


import android.app.AlertDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.app.Fragment;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.dd.processbutton.iml.ActionProcessButton;

import java.util.Timer;
import java.util.TimerTask;

import ptcorp.ptapplication.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class GameFragment extends Fragment{
    private static final String TAG = "GameFragment";
    private LoadingFragment loadingFragment;
    private AlertDialog alertDialogServe, alertDialogLock, alertDialogStrike, alertDialogRoundMessage;
    private ImageView mCompass;
    private TextView hostPoints, clientPoints, tvCurrentDegree, tvStartingDegree;
    private ProgressBar mProgressBar;
    private ProgressUpdater mProgressUpdater;
    private GameListener mGameListener;



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
        tvCurrentDegree = view.findViewById(R.id.tvCurrent);
        tvStartingDegree = view.findViewById(R.id.tvStart);
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
                Toast toast = Toast.makeText(getActivity(), message, Toast.LENGTH_LONG);
                toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 50);
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
                        mGameListener.onStrike();
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
                mProgressBar = v.findViewById(R.id.pbStrikeTime);
                mProgressUpdater = new ProgressUpdater();
                mProgressUpdater.execute();

                btnLock.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mProgressUpdater.cancel(true);
                        mGameListener.onStrike();
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
                        mGameListener.onLock();
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

    public void showRoundMessage(final String roundMessage){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                        .setTitle("Round over!")
                        .setMessage(roundMessage);
                alertDialogRoundMessage = builder.create();
                alertDialogRoundMessage.show();
            }
        });
    }

    public void dismissRoundMessage(){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(alertDialogRoundMessage.isShowing())
                    alertDialogRoundMessage.dismiss();
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
        mGameListener = (GameListener) getActivity();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void setCurrentDegree(float mCurrentDegree) {
        if (tvCurrentDegree!=null)
            tvCurrentDegree.setText(String.valueOf(mCurrentDegree));
    }

    public void setStartDegree(float startDegree){
        if (tvStartingDegree!=null)
            tvStartingDegree.setText(String.valueOf(startDegree));
    }

    public interface GameListener {
        void onLock();
        void onStrike();
        void onOutOfTime();
    }


    private class ProgressUpdater extends AsyncTask<Void,Integer,Integer>{
        private int timeCurrent = 5;

        @Override
        protected Integer doInBackground(Void... aVoid) {
            while(timeCurrent > 0){
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                onProgressUpdate(--timeCurrent);
            }
            return 0;
        }


        @Override
        protected void onPreExecute() {
            mProgressBar.setMax(5);
            mProgressBar.setProgress(5);
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Integer integer) {
            mProgressBar.setProgress(integer);
            mGameListener.onOutOfTime();
            alertDialogStrike.dismiss();
            super.onPostExecute(integer);
        }

        @Override
        protected void onProgressUpdate(Integer... integers) {
            mProgressBar.setProgress(integers[0]);
            super.onProgressUpdate(integers);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }
    }

}
