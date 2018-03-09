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

/**
 * Created by oskarg on 2018-03-09.
 */

public class DottedProgressBar extends View {
    //actual dot radius
    private int mDotRadius = 8;

    //Bounced Dot Radius
    private int mBounceDotRadius = 10;

    //to get identified in which position dot has to bounce
    private int  mDotPosition;

    //specify how many dots you need in a progressbar
    private int mDotAmount = 10;

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

    //Method to draw your customized dot on the canvas
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //function to create dot
        createDot(canvas,mPaint);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        //Animation called when attaching to the window, i.e to your screen
        startAnimation();
    }

    public void addDot() {
        mDotPosition+=2;
    }

    private void createDot(Canvas canvas, Paint paint) {
        //here i have setted progress bar with 10 dots , so repeat and wnen i = mDotPosition  then increase the radius of dot i.e mBounceDotRadius
        for(int i = 0; i < mDotAmount; i++ ){
            if(i <= mDotPosition){
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
        int width;
        int height;

        //calculate the view width
        int calculatedWidth = (20*9);

        width = calculatedWidth;
        height = (mBounceDotRadius*2);

        //MUST CALL THIS
        setMeasuredDimension(width, height);
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