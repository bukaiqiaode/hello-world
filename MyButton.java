package com.apress.gerber.reddot;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

/**
 * Created by kunwang on 12/14/2017.
 */

public class MyButton extends android.support.v7.widget.AppCompatButton {
    private float myX = 0.0f;
    private float myY = 0.0f;

    private static final String TAG = "MyButton";

    public MyButton(Context context) {
        super(context);
    }

    public MyButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public float getX()
    {
        return myX;
    }
    public float getY()
    {
        return myY;
    }

    private boolean onIt = false;
    public boolean isOnIt()
    {
        return onIt;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                Log.e(TAG, "onTouchEvent: ");
                //remember the point
                myX = event.getRawX();
                myY = event.getRawY();
                onIt = true;
                break;
        }

        return false;
    }
}
