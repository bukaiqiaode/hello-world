package com.apress.gerber.reddot;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.OvershootInterpolator;

/**
 * Created by kunwang on 12/14/2017.
 */

public class StickyView extends View {

    /**
     * 可以接受的，两圆心最大距离
     */
    float mFarthestDistance = 200;

    /**
     * 可以接受的，固定圆的最小半径
     */
    float mMinFixRadius = 8;
    /**
     * 拖拽圆的圆心
     */
    PointF mDragCanterPoint = new PointF(250, 450);
    /**
     * 固定圆的圆心
     */
    PointF mFixCanterPoint = new PointF(250, 450);
    /**
     * 控制点
     */
    PointF mCanterPoint = new PointF(250, 400);

    /**
     * 固定圆的切点
     */
    PointF[] mFixTangentPointes = new PointF[] { new PointF(235, 250),
            new PointF(265, 250) };
    /**
     * 拖拽圆的切点
     */
    PointF[] mDragTangentPoint = new PointF[] { new PointF(230, 450),
            new PointF(270, 450) };
    /**
     * 拖拽圆半径
     */
    float mDragRadius = 20;
    /**
     * 固定圆半径
     */
    float mFixRadiusOrigin = 20;
    float mFixRadius = 20;
    private int statusBarHeight;
    private Paint mPaint;
    private Path mPath;


    public StickyView(Context context) {
        super(context);
        mPaint = new Paint();
        mPaint.setColor(Color.RED);
        mPaint.setAntiAlias(true);
        mPath = new Path();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.save();
        canvas.translate(0, -statusBarHeight);

        if(dismissed == false) {
            /**
             * dismissed == false，
             * 可能是初始化的状态，那么我们应当画上圆。
             * 也可能是，用户从来没有将触点移动到 最大范围 之外。那么我们应当根据用户的触点更新
             */

            mFixRadius = updateStickRadius();
            /**
             * Draw the fixed circle -- the original red-dot
             */
            canvas.drawCircle(mFixCanterPoint.x, mFixCanterPoint.y, mFixRadius,
                    mPaint);

            /**
             * calculate the control-point
             *  == the central-point of 'mFixCanterPoint' and 'mDragCanterPoint'
             */
            float dy = mDragCanterPoint.y - mFixCanterPoint.y;
            float dx = mDragCanterPoint.x - mFixCanterPoint.x;

            mCanterPoint.set((mDragCanterPoint.x + mFixCanterPoint.x) / 2,
                    (mDragCanterPoint.y + mFixCanterPoint.y) / 2);

            if (dx != 0) {
                float k1 = dy / dx;
                float k2 = -1 / k1;

                /**
                 * 当 固定圆 与 拖拽圆 的 圆心连线水平 时，其斜率==0
                 * 此时，与圆心连线垂直的直线，其斜率不存在。
                 * 这里，应当特殊处理。
                 * 但是，我在实验中发现，当k1==0的时候，得到的k2是'-Infinity'
                 * 这是Java的语言特性？
                 */
                mDragTangentPoint = getIntersectionPoints(
                        mDragCanterPoint, mDragRadius, (double) k2);
                mFixTangentPointes = getIntersectionPoints(
                        mFixCanterPoint, mFixRadius, (double) k2);

            } else {
                /**
                 * 固定圆与拖拽圆的圆心在同一竖直x上， 圆心连线的斜率不存在
                 * 则与 圆心连线 垂直的直线，其斜率是0
                 */
                mDragTangentPoint = getIntersectionPoints(
                        mDragCanterPoint, mDragRadius, (double) 0);
                mFixTangentPointes = getIntersectionPoints(
                        mFixCanterPoint, mFixRadius, (double) 0);
            }

            /**
             * 从 固定圆切点1 出发，以 mCanterPoint 为控制点，做贝塞尔曲线到 拖拽圆切点1
             * 然后，从 拖拽圆切点1，画直线到 拖拽圆切点2
             * 然后，从 拖拽圆切点2，以 mCanterPoint 为控制点，做贝塞尔曲线到 固定圆切点2
             * 闭合路径，并填充路径内部
             */
            mPath.reset();
            mPath.moveTo(mFixTangentPointes[0].x, mFixTangentPointes[0].y);
            mPath.quadTo(mCanterPoint.x, mCanterPoint.y,
                    mDragTangentPoint[0].x, mDragTangentPoint[0].y);
            mPath.lineTo(mDragTangentPoint[1].x, mDragTangentPoint[1].y);
            mPath.quadTo(mCanterPoint.x, mCanterPoint.y,
                    mFixTangentPointes[1].x, mFixTangentPointes[1].y);
            mPath.close();
            canvas.drawPath(mPath, mPaint);

            canvas.drawCircle(mDragCanterPoint.x, mDragCanterPoint.y,
                    mDragRadius, mPaint);
        }

        //参考范围，没实际作用
        mPaint.setStyle(Paint.Style.STROKE);
        canvas.drawCircle(mFixCanterPoint.x, mFixCanterPoint.y, mFarthestDistance, mPaint);
        mPaint.setStyle(Paint.Style.FILL);
        canvas.restore();
    }

