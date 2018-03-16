package ptcorp.ptapplication.main.components;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * Created by oskarg on 2018-03-15.
 */

public class DialogViewPager extends ViewPager {
    private PagerAdapter mPagerAdapter;

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mPagerAdapter != null) {
            Log.d("DialogViewPager", "onAttachedToWindow: Setting PagerAdapter");
            super.setAdapter(mPagerAdapter);
        }
    }

    @Override
    public void setAdapter(PagerAdapter adapter) {
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height = 0;
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            child.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
            int h = child.getMeasuredHeight();
            if (h > height) height = h;
        }
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void storeAdapter(PagerAdapter adapter) {
        mPagerAdapter = adapter;
    }

    public DialogViewPager(Context context) {
        super(context);
    }

    public DialogViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

}
