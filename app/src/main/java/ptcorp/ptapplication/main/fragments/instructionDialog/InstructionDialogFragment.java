package ptcorp.ptapplication.main.fragments.instructionDialog;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.dd.processbutton.iml.ActionProcessButton;

import java.util.ArrayList;
import java.util.List;

import ptcorp.ptapplication.R;
import ptcorp.ptapplication.main.components.DialogViewPager;
import ptcorp.ptapplication.main.components.DottedProgressBar;


public class InstructionDialogFragment extends DialogFragment {

    private final short INSTRUCTION_AMOUNT = 5;

    private DottedProgressBar mDpbProgress;

    private int mLastPosition;

    private InstructionFragment mAboutFragment;
    private InstructionFragment mStartingGameFragment;
    private InstructionFragment mStrikeFragment;
    private InstructionFragment mCatchFragment;
    private InstructionFragment mServeFragment;

    @Override
    public void onStart() {
        super.onStart();
        Dialog d = getDialog();
        if (d != null) {
            int w = ViewGroup.LayoutParams.MATCH_PARENT;
            int h = ViewGroup.LayoutParams.MATCH_PARENT;
            if (d.getWindow() != null)
                d.getWindow().setLayout(w, h);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLastPosition = 0;

        mAboutFragment = new InstructionFragment();
        mAboutFragment.setTitle(R.string.about_the_game_title);
        mAboutFragment.setText(R.string.about_the_game_content);

        mStrikeFragment = new InstructionFragment();
        mStrikeFragment.setTitle(R.string.how_to_strike_title);
        mStrikeFragment.setText(R.string.how_to_strike_content);
        mStrikeFragment.setImage(R.drawable.cali_hand);

        mCatchFragment = new InstructionFragment();
        mCatchFragment.setTitle(R.string.how_to_catch_title);
        mCatchFragment.setText(R.string.how_to_catch_content);

        mStartingGameFragment = new InstructionFragment();
        mStartingGameFragment.setTitle(R.string.starting_game_title);
        mStartingGameFragment.setText(R.string.starting_game_content);

        mServeFragment = new InstructionFragment();
        mServeFragment.setTitle(R.string.how_to_serve_title);
        mServeFragment.setText(R.string.how_to_serve_content);
        mServeFragment.setImage(R.drawable.serve_lock_dialog);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mView = inflater.inflate(R.layout.fragment_dialog_how_to_play, container, false);

        mDpbProgress = mView.findViewById(R.id.dpb_progress);
        DialogViewPager mVpInstructions = mView.findViewById(R.id.vp_instructions);

        final InstructionPager ip = new InstructionPager(getChildFragmentManager());

        mVpInstructions.storeAdapter(ip);
        mVpInstructions.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }
            @Override
            public void onPageScrollStateChanged(int state) { }

            @Override
            public void onPageSelected(int position) {
                if (position > mLastPosition) {
                    mDpbProgress.incDotPosition();
                } else {
                    mDpbProgress.decDotPosition();
                }
                mLastPosition = position;
            }
        });

        return mView;
    }

    private class InstructionPager extends FragmentPagerAdapter {
        InstructionPager(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return mAboutFragment;
                case 1:
                    return mStrikeFragment;
                case 2:
                    return mCatchFragment;
                case 3:
                    return mStartingGameFragment;
                case 4:
                    return mServeFragment;
            }
            return null;
        }

        @Override
        public int getCount() {
            return INSTRUCTION_AMOUNT;
        }
    }
}