    /** 获取状态栏高度
     * @param v
     * @return
     */
    public static int getStatusBarHeight(View v) {
        if (v == null) {
            return 0;
        }
        Rect frame = new Rect();
        v.getWindowVisibleDisplayFrame(frame);
        return frame.top;
    }
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        statusBarHeight=getStatusBarHeight(this);
    }

    /**
     * Get the point of intersection between circle and line.
     * 获取 通过指定圆心，斜率为lineK的直线与圆的交点。
     *
     * @param pMiddle The circle center point.
     * @param radius The circle radius.
     * @param lineK The slope of line which cross the pMiddle.
     * @return
     */
    public static PointF[] getIntersectionPoints(PointF pMiddle, float radius, Double lineK) {
        PointF[] points = new PointF[2];

        float radian, xOffset = 0, yOffset = 0;
        if(lineK != null){

            radian= (float) Math.atan(lineK);
            xOffset = (float) (Math.cos(radian) * radius);
            yOffset = (float) (Math.sin(radian) * radius);
        }else {
            xOffset = radius;
            yOffset = 0;
        }
        points[0] = new PointF(pMiddle.x + xOffset, pMiddle.y + yOffset);
        points[1] = new PointF(pMiddle.x - xOffset, pMiddle.y - yOffset);

        return points;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(dismissed == true) {
            /**
             * 已经消除小圆点了，不必再理会用户的动作
             */
            return  true;
        }
        /**
         * 根据动作发生的坐标，计算 动作发生点 和 固定圆的圆心 之间的距离
         */
        float actionX = event.getRawX();
        float actionY = event.getRawY() - statusBarHeight;
        float tmpDistance = GeometryUtil.getDistanceBetween2Points(mFixCanterPoint, new PointF(actionX, actionY));
        switch (event.getAction())
        {

            case MotionEvent.ACTION_DOWN:
                if(tmpDistance > mFarthestDistance)
                {
                    /**
                     *  用户在最大范围外，点下按钮，则不响应
                     */
                }
                else
                {
                    updateDragCenterPoint(actionX, actionY);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if( tmpDistance > mFarthestDistance) {
                    //未来增加消除掉小圆点的逻辑
                    dismissAll();
                }
                else {
                    //不需要做什么，正常更新拖拽圆的绘制
                    updateDragCenterPoint(actionX, actionY);
                }
                break;
            case MotionEvent.ACTION_UP:
                if(tmpDistance > mFarthestDistance){
                    /**
                     * 在最大范围外释放触点，什么都不需要做。
                     * 因为，在移动到这里的过程中，小圆点已经被自动消除。
                     */
                }
                else{
                    /**
                     * 在最大范围内释放触点，回弹
                     */
                    dotRestore();
                }
                break;
        }
        return  true;
    }

    private void updateDragCenterPoint(float x, float y)
    {
        /**
         * 重新设置拖拽圆的圆心位置
         */
        mDragCanterPoint.set(x, y);

        /**
         * 重新绘制
         */
        invalidate();
    }

    private float updateStickRadius()
    {
        float distance = (float)Math.sqrt(Math.pow(mDragCanterPoint.y - mFixCanterPoint.y, 2)
                + Math.pow(mDragCanterPoint.x - mFixCanterPoint.x, 2));
        distance = Math.min(distance, mFarthestDistance);
        float percent = (1.0f * distance) / mFarthestDistance;
        return mFixRadiusOrigin + (mMinFixRadius - mFixRadiusOrigin) * percent;
    }

    /**
     * 在允许的范围内松手，则播放回弹动画
     */
    private void dotRestore()
    {
        /**
         * 以startPoint和endPoint为端点， 来回移动 拖拽圆
         */
        final PointF startPoint = new PointF(mDragCanterPoint.x, mDragCanterPoint.y);
        final PointF endPoint = new PointF(mFixCanterPoint.x, mFixCanterPoint.y);

        ValueAnimator animator = ValueAnimator.ofFloat(1.0f);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float fraction = valueAnimator.getAnimatedFraction();
                PointF byPercent = GeometryUtil.getPointByPercent(startPoint, endPoint, fraction);
                updateDragCenterPoint(byPercent.x, byPercent.y);
            }
        });

        animator.setInterpolator(new OvershootInterpolator(4.0f));
        animator.setDuration(500);
        animator.start();
    }

    private boolean dismissed = false;
    private void dismissAll()
    {

        /**
         * 记录下，小圆点已经被消除
         * 然后，重新绘制
         */
        Log.e("TEST", "dismissAll" );
        dismissed = true;
        invalidate();
    }
}
