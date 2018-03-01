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

import com.dd.processbutton.iml.ActionProcessButton;

import ptcorp.ptapplication.FButton;
import ptcorp.ptapplication.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class ServerConnectFragment extends DialogFragment {
    private ListView mServers;
    private ActionProcessButton mCancelBtn;
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

        return view;
    }

    private class ListAdapter extends BaseAdapter {

        @Override
        public int getCount() {

            return 0;
        }

        @Override
        public Object getItem(int position) {

            return 0;
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

            return convertView;
        }
    }

}
