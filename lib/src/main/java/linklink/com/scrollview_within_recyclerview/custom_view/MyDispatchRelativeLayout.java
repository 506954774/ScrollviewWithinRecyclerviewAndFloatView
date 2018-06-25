package linklink.com.scrollview_within_recyclerview.custom_view;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

import linklink.com.scrollview_within_recyclerview.utils.LogUtil;


/**
 * MyDispatchRelativeLayout
 * 重写事件拦截
 * 责任人:  Chuck
 * 修改人： Chuck
 * 创建/修改时间: 2018/5/23  16:45
 * Copyright : 2014-2017 深圳令令科技有限公司-版权所有
 **/

public class MyDispatchRelativeLayout extends RelativeLayout {


    private static  final String TAG="MyDispatchRelativeLayout";
    public static final int SCROLL_THRESHOLD = 10;//横向滑动超过两个像素,就算是横向滑动


    public MyDispatchRelativeLayout(Context context) {
        super(context);
    }


    public MyDispatchRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyDispatchRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
    }

    private float mActionDownX, mLastX;
    private float mActionDownY, mLastY;
    private int mActionDownRowY;



    /**
     * @method name:onInterceptTouchEvent
     * @des:左右滑动不拦截   上下滑动,根据接口的返回值来决定是否拦截
     * @param :[event]
     * @return type:boolean
     * @date 创建时间:2018/5/23
     * @author Chuck
     **/
    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {

        if(1==0){//测试,全部拦截.自己处理事件,如果需要分发,则手动分发
            return true;
        }

        //return super.onInterceptTouchEvent(ev);

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN://按下时记录纵坐标



                LogUtil.i(TAG, "=============================ACTION_DOWN,getX()=" + event.getX());
                LogUtil.i(TAG, "=============================ACTION_DOWN,getY()=" + event.getY());
                LogUtil.i(TAG, "=============================ACTION_DOWN,getRowX()=" + event.getRawX());
                LogUtil.i(TAG, "=============================ACTION_DOWN,getRowY()=" + event.getRawY());

                mLastX =  event.getX();//最后一个action时X值
                //LogUtil.i(TAG, "=============================ACTION_DOWN,mLastX" + mLastX);


                mActionDownX =  event.getX();//按下的瞬间Y
                //LogUtil.i(TAG, "mActionDownX:" + mActionDownX);


                mLastY =  event.getY();//最后一个action时Y值
                //LogUtil.i(TAG, "mLastY:" + mLastY);

                mActionDownY =  event.getY();//按下的瞬间Y
                //LogUtil.i(TAG, "mActionDownY:" + mActionDownY);



                mActionDownRowY = (int) event.getRawY();//按下的瞬间mActionDownRowY
                //LogUtil.i(TAG, "mActionDownRowY:" +mActionDownRowY);

                //LogUtil.i(TAG, "=============================ACTION_DOWN,mLastY" + mLastY);

                if(mInterceptProvider!=null){
                    mInterceptProvider.onActionDown(mActionDownRowY);
                }
                break;

            case MotionEvent.ACTION_MOVE://左右滑动,不拦截.上下滑动,由接口来决定是否拦截 by:Chuck 2018/05/23

                LogUtil.i(TAG, "=============================ACTION_MOVE,getX()=" + event.getX());
                LogUtil.i(TAG, "=============================ACTION_MOVE,getY()=" + event.getY());
                LogUtil.i(TAG, "=============================ACTION_MOVE,getRowX()=" + event.getRawX());
                LogUtil.i(TAG, "=============================ACTION_MOVE,getRowY()=" + event.getRawY());



                float dX =  event.getX() - mActionDownX;


                LogUtil.i(TAG, "dX=============================" + dX);

                float dY =  event.getY() - mActionDownY;
                LogUtil.i(TAG, "dY=============================" + dY);

                LogUtil.i(TAG, "Math.abs(dX)=============================" + Math.abs(dX));
                LogUtil.i(TAG, "Math.abs(dY)=============================" + Math.abs(dY));

                if(Math.abs(dX)==0&& Math.abs(dY)==0){//实测的时候,发现有这种情况出现:手指上滑,但是坐标没变
                    LogUtil.i(TAG, "Math.abs(dX)==0&&Math.abs(dY)==0,不拦截" );

                    return false;//不拦截
                }
                else if(Math.abs(dX)> Math.abs(dY)){//横向滑动的距离大于纵向的,则不拦截
                    LogUtil.i(TAG, "Math.abs(dX)>Math.abs(dY),不拦截" );

                    return false;//不拦截
                }
                else{//横向滑动的距离小于纵向

                    if(Math.abs(dX)>SCROLL_THRESHOLD){//左右滑动的距离超过了阈值,则不拦截,子控件处理

                        LogUtil.i(TAG, "不拦截:Math.abs(dX)" + Math.abs(dX));

                        return false;
                    }
                    else{//判定为 上下滑动事件
                        if(mInterceptProvider!=null){//接口来处理是否拦截. "醉美大连"业务里的是切换的tab已经浮动到了顶部,则里面的viewPager里的recyclerView可以滑动

                            //现在记录的是getY,也就是距离view边界的距离,上滑的话,新的y会比旧的y小
                            boolean isScrollUp=event.getY()<=mActionDownY;

                            if(event.getY()==mActionDownY){//y值没变默认为上滑.经实测,下滑不会出问题.但是有时候上滑,y值拿不到
                                isScrollUp=true;
                            }
                            LogUtil.i(TAG, "上滑? :" + isScrollUp);

                            boolean intercept=mInterceptProvider.onInterceptTouchEvent(isScrollUp);
                            LogUtil.i(TAG, "接口的返回值:" + intercept+(intercept?",拦截":",不拦截"));

                            return intercept;
                        }
                    }
                }

                break;

            case MotionEvent.ACTION_UP:
                LogUtil.i(TAG, "MotionEvent.ACTION_UP");
                break;


            case MotionEvent.ACTION_CANCEL:
                break;

        }

        return false;

    }
