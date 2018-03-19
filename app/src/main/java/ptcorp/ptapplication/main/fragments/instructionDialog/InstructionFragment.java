package ptcorp.ptapplication.main.fragments.instructionDialog;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import ptcorp.ptapplication.R;

/**
 * Created by oskarg on 2018-03-15.
 */

public class InstructionFragment extends Fragment {
    private int mTextResource;
    private int mTitleResource;
    private int mDrawableResource = -1;

    private TextView mTvTextContent;
    private TextView mTvTitle;
    private ImageView mIvImage;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.instruction_fragment, container, false);

         mTvTextContent = v.findViewById(R.id.tv_inst_content);
         mTvTitle = v.findViewById(R.id.tv_inst_title);
         mIvImage = v.findViewById(R.id.iv_instruction);

         mTvTitle.setText(mTitleResource);
         mTvTextContent.setText(mTextResource);
         if (mDrawableResource != -1) {
             mIvImage.setImageResource(mDrawableResource);
         }

        return v;
    }

    public void setTitle(int resource) {
        mTitleResource = resource;
    }

    public void setText(int resource) {
        mTextResource = resource;
    }

    public void setImage(int resource) {
        mDrawableResource = resource;
    }
}
