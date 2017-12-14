package com.apress.gerber.reddot;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

/**
 * Created by kunwang on 12/14/2017.
 */

public class DraftLayout extends FrameLayout {
    private static final String TAG = "DraftLayout";
    MyButton mbtn = null;
    private int statusBarHeight;
    PointF mFixCanterPoint = new PointF(450, 450);
    PointF mDragCanterPoint = new PointF(250, 450);


    private Paint mPaint;
    float mFixRadius = 20;
    float mDragRadius = 20;

    private boolean working = false;

    public DraftLayout(@NonNull Context context) {
        super(context);
        mPaint = new Paint();
        mPaint.setColor(Color.RED);
        mPaint.setAntiAlias(true);
    }

    public DraftLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint();
        mPaint.setColor(Color.RED);
        mPaint.setAntiAlias(true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.save();
        canvas.translate(0, -statusBarHeight);

        if(working == true)
        {
            canvas.drawCircle(mFixCanterPoint.x, mFixCanterPoint.y, mFixRadius,
                    mPaint);
            canvas.drawCircle(mDragCanterPoint.x, mDragCanterPoint.y, mDragRadius,
                    mPaint);
        }
        canvas.restore();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float theX = 0f;
        float theY = 0f;
        boolean onIt = false;
        if(getChildCount() > 0)
        {
            mbtn = (MyButton)getChildAt(0);
            theX = mbtn.getX();
            theY = mbtn.getY() - statusBarHeight;
            onIt = mbtn.isOnIt();
            mbtn.setVisibility(INVISIBLE);
        }
        switch (event.getAction())
        {

            case MotionEvent.ACTION_DOWN:
                if(onIt == true) {
                    working = true;

                    //we know the point now
                    mFixCanterPoint.set(new PointF(theX, theY));

                    //draw the cycle
                    invalidate();
                    Log.e(TAG, "ACTION_DOWN" + theX + ", " + theY + ", on = " + onIt);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                PointF temp = new PointF(event.getRawX(), event.getRawY());
                mDragCanterPoint.set(temp);
                invalidate();
                Log.e(TAG, "ACTION_MOVE" + theX + ", " + theY);
                break;
            case MotionEvent.ACTION_UP:
                working = false;
                if(mbtn != null){
                    mbtn.setVisibility(VISIBLE);
                }
                Log.e(TAG, "ACTION_UP" + theX + ", " + theY);
                break;
        }
        return  true;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        statusBarHeight=getStatusBarHeight(this);
    }

    public static int getStatusBarHeight(View v) {
        if (v == null) {
            return 0;
        }
        Rect frame = new Rect();
        v.getWindowVisibleDisplayFrame(frame);
        return frame.top;
    }
}
