package ptcorp.ptapplication.main.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;


public class DottedProgressBar extends View {
    private int mDotRadius = 8;
    private int mBounceDotRadius = 10;
    private int  mDotPosition;
    private int mDotAmount = 2;
    private Paint mPaint;

    public DottedProgressBar(Context context) {
        super(context);
        mPaint = new Paint();
    }

    public DottedProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint();
    }

    public DottedProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mPaint = new Paint();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        createDot(canvas);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        startAnimation();
    }

    public void incDotPosition() {
        mDotPosition++;
    }

    public void decDotPosition() {
        mDotPosition--;
    }

    private void createDot(Canvas canvas) {
        for(int i = 0; i < mDotAmount; i++ ){
            if(i == mDotPosition){
                mPaint.setColor(Color.parseColor("#fd583f"));
                canvas.drawCircle(10+(i*20), mBounceDotRadius, mBounceDotRadius, mPaint);
            }else {
                mPaint.setColor(Color.parseColor("#FF2E110C"));
                canvas.drawCircle(10+(i*20), mBounceDotRadius, mDotRadius, mPaint);
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension((20*mDotAmount), (mBounceDotRadius*2));
    }

    private void startAnimation() {
        BounceAnimation bounceAnimation = new BounceAnimation();
        bounceAnimation.setDuration(100);
        bounceAnimation.setRepeatCount(Animation.INFINITE);
        bounceAnimation.setInterpolator(new LinearInterpolator());
        startAnimation(bounceAnimation);
    }

    private class BounceAnimation extends Animation {
        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            super.applyTransformation(interpolatedTime, t);
            //call invalidate to redraw your view againg.
            invalidate();
        }
    }
}