/*
    */
/**
     * @method name:onInterceptTouchEvent
     * @des:左右滑动不拦截   上下滑动,根据接口的返回值来决定是否拦截
     * @param :[event]
     * @return type:boolean
     * @date 创建时间:2018/5/23
     * @author Chuck
     **//*

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {


        //return super.onInterceptTouchEvent(ev);

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN://按下时记录纵坐标

                mLastX = (int) event.getRawX();//最后一个action时X值
                LogUtil.i(TAG, "mLastX:" + mLastX);

                mActionDownX = (int) event.getRawX();//按下的瞬间Y
                LogUtil.i(TAG, "mActionDownX:" + mActionDownX);

                LogUtil.i(TAG, "=============================ACTION_DOWN,mLastX" + mLastX);

                mLastY = (int) event.getRawY();//最后一个action时Y值
                LogUtil.i(TAG, "mLastY:" + mLastY);

                mActionDownY = (int) event.getRawY();//按下的瞬间Y
                LogUtil.i(TAG, "mActionDownY:" + mActionDownY);

                LogUtil.i(TAG, "=============================ACTION_DOWN,mLastY" + mLastY);

                if(mInterceptProvider!=null){
                    mInterceptProvider.onActionDown(mActionDownY);
                }
                break;

            case MotionEvent.ACTION_MOVE://左右滑动,不拦截.上下滑动,由接口来决定是否拦截 by:Chuck 2018/05/23

                LogUtil.i(TAG, "=============================ACTION_MOVE");
                LogUtil.i(TAG, "event.getRawX()=============================" + event.getRawX());

                int dX = (int) event.getRawX() - mActionDownX;
                LogUtil.i(TAG, "event.getRawX()===================y==========" + event.getRawX());

                LogUtil.i(TAG, "dX===================y==========" + dX);

                int dY = (int) event.getRawY() - mActionDownY;
                LogUtil.i(TAG, "event.getRawY()=============================" + event.getRawY());
                LogUtil.i(TAG, "dY=============================" + dY);

                if(Math.abs(dX)>SCROLL_THRESHOLD){//左右滑动的距离超过了阈值,则不拦截,子控件处理

                    LogUtil.i(TAG, "不拦截:Math.abs(dX)" + Math.abs(dX));

                    return false;
                }
                else{
                    if(mInterceptProvider!=null){//接口来处理是否拦截. "醉美大连"业务里的是切换的tab已经浮动到了顶部,则里面的viewPager里的recyclerView可以滑动

                        boolean isScrollUp=(mActionDownY-event.getRawY())>0;
                        LogUtil.i(TAG, "上滑? :" + isScrollUp);

                        boolean intercept=mInterceptProvider.onInterceptTouchEvent(isScrollUp);
                        LogUtil.i(TAG, "接口的返回值:" + intercept+(intercept?",拦截":",不拦截"));

                        return intercept;
                    }
                }



                break;

            case MotionEvent.ACTION_UP:
                LogUtil.i(TAG, "MotionEvent.ACTION_UP");
                break;


            case MotionEvent.ACTION_CANCEL:
                break;

        }

        return false;

    }
*/

    //实现是否拦截当前的事件
    public interface InterceptProvider{
        boolean onInterceptTouchEvent(boolean isScrollUp);
        void onActionDown(int actionDownY);
    }

    private InterceptProvider mInterceptProvider;

    public InterceptProvider getmInterceptProvider() {
        return mInterceptProvider;
    }

    public void setmInterceptProvider(InterceptProvider mInterceptProvider) {
        this.mInterceptProvider = mInterceptProvider;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        try {
            //Canvas.MAXMIMUM_BITMAP_SIZE;
            LogUtil.i(TAG,"canvas.getMaximumBitmapHeight():"+canvas.getMaximumBitmapHeight());
            LogUtil.i(TAG,"canvas.getMaximumBitmapWidth():"+canvas.getMaximumBitmapWidth());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
