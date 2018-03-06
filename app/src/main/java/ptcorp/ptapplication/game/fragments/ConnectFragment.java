package ptcorp.ptapplication.game.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ptcorp.ptapplication.R;

/**
 * Created by LinusHakansson on 2018-03-01.
 */

public class ConnectFragment extends Fragment {
    private info.hoang8f.widget.FButton mHostButton, mConnectButton;
    private ConnectFragmentListener mListener;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.connect_fragment, container, false);

        mHostButton = view.findViewById(R.id.fbHost);
        mHostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.host();
            }
        });
        mConnectButton = view.findViewById(R.id.fbConnect);
        mConnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.connect();
            }
        });
        return view;
    }

    public void disableButtons() {
        mHostButton.setEnabled(false);
        mConnectButton.setEnabled(false);
    }

    public void enableButtons() {
        mHostButton.setEnabled(true);
        mConnectButton.setEnabled(true);
    }

    public void showHostNotStartedError() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.host_error);
        builder.setMessage(R.string.host_error_explanation);
        builder.setPositiveButton(R.string.close, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    public void showNotConnectedError() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.bluetooth_error);
        builder.setMessage(R.string.bluetooth_error_explination);
        builder.setPositiveButton(R.string.close, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    public interface ConnectFragmentListener{
        void host();
        void connect();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mListener = (ConnectFragmentListener) getActivity();
    }
}
