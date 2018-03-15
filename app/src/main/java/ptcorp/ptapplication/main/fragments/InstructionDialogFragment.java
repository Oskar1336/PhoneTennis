package ptcorp.ptapplication.main.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dd.processbutton.iml.ActionProcessButton;

import ptcorp.ptapplication.R;


public class InstructionDialogFragment extends DialogFragment {
    private static final String TAG = "InstructionDialogFragme";

    private ViewPager mVpInstructions;
    private ActionProcessButton mBtnPrevious;
    private ActionProcessButton mBtnNext;

    private short mCurVpPos;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dialog_how_to_play, container, false);

        mVpInstructions = view.findViewById(R.id.vp_instructions);

        mBtnNext = view.findViewById(R.id.btn_next);
        mBtnPrevious = view.findViewById(R.id.btn_previous);

        mBtnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: on next");
                mCurVpPos++;
                mVpInstructions.setCurrentItem(mCurVpPos);
            }
        });
        mBtnPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: on prev");
                mCurVpPos--;
                mVpInstructions.setCurrentItem(mCurVpPos);
            }
        });

        return view;
    }

    @Override
    public void onAttachFragment(Fragment childFragment) {
        super.onAttachFragment(childFragment);
        Log.d(TAG, "onAttachFragment: Fragment attached");
        mVpInstructions.setAdapter(new InstructionPager(getFragmentManager()));
    }

    private class InstructionPager extends FragmentPagerAdapter {

        InstructionPager(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Log.d(TAG, "getItem: Next fragment");
            switch (position) {
                case 1:
                    InstructionFragment iF = new InstructionFragment();
                    iF.setTitle(R.string.starting_game_title);
                    iF.setText(R.string.starting_game_content);
                    iF.setImage(R.drawable.beach_tennis_ball);
                    return iF;
                case 2:
                    InstructionFragment iF2 = new InstructionFragment();
                    iF2.setTitle(R.string.serve_title);
                    iF2.setText(R.string.serve_content);
                    iF2.setImage(R.drawable.beach_tennis_ball);
                    return iF2;
            }
            return null;
        }

        @Override
        public int getCount() {
            return 2;
        }
    }
}
