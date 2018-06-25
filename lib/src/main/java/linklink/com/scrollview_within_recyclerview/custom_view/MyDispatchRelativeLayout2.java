package linklink.com.scrollview_within_recyclerview.custom_view;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

import linklink.com.scrollview_within_recyclerview.utils.LogUtil;


/**
 * MyDispatchRelativeLayout2
 * 重写事件拦截
 * 责任人:  Chuck
 * 修改人： Chuck
 * 创建/修改时间: 2018/5/23  16:45
 * Copyright : 2014-2017 深圳令令科技有限公司-版权所有
 **/

public class MyDispatchRelativeLayout2 extends RelativeLayout {


    private static  final String TAG="MyDispatchLinearLayout";
    private static final int SCROLL_THRESHOLD = 50;


    public MyDispatchRelativeLayout2(Context context) {
        super(context);
    }


    public MyDispatchRelativeLayout2(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyDispatchRelativeLayout2(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
    }

    private int mActionDownY,mLastY,mLastRawY;

    //上下滑动,则拦截,自己处理掉事件
    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        //return super.onInterceptTouchEvent(ev);

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN://按下时记录纵坐标

                mLastY = (int) event.getY();//最后一个action时Y值
                LogUtil.e(TAG, "mLastY:" + mLastY);

                mActionDownY = (int) event.getY();//按下的瞬间Y
                LogUtil.e(TAG, "mActionDownY:" + mActionDownY);

                mLastRawY= (int) event.getRawY();//记录y值返回给其子控件使用

                LogUtil.e(TAG, "=============================ACTION_DOWN,mLastY" + mLastY);

                if(mActionDownCallBack!=null){
                    mActionDownCallBack.recordActionDownY(mLastRawY);
                }

                break;

            case MotionEvent.ACTION_MOVE:

                LogUtil.e(TAG, "=============================ACTION_MOVE");
                LogUtil.e(TAG, "event.getY()=============================" + event.getY());

                int dY = (int) event.getY() - mActionDownY;
                LogUtil.e(TAG, "dY=============================" + dY);


                if(Math.abs(dY)>=SCROLL_THRESHOLD){//是上下滑动,则拦截,此view自己来处理事件
                    return true;
                }
                break;

            case MotionEvent.ACTION_UP://注意,全部是getRawX和getRawY
                LogUtil.e(TAG, "MotionEvent.ACTION_UP");
                break;


            case MotionEvent.ACTION_CANCEL:
                break;

        }

        return false;

    }

    public ActionDownCallBack getmActionDownCallBack() {
        return mActionDownCallBack;
    }

    public void setmActionDownCallBack(ActionDownCallBack mActionDownCallBack) {
        this.mActionDownCallBack = mActionDownCallBack;
    }

    private ActionDownCallBack mActionDownCallBack;//按下一瞬间的rawY的处理

    public interface ActionDownCallBack {
        void recordActionDownY(int mLastRawY);
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
