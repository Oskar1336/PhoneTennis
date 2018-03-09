package ptcorp.ptapplication.main.fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dd.processbutton.iml.ActionProcessButton;

import ptcorp.ptapplication.R;


public class CalibrateStrikeFragment extends Fragment {

    private ActionProcessButton mStartCali, mCancel;
    private CalibrateButtonListener mListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_calibrate_strike_sensor, container, false);

        mStartCali = v.findViewById(R.id.btn_start_cali);
        mCancel = v.findViewById(R.id.btn_cancel_cali);
        mStartCali.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onStartCalibrate();
            }
        });

        mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onCancel();
            }
        });
        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mListener = (CalibrateButtonListener)getActivity();
    }

    public interface CalibrateButtonListener {
        void onStartCalibrate();
        void onCancel();
    }
}
