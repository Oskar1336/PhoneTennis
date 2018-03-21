package ptcorp.ptapplication.main.fragments;


import android.content.Context;
import android.content.SharedPreferences;
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

import ptcorp.ptapplication.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class LoginFragment extends Fragment {

    private ActionProcessButton loginBtn, createBtn;
    private final static String PT_PREFS = "phoneTennisPrefs";
    private EditText username, password;
    private MaterialTextField mtf_username, mtf_password;
    private SharedPreferences mSharedPrefs;

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
        mSharedPrefs = getActivity().getSharedPreferences(PT_PREFS ,Context.MODE_PRIVATE);

        loginBtn = view.findViewById(R.id.loginBtn);
        createBtn = view.findViewById(R.id.createBtn);
        username = view.findViewById(R.id.username);
        password = view.findViewById(R.id.password);
        mtf_username = view.findViewById(R.id.mtf_username);
        mtf_password = view.findViewById(R.id.mtf_password);

        mtf_username.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mtf_username.isExpanded()) {
                    mtf_username.reduce();
                    mtf_password.reduce();
                } else {
                    mtf_username.expand();
                    mtf_password.expand();
                }
            }
        });

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginBtn.setMode(ActionProcessButton.Mode.ENDLESS);
                loginBtn.setProgress(1);
                String email = username.getText().toString();
                String pw = password.getText().toString();
                if(!email.equals("") && !pw.equals("")){
                    listener.login(email, pw);
                    saveEmail();
                }else{
                    createBtn.setProgress(-1);
                }
            }
        });

        createBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createBtn.setMode(ActionProcessButton.Mode.ENDLESS);
                createBtn.setProgress(1);
                String email = username.getText().toString();
                String pw = password.getText().toString();
                if(!email.equals("") && !pw.equals("")){
                    listener.create(username.getText().toString(), password.getText().toString());
                    saveEmail();
                }else{
                    createBtn.setProgress(-1);
                }
            }
        });
        setEmail();

        return view;
    }

    public void clearFocus(){
        username.setFocusable(false);
        password.setFocusable(false);
    }
    public void setLoginBtnProgress(int progress) {
        loginBtn.setProgress(progress);
    }

    public void setCreateBtnProgress(int progress) {
        createBtn.setProgress(progress);
    }

    private void saveEmail(){
        SharedPreferences.Editor edit = mSharedPrefs.edit();
        edit.putString("email", username.getText().toString());
        edit.putString("password", password.getText().toString());
        edit.apply();
    }

    private void setEmail(){
        username.setText(mSharedPrefs.getString("email", ""));
        password.setText(mSharedPrefs.getString("password", ""));
    }

    public interface LoginListener {
        void login(String user, String pass);
        void create(String user, String pass);
    }
}
