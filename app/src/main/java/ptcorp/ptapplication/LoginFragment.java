package ptcorp.ptapplication;


import android.drm.DrmStore;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.dd.processbutton.iml.ActionProcessButton;
import com.github.florent37.materialtextfield.MaterialTextField;


/**
 * A simple {@link Fragment} subclass.
 */
public class LoginFragment extends Fragment {

    public ActionProcessButton loginBtn, createBtn;
    private EditText username, password;
    private MaterialTextField mtf_username, mtf_password;

    private LoginListener listener;

    public LoginFragment() {
        // Required empty public constructor
    }

    public void setListener(LoginListener listener){
        this.listener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        loginBtn = view.findViewById(R.id.loginBtn);
        createBtn = view.findViewById(R.id.createBtn);
        username = view.findViewById(R.id.username);
        password = view.findViewById(R.id.password);
        mtf_username = view.findViewById(R.id.mtf_username);
        mtf_password = view.findViewById(R.id.mtf_password);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginBtn.setMode(ActionProcessButton.Mode.ENDLESS);
                loginBtn.setProgress(1);
                listener.login(username.getText().toString(), password.getText().toString());
            }
        });

        createBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createBtn.setMode(ActionProcessButton.Mode.ENDLESS);
                createBtn.setProgress(1);
                listener.create(username.getText().toString(), password.getText().toString());
            }
        });

        return view;
    }

    public interface LoginListener {
        void login(String user, String pass);
        void create(String user, String pass);
    }
}
