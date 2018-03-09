package ptcorp.ptapplication.main.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dd.processbutton.iml.ActionProcessButton;
import com.victor.loading.rotate.RotateLoading;

import ptcorp.ptapplication.R;
import ptcorp.ptapplication.main.components.DottedProgressBar;


public class CalibrateDialogFragment extends DialogFragment {

    private ActionProcessButton mCancel;
    private CalibratingListener mListener;
    private DottedProgressBar mDottedProgress;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_fragment_calibrate, container, false);

        mCancel = view.findViewById(R.id.btnCancel);
        mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        mDottedProgress = view.findViewById(R.id.dottedProgress);

        return view;
    }

    public void setListener(CalibratingListener listener) {
        mListener = listener;
    }

    public void updateProgress() {
        if (mDottedProgress != null) mDottedProgress.addDot();
    }

    @Override
    public void dismiss() {
        if (mListener != null) mListener.onCalibrateCancel();
        super.dismiss();
    }

    public interface CalibratingListener {
        void onCalibrateCancel();
    }
}
