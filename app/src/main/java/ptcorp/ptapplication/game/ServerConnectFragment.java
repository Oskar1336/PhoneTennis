package ptcorp.ptapplication.game;


import android.os.Bundle;
import android.app.Fragment;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.baoyz.widget.PullRefreshLayout;
import com.dd.processbutton.iml.ActionProcessButton;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.ArrayList;
import java.util.List;

import ptcorp.ptapplication.FButton;
import ptcorp.ptapplication.R;
import ptcorp.ptapplication.bluetooth.bluetoothConnection.BTDevice;

/**
 * A simple {@link Fragment} subclass.
 */
public class ServerConnectFragment extends DialogFragment {
<<<<<<< HEAD
    private static final String TAG = "ServerConnectFragment";
    private ArrayList<BTDevice> mDevicesList;
    private AlertDialog.Builder dialogBuilder;
    private AlertDialog alertDialog;
=======
>>>>>>> f1a805f2bca86b277e9b4e23bb14544a9b92fda8
    private ListView mServers;
    private ListAdapter mServersAdapter;
    private ActionProcessButton mCancelBtn;
    private PullRefreshLayout mPullRefresh;

    public ServerConnectFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_servers, container, false);
        mServers = view.findViewById(R.id.lvServers);
        mServers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });

        mServersAdapter = new ListAdapter();
        mServers.setAdapter(mServersAdapter);
        mPullRefresh.setRefreshing(true);

        mCancelBtn = view.findViewById(R.id.btnCancelServers);
        mCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if(alertDialog.isShowing()){
                    alertDialog.dismiss();
                }

            }
        });

        mPullRefresh = view.findViewById(R.id.swipeRefreshLayout);
        mPullRefresh.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //TODO: Update adpater
            }
        });

        showSearchingDialog();

        return view;
    }

    public void addItemToList(BTDevice btDevice){
        mServersAdapter.addItem(btDevice);
    }

    public void updateComplete() {
        mPullRefresh.setRefreshing(false);
//        alertDialog.dismiss();
    }

    private void showSearchingDialog(){
        Log.d(TAG, "showSearchingDialog: called");
        dialogBuilder = new AlertDialog.Builder(this.getActivity());
// ...Irrelevant code for customizing the buttons and title
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.searching_hosts, null);
        dialogBuilder.setView(dialogView);

        AVLoadingIndicatorView avLoadingIndicatorView = dialogView.findViewById(R.id.loadingDots);
        avLoadingIndicatorView.smoothToShow();
        alertDialog = dialogBuilder.create();
        alertDialog.show();

        Log.d(TAG, "showSearchingDialog: Showing?: " + alertDialog.isShowing());
    }

    private class ListAdapter extends BaseAdapter {
        private ArrayList<BTDevice> mDevicesList;

        ListAdapter() {
            mDevicesList = new ArrayList<>();
        }

        private void addItem(BTDevice btDevice) {
            mDevicesList.add(btDevice);
            this.notifyDataSetChanged();
        }

        @Override
        public int getCount() {

            return mDevicesList.size();
        }

        @Override
        public Object getItem(int position) {
            return mDevicesList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if(convertView==null){
                convertView = getLayoutInflater().inflate(R.layout.list_rows, parent, false);
            }

            TextView tvDeviceName = convertView.findViewById(R.id.deviceName);
            tvDeviceName.setText(mDevicesList.get(position).getDeviceName());
            TextView tvMacAddress = convertView.findViewById(R.id.macAddress);
            tvMacAddress.setText(mDevicesList.get(position).getBtDevice().getAddress());
            return convertView;
        }
    }
}
