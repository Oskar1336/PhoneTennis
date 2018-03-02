package ptcorp.ptapplication.game;


import android.os.Bundle;
import android.app.Fragment;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.baoyz.widget.PullRefreshLayout;
import com.dd.processbutton.iml.ActionProcessButton;

import java.util.ArrayList;
import java.util.List;

import ptcorp.ptapplication.FButton;
import ptcorp.ptapplication.R;
import ptcorp.ptapplication.bluetooth.bluetoothConnection.BTDevice;

/**
 * A simple {@link Fragment} subclass.
 */
public class ServerConnectFragment extends DialogFragment {
    private ArrayList<BTDevice> mDevicesList;
    private ListView mServers;
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

        mCancelBtn = view.findViewById(R.id.btnCancelServers);
        mCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        mPullRefresh = view.findViewById(R.id.swipeRefreshLayout);
        mPullRefresh.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //TODO: Update adpater
            }
        });
//        mPullRefresh.setRefreshing(false);

        return view;
    }

    public void setServersForAdapter(ArrayList<BTDevice> btDeviceList){
        this.mDevicesList = btDeviceList;
        mServers.setAdapter(new ListAdapter());
        mPullRefresh.setRefreshing(false);
    }

    private class ListAdapter extends BaseAdapter {

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
