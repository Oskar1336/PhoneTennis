package ptcorp.ptapplication.game.fragments;

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

/**
 * Created by LinusHakansson on 2018-03-01.
 */

public class LoadingFragment extends DialogFragment {
    private RotateLoading mLoading;
    private ActionProcessButton mCancel;
    private TextView tvTitle;
    private String title;
    private boolean btnVisibility = true;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.loading_fragment, container, false);
        mLoading = view.findViewById(R.id.rotateloading);
        tvTitle = view.findViewById(R.id.tvTitle);
        tvTitle.setText(this.title);
        mLoading.start();

        mCancel = view.findViewById(R.id.btnCancel);
        mCancel.setVisibility(btnVisibility ? View.VISIBLE : View.INVISIBLE);
        mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        return view;
    }

    public void setTitle(String title){
        this.title = title;
    }

    public void enableButton(boolean shouldBeVisible){
       this.btnVisibility = shouldBeVisible;
    }

    @Override
    public void dismiss() {
        mLoading.stop();
        super.dismiss();
    }
}
