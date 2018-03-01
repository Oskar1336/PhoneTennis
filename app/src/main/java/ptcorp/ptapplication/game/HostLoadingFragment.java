package ptcorp.ptapplication.game;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dd.processbutton.iml.ActionProcessButton;
import com.victor.loading.rotate.RotateLoading;

import ptcorp.ptapplication.R;

/**
 * Created by LinusHakansson on 2018-03-01.
 */

public class HostLoadingFragment extends DialogFragment {
    private RotateLoading mLoading;
    private ActionProcessButton mCancel;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.host_loading_fragment, container, false);
        mLoading = view.findViewById(R.id.rotateloading);
        mLoading.start();

        mCancel = view.findViewById(R.id.btnCancel);
        mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        return view;
    }

    @Override
    public void dismiss() {
        mLoading.stop();
        super.dismiss();
    }
}
