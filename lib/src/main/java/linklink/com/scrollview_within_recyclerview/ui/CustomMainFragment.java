package linklink.com.scrollview_within_recyclerview.ui;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

import linklink.com.scrollview_within_recyclerview.R;
import linklink.com.scrollview_within_recyclerview.base.CustomBaseFragment2;
import linklink.com.scrollview_within_recyclerview.custom_view.CGBHeader;
import linklink.com.scrollview_within_recyclerview.custom_view.MyDispatchRelativeLayout;
import linklink.com.scrollview_within_recyclerview.custom_view.MyDispatchRelativeLayout2;
import linklink.com.scrollview_within_recyclerview.utils.DensityUtil;
import linklink.com.scrollview_within_recyclerview.utils.LogUtil;

/**
 * MainFragment
 * 责任人:  Chuck
 * 修改人： Chuck
 * 创建/修改时间: 2018/6/25  10:11
 * Copyright : 2014-2018 深圳令令科技有限公司-版权所有
 **/
public abstract class CustomMainFragment extends Fragment implements ViewPager.OnPageChangeListener {

    private static final String TAG = "MainFragment";

    protected static final int MARGIN_THRESHOLD = 10;//浮动tab的允许误差值
    private static final int FLOAT_THRESHOLD_Y = 150;//悬浮临界容差
    private static  int DOWN_BACK_THRESHOLD = 200;//是否可以下滑,下滑的上限.有则滑到极限松手,回弹
    private static  int TOP_REFRESH_THRESHOLD = 100;//触发刷新的阈值

    protected MyDispatchRelativeLayout2 llHead;
    protected LinearLayout adList;
    protected ImageView ivBackground;
    protected MyDispatchRelativeLayout rlVpContainner;
    protected RelativeLayout  rlTitleFilled;
    protected ViewPager vp;
    private RelativeLayout rl_content_root;

    private CGBHeader mCGBHeader;


    protected List<CustomBaseFragment2> mPagerList = new ArrayList<CustomBaseFragment2>();// 碎片集合
    protected int initIndex = 0;//子页索引
    protected LinearLayout mTitleViewRoot;
    private LinearLayout mTabContainer;
    private int mCGBHeaderHeight;//头动画的高度
    private boolean mRefreshing;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mCGBHeader= (CGBHeader) getView().findViewById(R.id.anima);

        if(!isRefreshable()){//头,动画
            mCGBHeader.setVisibility(View.INVISIBLE);
        }

        llHead= (MyDispatchRelativeLayout2) getView().findViewById(R.id.ll_head);
        adList= (LinearLayout) getView().findViewById(R.id.ad_list);
        ivBackground= (ImageView) getView().findViewById(R.id.iv_background);
        rlVpContainner= (MyDispatchRelativeLayout) getView().findViewById(R.id.rl_vp_containner);
        rlTitleFilled= (RelativeLayout) getView().findViewById(R.id.rl_title);
        vp= (ViewPager) getView().findViewById(R.id.vp);
        rl_content_root= (RelativeLayout) getView().findViewById(R.id.rl_content_root);

        //重置title高度(子类的title可能需要设置不同的高度)
        ViewGroup.LayoutParams pa=rlTitleFilled.getLayoutParams();
        pa.height= getTitleViewParentHeight();
        LogUtil.i(TAG,"重置title父容器的高度值:"+ getTitleViewParentHeight());
        rlTitleFilled.setLayoutParams(pa);

        ivBackground.setImageResource(getTitleBackgroundRes());
        mTitleViewRoot= (LinearLayout) getView().findViewById(R.id.rl_titlt_root);

        View titleView=getTitleView();
        if(titleView!=null){
            mTitleViewRoot.addView(titleView);
        }
        //重置title的margigTop,以适应不同的状态栏高度
        ViewGroup.MarginLayoutParams titlePa= (ViewGroup.MarginLayoutParams) titleView.getLayoutParams();
        titlePa.setMargins(0, getTitleViewMarginTop(),0,0);




        View headView=getHeadView();
        if(headView!=null){
            adList.addView(headView);
        }

        mTabContainer= (LinearLayout) getView().findViewById(R.id.ll_tab_container);
        View floatView=getFloatView();
        if(floatView!=null){
            mTabContainer.addView(floatView);
        }

        if(autoSetupViewPager()){
            getFragmnets();
        }
        initHeadScrollListener();
        initViewPagerContainerScrollListener();
        getScroolMax();
    }

    public boolean ismRefreshing() {
        return mRefreshing;
    }

    protected int mHeadActionDownX, mHeadActionDownY, mHeadLastY, mHeadSlidedDistance, mScroolMax;

    /**
     * @param :[]
     * @return type:void
     * @method name:initHeadScrollListener
     * @des:给列表顶部的控件加滑动监听: 上滑时, 改变控件的位置, 并带动底下的viewpage一起滑动
     * 下滑时类似
     * 手指抬起时,如果是下滑的,则做一个回弹效果
     * @date 创建时间:2018/5/23
     * @author Chuck
     **/
    private void initHeadScrollListener() {

        llHead.setmActionDownCallBack(new MyDispatchRelativeLayout2.ActionDownCallBack() {
            @Override
            public void recordActionDownY(int mLastRawY) {

                LogUtil.e(TAG, "顶部滑动控件,记录父容器按下的y的rawY值:" +mLastRawY);

                mHeadLastY = (int) mLastRawY;//最后一个action时Y值
                mHeadActionDownX = (int) mLastRawY;//按下的瞬间X
            }
        });

        llHead.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(final View v, MotionEvent event) {

                if(mRefreshing){//正在刷新,直接返回
                    return false;
                }

                switch (event.getAction()) {

                    case MotionEvent.ACTION_DOWN://按下时记录纵坐标




                        if (mScroolMax == 0) {//是个正值,head在上滑的上限值
                            //悬浮于顶部,距离值
                            mScroolMax = adList.getHeight()-rlTitleFilled.getHeight();

                            //mScroolMax = mViewBind.llHead.getHeight() - mViewBind.llTabContainer.getHeight()-mViewBind.rlTitleFilled.getHeight();
                            LogUtil.e(TAG, "能上滑的最大距离:" + mScroolMax);
                        }

                        mHeadLastY = (int) event.getRawY();//最后一个action时Y值
                        mHeadActionDownX = (int) event.getRawX();//按下的瞬间X
                        LogUtil.e(TAG, "mActionDownX:" + mHeadActionDownX);

                        mHeadActionDownY = (int) event.getRawY();//按下的瞬间Y
                        LogUtil.e(TAG, "mActionDownY:" + mHeadActionDownY);

                        LogUtil.e(TAG, "=============================ACTION_DOWN,mLastY" + mHeadLastY);
                        break;

                    case MotionEvent.ACTION_MOVE:

                        LogUtil.e(TAG, "=============================ACTION_MOVE");
                        LogUtil.e(TAG, "event.getRawY()=============================" + event.getRawY());

                        int dY = (int) event.getRawY() - mHeadLastY;
                        LogUtil.e(TAG, "dY=============================" + dY);

                        mHeadSlidedDistance = (int) event.getRawY() - mHeadActionDownY;
                        LogUtil.e(TAG, "mSlidedDistance=============================" + mHeadSlidedDistance);

                        final ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
                        int left = params.leftMargin;
                        int top = params.topMargin;
                        int right = params.rightMargin;
                        int bottom = params.bottomMargin;

                        final ViewGroup.MarginLayoutParams vpParams = (ViewGroup.MarginLayoutParams) rlVpContainner.getLayoutParams();
                        //int left = vpParams.leftMargin;
                        //int top = vpParams.topMargin;
                        //int right = vpParams.rightMargin;
                        //int bottom =vpParams.bottomMargin;

                        LogUtil.e(TAG, "left:" + left + ",top:" + top + ",right:" + right + ",bottom" + bottom);

                        int topNew = top + dY;
                        int bottomNew = bottom - dY;


                        int stetchDistance = DOWN_BACK_THRESHOLD;//下滑的回弹距离上限


                        //上滑极限是tab的位置,下滑极限是回弹的距离   topNew小于0是上滑  topNew大于0是下滑  by:Chuck 2018/05/23
                        if ((topNew <= 0 && Math.abs(topNew) <= mScroolMax) || (topNew > 0 && topNew < stetchDistance)) {

                            if(topNew <= 0 && Math.abs(topNew) >= mScroolMax){//滑动超标了.因为回调并不是一个像素一个像素的
                                topNew=-1 * mScroolMax;//赋值为阈值
                            }

                            LogUtil.e(TAG, topNew + "=============================MOVE");
                            params.setMargins(left, topNew, right, bottomNew);
                            v.setLayoutParams(params);

                            vpParams.setMargins(left, topNew, right, bottomNew);
                            rlVpContainner.setLayoutParams(vpParams);
                            mHeadLastY = (int) event.getRawY();

                            //重置顶部title的透明度
                            resetTitleAlpha(bottomNew);


                            //刷新动画开启,下滑,
                            if(isRefreshable()){
                                final ViewGroup.MarginLayoutParams p3 = (ViewGroup.MarginLayoutParams) mCGBHeader.getLayoutParams();
                                p3.setMargins(left, topNew-mCGBHeaderHeight, right, bottomNew+mCGBHeaderHeight);
                                mCGBHeader.setLayoutParams(p3);

                                //修改动画
                                mCGBHeader.onUIPositionChange(Math.abs(mHeadSlidedDistance),Math.abs(TOP_REFRESH_THRESHOLD));
                            }
                        }



                        break;

                    case MotionEvent.ACTION_UP://注意,全部是getRawX和getRawY
                        LogUtil.e(TAG, "MotionEvent.ACTION_UP");


                        final ViewGroup.MarginLayoutParams paramsNew = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
                        int topUp = paramsNew.topMargin;

                        final ViewGroup.MarginLayoutParams paramsNew2 = (ViewGroup.MarginLayoutParams) rlVpContainner.getLayoutParams();

                        final ViewGroup.MarginLayoutParams p3 = (ViewGroup.MarginLayoutParams) mCGBHeader.getLayoutParams();


                        LogUtil.e(TAG, "MotionEvent.ACTION_UP,topUp" + topUp);

                        if(topUp<=0){//上滑,几乎快要悬浮,则抬手时让它悬浮
                            if(Math.abs(Math.abs(topUp)-mScroolMax)<=FLOAT_THRESHOLD_Y ){
                                paramsNew.setMargins(0, -1 * mScroolMax, 0,mScroolMax);
                                v.setLayoutParams(paramsNew);

                                paramsNew2.setMargins(0,  -1 *mScroolMax, 0,mScroolMax);
                                rlVpContainner.setLayoutParams(paramsNew2);


                                p3.setMargins(0, mCGBHeaderHeight*-1,0, mCGBHeaderHeight);
                                mCGBHeader.setLayoutParams(p3);

                                //重置顶部title的透明度
                                resetTitleAlpha(mScroolMax);
                            }

                        }

                        if (topUp > 0) {//topUp>0表示是下滑后抬起手指,此时回弹head和底下的viewPager

                            if(!isRefreshable()|| Math.abs(mHeadSlidedDistance)<TOP_REFRESH_THRESHOLD){//无需刷新或者滑动太短,直接回去
                                ValueAnimator anim = ValueAnimator.ofInt(topUp, 0);
                                anim.setDuration(400); // 设置动画运行的时长 anim.setStartDelay(500); // 设置动画延迟播放时间
                                anim.setRepeatCount(0); // 设置动画重复播放次数 = 重放次数+1 // 动画播放次数 = infinite时,动画无限重复
                                // anim.setRepeatMode(ValueAnimator.RESTART);
                                anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                                    @Override
                                    public void onAnimationUpdate(ValueAnimator animation) {
                                        int currentValue = (Integer) animation.getAnimatedValue();

                                        paramsNew.setMargins(0, currentValue, 0, -1 * currentValue);
                                        v.setLayoutParams(paramsNew);

                                        paramsNew2.setMargins(0, currentValue, 0, -1 * currentValue);
                                        rlVpContainner.setLayoutParams(paramsNew2);

                                        p3.setMargins(0, mCGBHeaderHeight*-1,0, mCGBHeaderHeight);
                                        mCGBHeader.setLayoutParams(p3);
                                    }
                                });

                                anim.start();

                                //重置顶部title的透明度
                                resetTitleAlpha(0);
                            }

                            else{//需要开启刷新
                                ValueAnimator anim = ValueAnimator.ofInt(topUp, mCGBHeaderHeight);
                                anim.setDuration(400); // 设置动画运行的时长 anim.setStartDelay(500); // 设置动画延迟播放时间
                                anim.setRepeatCount(0); // 设置动画重复播放次数 = 重放次数+1 // 动画播放次数 = infinite时,动画无限重复
                                // anim.setRepeatMode(ValueAnimator.RESTART);
                                anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                                    @Override
                                    public void onAnimationUpdate(ValueAnimator animation) {
                                        int currentValue = (Integer) animation.getAnimatedValue();

                                        paramsNew.setMargins(0, currentValue, 0, -1 * currentValue);
                                        v.setLayoutParams(paramsNew);

                                        paramsNew2.setMargins(0, currentValue, 0, -1 * currentValue);
                                        rlVpContainner.setLayoutParams(paramsNew2);

                                        p3.setMargins(0, currentValue-mCGBHeaderHeight, 0, -1 * currentValue+mCGBHeaderHeight);
                                        mCGBHeader.setLayoutParams(p3);
                                    }
                                });
                                Animator.AnimatorListener li=new Animator.AnimatorListener() {
                                    @Override
                                    public void onAnimationStart(Animator animator) {
                                        mRefreshing=true;
                                    }

                                    @Override
                                    public void onAnimationEnd(Animator animator) {
                                        mCGBHeader.onUIRefreshBegin();
                                        mCGBHeader.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                onRefresh();//子类去干活
                                            }
                                        },1000);
                                    }

                                    @Override
                                    public void onAnimationCancel(Animator animator) {

                                    }

                                    @Override
                                    public void onAnimationRepeat(Animator animator) {

                                    }
                                };
                                anim.addListener(li);

                                anim.start();

                                //重置顶部title的透明度
                                resetTitleAlpha(0);
                            }

                        }



                        break;


                    case MotionEvent.ACTION_CANCEL:
                        break;

                }
                return true;
            }
        });

    }

    private int mContainerActionDownX, mContainerActionDownY, mContainerLastY, mContainerSlidedDistance;

    /**
     * @param :[]
     * @return type:void
     * @method name:initViewPagerContainerScrollListener
     * @des:给底下的viewpager的父容器控件加滑动监听: 上滑时, 改变控件的位置, 并带动上面的head一起滑动
     * 下滑时类似
     * 手指抬起时,如果是下滑的,则做一个回弹效果
     * 所有的滑动动作使用重置margin来实现
     * @date 创建时间:2018/5/23
     * @author Chuck
     **/
    private void initViewPagerContainerScrollListener() {

        /************
         *
         *  此监听器在控件的纯左右滑动时不会起作用,只在上下滑动时起作用. "醉美大连"业务里:
         tab已经浮动到了顶部,上滑则recyclerView消费滑动事件
         下滑:如果此时headview正在顶部,getY为0,则父容器消费滑动事件
         否recyclerView消费滑动事件(headview看不到了,下滑应该先把headview滑出来)

         tab不在顶部:
         上滑:父容器消费
         下滑:如果tab在原始位置,也就是最低位置,则recyclerView消费滑动事件
         否则:父容器消费滑动事件
         by:Chuck
         *
         *
         */

        rlVpContainner.setmInterceptProvider(new MyDispatchRelativeLayout.InterceptProvider() {

            @Override
            public void onActionDown(int actionDownY) {

                mContainerLastY=actionDownY;
                LogUtil.e(TAG, "通过InterceptProvider接口记录首次按下的Y坐标:" + mContainerLastY);
            }

            @Override
            public boolean onInterceptTouchEvent(boolean isScrollUp) {

                if(mRefreshing){//正在刷新,直接返回
                    return true;
                }
                return judgeIntercept(isScrollUp);

            }
        });


        rlVpContainner.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(final View v, MotionEvent event) {

                if(mRefreshing){//正在刷新,直接返回
                    return false;
                }

                switch (event.getAction()) {

                    case MotionEvent.ACTION_DOWN://按下时记录纵坐标,注意,如果按下时没有记录的话,这个值会是空,所以,在父容器里要把
                        //按下一瞬间的值传过来(接口形式),否则首次的值会是0.结果造成滑动距离超标
                        if (mScroolMax == 0) {//是个正值,head在上滑的上限值
                            //悬浮于顶部,距离值
                            mScroolMax = adList.getHeight()-rlTitleFilled.getHeight();

                            // mScroolMax = mViewBind.llHead.getHeight() - mViewBind.llTabContainer.getHeight();
                            LogUtil.e(TAG, "能上滑的最大距离:" + mScroolMax);
                        }

                        mContainerLastY = (int) event.getRawY();//最后一个action时Y值
                        mContainerActionDownX = (int) event.getRawX();//按下的瞬间X
                        LogUtil.e(TAG, "mContainerActionDownX:" + mContainerActionDownX);

                        mContainerActionDownY = (int) event.getRawY();//按下的瞬间Y
                        LogUtil.e(TAG, "mContainerActionDownY:" + mContainerActionDownY);

                        LogUtil.e(TAG, "=============================ACTION_DOWN,mContainerLastY" + mContainerLastY);

                        return true;//down事件必须消费

                    case MotionEvent.ACTION_MOVE:

                        LogUtil.e(TAG, "=============================ACTION_MOVE");
                        LogUtil.e(TAG, "event.getRawX()=============================" + event.getRawX());
                        LogUtil.e(TAG, "event.getRawY()=============================" + event.getRawY());

                        LogUtil.e(TAG, "mContainerLastY============================="+mContainerLastY);


                        /****************************************
                         * 测试:父容器不再拦截,直接在touch事件里处理掉.如果需要分发,则手动调用子类的onTounch
                         * 如果左右滑动,分发
                         * 如果上下滑动,根据tab的位置判断是否分发
                         *
                         */
                       /* boolean isScrollUp=event.getRawY()<=mContainerLastY;//上滑
                        LogUtil.e(TAG, "上滑?"+isScrollUp);

                        boolean isHonrizonalScroll=Math.abs(Math.abs(event.getRawX())-Math.abs(mContainerActionDownX))>MyDispatchRelativeLayout.SCROLL_THRESHOLD;
                        LogUtil.e(TAG, "横向滑动?"+isHonrizonalScroll);

                        if(  mDispatchEventToRecyclerView || isHonrizonalScroll || !judgeIntercept(isScrollUp)  ){
                            mDispatchEventToRecyclerView=true;
                            mViewBind.vp.onTouchEvent(event);//教给vp去消费
                            //mViewBind.vp.getO;//教给vp去消费
                            return false;
                        }*/
                        /****************************************
                         * 测试:父容器不再拦截,直接在touch事件里处理掉.如果需要分发,则手动调用子类的onTounch
                         * 如果左右滑动,分发
                         * 如果上下滑动,根据tab的位置判断是否分发
                         *
                         */


                        int containerLastdY = (int) event.getRawY() - mContainerLastY;
                        LogUtil.e(TAG, "containerLastdY=============================" + containerLastdY);

                        mContainerSlidedDistance = (int) event.getRawY() - mContainerActionDownY;
                        LogUtil.e(TAG, "父容器滑动距离:mSlidedDistance=" + mContainerSlidedDistance);

                        final ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
                        int left = params.leftMargin;
                        int top = params.topMargin;
                        int right = params.rightMargin;
                        int bottom = params.bottomMargin;

                        final ViewGroup.MarginLayoutParams vpParams = (ViewGroup.MarginLayoutParams) llHead.getLayoutParams();
                        //int left = vpParams.leftMargin;
                        //int top = vpParams.topMargin;
                        //int right = vpParams.rightMargin;
                        //int bottom =vpParams.bottomMargin;

                        LogUtil.e(TAG, "container:left:" + left + ",top:" + top + ",right:" + right + ",bottom" + bottom);

                        int topNew = top + containerLastdY;
                        int bottomNew = bottom - containerLastdY;

                        LogUtil.e(TAG, "container:topNew:" + topNew + ",bottomNew" + bottomNew);



                        //上滑极限是tab的位置,不可下滑(相对于初始位置来讲)!!!! topNew小于0是上滑  topNew大于0是下滑  by:Chuck 2018/05/23
                        if ((topNew <= 0 && Math.abs(topNew) <= mScroolMax)) {

                            if(topNew <= 0 && Math.abs(topNew) >= mScroolMax){//滑动超标了.因为回调并不是一个像素一个像素的
                                topNew=-1 * mScroolMax;//赋值为阈值
                            }

                            LogUtil.e(TAG, topNew + "container=============================MOVE");
                            params.setMargins(left, topNew, right, bottomNew);
                            v.setLayoutParams(params);

                            vpParams.setMargins(left, topNew, right, bottomNew);
                            llHead.setLayoutParams(vpParams);
                            mContainerLastY = (int) event.getRawY();

                            //重置顶部title的透明度
                            resetTitleAlpha(bottomNew);
                        }
                        /*else{//分发给viewpager ps:这样处理无效.结果就是,recyclerview在从底部滑到顶部时.需要松手再滑,才能使recyclerview滑到
                            vp.onTouchEvent(event);
                            mContainerLastY = (int) event.getRawY();
                            resetTitleAlpha(bottomNew);
                            return false;
                        }*/


                        break;

                    case MotionEvent.ACTION_UP://注意,全部是getRawX和getRawY
                        LogUtil.e(TAG, "MotionEvent.ACTION_UP");



                        //vp父容器的布局参数
                        final ViewGroup.MarginLayoutParams paramsVpUp = (ViewGroup.MarginLayoutParams) v.getLayoutParams();

                        //head的布局参数
                        final ViewGroup.MarginLayoutParams paramsUp = (ViewGroup.MarginLayoutParams) llHead.getLayoutParams();

                        //上滑到一个临界值,container:left:0,top:-702,right:0,bottom702  则把它设置为悬浮状态
                        if(paramsUp.bottomMargin>0 && Math.abs(mScroolMax-paramsUp.bottomMargin)<=FLOAT_THRESHOLD_Y ){
                            paramsUp.setMargins(0, -1 * mScroolMax, 0,mScroolMax);
                            v.setLayoutParams(paramsUp);

                            paramsVpUp.setMargins(0,  -1 *mScroolMax, 0,mScroolMax);
                            rlVpContainner.setLayoutParams(paramsVpUp);

                            //重置顶部title的透明度
                            resetTitleAlpha(mScroolMax);
                        }

                        //下滑,如果手指抬起来时,tab的位置与初始值的位置的阈值在MARGIN_THRESHOLD范围内,则把一切还原
                        else if(Math.abs(paramsUp.topMargin)<=FLOAT_THRESHOLD_Y){//已经在底部,初始的位置
                            LogUtil.e(TAG, "tab在最底部,下滑,不拦截>>>recyclerView消费滑动事件");


                            paramsVpUp.setMargins(0, 0,0, 0);
                            v.setLayoutParams(paramsVpUp);

                            paramsUp.setMargins(0, 0, 0, 0);
                            llHead.setLayoutParams(paramsUp);

                            try {
                                CustomBaseFragment2 fragment = mPagerList.get(initIndex);
                                LogUtil.e(TAG, "返回顶部,fragment.isVisible()?" + fragment.isVisible());
                                if (fragment!=null&&fragment.isVisible()) {
                                    fragment.setSelection(0);//滑动到顶部
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            //重置顶部title的透明度
                            resetTitleAlpha(0);
                        }




                        return true;
                    //break;


                    case MotionEvent.ACTION_CANCEL:
                        break;

                }
                return false;
            }
        });

    }

    /**
     * @method name:judgeIntercept
     * @des:是否拦截事件.true则拦截,拦截了则recyclerView滑动不了
     * @param :[isScrollUp]
     * @return type:boolean
     * @date 创建时间:2018/5/24
     * @author Chuck
     **/
    protected boolean judgeIntercept(boolean isScrollUp) {



        final ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) llHead.getLayoutParams();

        int bottom = params.bottomMargin;

        LogUtil.e(TAG, "此时head的marginBottom:" + bottom);

        if (Math.abs(bottom-mScroolMax)<=MARGIN_THRESHOLD) {//满足这个条件,则表示,切换碎片的tab已经悬浮于顶部了,此时可以返回false,父容器不拦截,则子控件viewPager(里面是frgment)自行处理事件



            //return false;

            if (isScrollUp) {//上滑
                LogUtil.e(TAG, "tab在顶部,上滑,不拦截>>>recyclerView消费滑动事件");
                return false;
            } else {//下滑
                try {
                    CustomBaseFragment2 fragment = mPagerList.get(initIndex);
                    if (fragment!=null&&fragment.isHeadviewAtTopNow()) {//下滑,如果自定义的headview就在顶部,再下滑,就要返回true,不触发recyclerView的滑动事件
                        LogUtil.e(TAG, "tab在顶部,下滑,recyclerView的headview在顶部,拦截>>>父容器消费滑动事件");
                        return true;
                    } else {//headview不再顶部,则让recyclerview消费滑动事件
                        LogUtil.e(TAG, "tab在顶部,下滑,recyclerView的headview不在顶部,不拦截>>>recyclerView消费滑动事件");
                        return false;
                    }
                } catch (Exception e) {
                    LogUtil.e(TAG, "抛异常,拦截>>>父容器消费滑动事件");

                    e.printStackTrace();
                    return true;
                }
            }
        }
        else {
            if (isScrollUp) {//上滑,父类消费滑动事件
                LogUtil.e(TAG, "tab不在顶部,上滑,拦截>>>父容器消费滑动事件");

                return true;
            } else {//下滑
                if(Math.abs(params.topMargin)<=MARGIN_THRESHOLD){//已经在底部,初始的位置
                    LogUtil.e(TAG, "tab在最底部,下滑,不拦截>>>recyclerView消费滑动事件");
                    return  false;//
                }
                else{
                    LogUtil.e(TAG, "tab在中间,下滑,拦截>>>父容器消费滑动事件");
                    return true;
                }
            }
        }
    }


    protected void resetTitleAlpha(int llHeadMarginBottom){

        LogUtil.i(TAG,"resetTitleAlpha:llHeadMarginBottom,="+llHeadMarginBottom);
        //原始值
        int orginalMarginBottom = 0;

        //滑动到limit时,把mainActivity的titile的alpha值设置为1
        int limit =mScroolMax;//tab处于顶部时的marginBottom

        float alpha = 0.0f;

        //llHeadMarginBottom     alpha
        //707                     1
        //600                     0.8
        //500                     0.5
        //330                     0.3
        //0                       0

        if (llHeadMarginBottom <= orginalMarginBottom) {
            alpha=0;
        } else if (llHeadMarginBottom > orginalMarginBottom && llHeadMarginBottom < limit) {//上滑
            alpha = 1.0f - (1.0f * (limit - llHeadMarginBottom) / (limit - orginalMarginBottom));
        } else if (llHeadMarginBottom >= limit) {
            alpha = 1.0f;
        }
        LogUtil.i(TAG,"resetTitleAlpha:alpha,="+alpha);

        ivBackground.setAlpha(alpha);



    }


    /**
     * @method name:setAlpha
     * @des:子类可以重写,拿到这个alpha,对某个控件进行处理
     * @param :[alpha]
     * @return type:void
     * @date 创建时间:2018/12/6
     * @author Chuck
     **/
    protected void setAlpha(float alpha){

    }


    /**
     * @param :[]
     * @return type:void
     * @method name:getScroolMax
     * @des:初始化滑动阈值
     * @date 创建时间:2018/5/23
     * @author Chuck
     **/
    private void getScroolMax() {
        ViewTreeObserver viewTreeObserver = getView().getViewTreeObserver();
        if (viewTreeObserver != null) {//绘制完成的监听
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {

                    LogUtil.i(TAG,"onGlobalLayout()");
                    LogUtil.i(TAG,"onGlobalLayout(),屏幕总高度:"+DensityUtil.getDisplayHeight(getActivity()));


                    mCGBHeaderHeight=mCGBHeader.getHeight();

                    LogUtil.i(TAG,"动画控件高度:"+mCGBHeaderHeight);

                    final ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) mCGBHeader.getLayoutParams();
                    params.setMargins(0, mCGBHeaderHeight*-1,0, mCGBHeaderHeight);
                    mCGBHeader.setLayoutParams(params);

                    DOWN_BACK_THRESHOLD = mCGBHeaderHeight*2;//是否可以下滑,下滑的上限.有则滑到极限松手,回弹
                    TOP_REFRESH_THRESHOLD = mCGBHeaderHeight;//触发刷新的阈值


                    //如果虚拟物理键盘被隐藏了,这个应该会被回调.所以不再remove监听.只要界面有重绘,就重新设置viewPager的高度
                    getView().getViewTreeObserver().removeGlobalOnLayoutListener(this);//只需要监听一次，之后通过listener回调即可

                    //悬浮于顶部,距离值
                    mScroolMax =adList.getHeight()-rlTitleFilled.getHeight();

                    LogUtil.e(TAG, "能上滑的最大距离:" + mScroolMax);

                    //重置viewPager的最大高度:  注意:虚拟返回键被收起后,看看获得到的屏幕高度是否有变化
                    //rl_vp_containner  屏幕高度-title-tab-底部tab
                    //int maxHeight= DensityUtil.getDisplayHeight(getActivity()) - DensityUtil.dp2px(getActivity(),70+45+0);

                    //不再使用这种方式获取能滑动的最大高度,因为界面可能不包含状态栏
                    //int maxHeight= DensityUtil.getDisplayHeight(getActivity())-rlTitleFilled.getHeight()-mTabContainer.getHeight();

                    int maxHeight= rl_content_root.getHeight()-rlTitleFilled.getHeight()-mTabContainer.getHeight();
                    ViewGroup.LayoutParams pa=rlVpContainner.getLayoutParams();
                    pa.height=maxHeight;
                    LogUtil.i(TAG,"重置vp父容器的高度值:"+maxHeight);
                    rlVpContainner.setLayoutParams(pa);


                }
            });
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        LogUtil.e(TAG, "POSITION:" + position);
        initIndex = position;//重置position
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }


    /**
     * ViewPager适配器
     */
    public class MyViewPagerAdapter extends FragmentPagerAdapter {

        public MyViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int arg0) {
            return mPagerList.get(arg0);
        }

        @Override
        public int getCount() {
            return mPagerList.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "";
        }
    }

    /**
     * @method name:getFragmnets
     * @des:填充碎片
     * @param :[]
     * @return type:void
     * @date 创建时间:2018/5/29
     * @author Chuck
     **/
    private void getFragmnets() {
        /*for (int i = 0; i < mCropsBeans.size(); i++) {
            CateBean bean = mCropsBeans.get(i);//获取战队,传值进里面的fragment
            fragment = new HomeFragment();
            // TODO: 2017/9/15 碎片传参数
            Bundle bundle = new Bundle();
            bundle.putInt(HomeFragment.INTENT_KEY_CROPS_ID, bean.getCateId());
            fragment.setArguments(bundle);
            mPagerList.add(fragment);
        }*/




        mPagerList.addAll(getSubFragments());

        if (vp.getAdapter() == null) {
            //初始化ViewPager
            //vp.setAdapter(new MyViewPager(getChildFragmentManager()));//注意fragmentAdapter的构造器
            vp.setAdapter(getMyViewPagerAdapter());//注意fragmentAdapter的构造器
            vp.setOnPageChangeListener(this);

            LogUtil.e(TAG, "initIndex:" + initIndex);

            if(initIndex<0||initIndex>=mPagerList.size()){
                initIndex=0;
            }
            LogUtil.e(TAG, "initIndex:" + initIndex);

            vp.setCurrentItem(initIndex);

            //tab的初始化、tab和ViewPager的互相绑定
            //tabs.setSmoothScroll(false);
            //tabs.setViewPager(mViewBind.vp);
            //tabs.setOnPageChangeListener(this);
        }
    }

    /**
     * @method name:onRefresh
     * @des:刷新,准备请求数据
     * @param :[]
     * @return type:void
     * @date 创建时间:2018/12/5
     * @author Chuck
     **/
    protected    void  onRefresh(){


        mTitleViewRoot.setVisibility(View.INVISIBLE);

         mCGBHeader.postDelayed(new Runnable() {
             @Override
             public void run() {
                 refreshCompleted();
             }
         },2000);

    }

    /**
     * @method name:refreshCompleted
     * @des:刷新完成
     * @param :[]
     * @return type:void
     * @date 创建时间:2018/12/5
     * @author Chuck
     **/
    protected    void  refreshCompleted(){
        mTitleViewRoot.setVisibility(View.VISIBLE);

        mRefreshing=false;
        mCGBHeader.onUIRefreshComplete();
        final ViewGroup.MarginLayoutParams paramsNew = (ViewGroup.MarginLayoutParams) llHead.getLayoutParams();

        final ViewGroup.MarginLayoutParams paramsNew2 = (ViewGroup.MarginLayoutParams) rlVpContainner.getLayoutParams();

        final ViewGroup.MarginLayoutParams p3 = (ViewGroup.MarginLayoutParams) mCGBHeader.getLayoutParams();

        ValueAnimator anim = ValueAnimator.ofInt(mCGBHeaderHeight, 0);
        anim.setDuration(400); // 设置动画运行的时长 anim.setStartDelay(500); // 设置动画延迟播放时间
        anim.setRepeatCount(0); // 设置动画重复播放次数 = 重放次数+1 // 动画播放次数 = infinite时,动画无限重复
        // anim.setRepeatMode(ValueAnimator.RESTART);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int currentValue = (Integer) animation.getAnimatedValue();

                paramsNew.setMargins(0, currentValue, 0, -1 * currentValue);
                llHead.setLayoutParams(paramsNew);

                paramsNew2.setMargins(0, currentValue, 0, -1 * currentValue);
                rlVpContainner.setLayoutParams(paramsNew2);

                p3.setMargins(0, currentValue-mCGBHeaderHeight, 0, mCGBHeaderHeight- currentValue);
                mCGBHeader.setLayoutParams(p3);
            }
        });

        anim.start();
    }

    /**
     * @method name:isRefreshable()
     * @des:是否支持顶部刷新
     * @param :[]
     * @return type: boolean
     * @date 创建时间:2018/12/3
     * @author Chuck
     **/
    protected      boolean isRefreshable() {
        return true;
    }


    /**
     * @method name:getMyViewPager
     * @des:viewPager适配器
     * @param :[]
     * @return type:linklink.com.scrollview_within_recyclerview.ui.CustomMainFragment.MyViewPager
     * @date 创建时间:2018/12/3
     * @author Chuck
     **/
    protected      MyViewPagerAdapter getMyViewPagerAdapter(){
        return new MyViewPagerAdapter(getChildFragmentManager());
    }

    /**
     * @method name:autoSetupViewPager
     * @des:是否开始就自动填充viewpager,加这个方法,使得子类能动态修改viewapager(因为有的时候数据类型是要另外通过接口获取的)
     * @param :[]
     * @return type:boolean
     * @date 创建时间:2018/12/4
     * @author Chuck
     **/
    protected   boolean  autoSetupViewPager(){
        return true;
    }




    /**
     * @method name:getTitleBackgroundRes
     * @des:title背景资源设置
     * @param :[]
     * @return type:int
     * @date 创建时间:2018/6/25
     * @author Chuck
     **/
    public  abstract int getTitleBackgroundRes();

    /**
     * @method name:getTitleView
     * @des:设置titleview.
     * @param :[]
     * @return type:android.view.View
     * @date 创建时间:2018/6/25
     * @author Chuck
     **/
    public  abstract View getTitleView();

    /**
     * @method name:getTitleViewParentHeight
     * @des:重置title父容器总高度(不包括getTitleView,因为getTitleView的父容器它有一个paddingTop)
     * @param :[]
     * @return type:int
     * @date 创建时间:2018/6/25
     * @author Chuck
     **/
    public  abstract int getTitleViewParentHeight();

    /**
     * @method name:getTitleViewMarginTop
     * @des:重置title父容器paddingTop,单位:像素
     * @param :[]
     * @return type:int
     * @date 创建时间:2018/6/25
     * @author Chuck
     **/
    public  abstract int getTitleViewMarginTop();

    /**
     * @method name:getHeadView
     * @des:顶部的"headview" 可以是包含 左右滑动的banner.滑动事件的分发逻辑已经处理了.左右滑动时.事件将被banner消费
     * @param :[]
     * @return type:android.view.View
     * @date 创建时间:2018/6/25
     * @author Chuck
     **/
    public  abstract  View getHeadView();

    /**
     * @method name:getFloatView
     * @des:悬浮控件设置,悬浮控件和底下viewpager的绑定,可以在onActivityCreated里添加
     * @param :[]
     * @return type:android.view.View
     * @date 创建时间:2018/6/25
     * @author Chuck
     **/
    public  abstract   View getFloatView();

    /**
     * @method name:getSubFragments
     * @des:设置子碎片,CustomBaseFragment2是自定义类,里面必须实现的是isHeadviewAtTopNow()
     * @param :[]
     * @return type:java.util.ArrayList<linklink.com.scrollview_within_recyclerview.base.CustomBaseFragment2>
     * @date 创建时间:2018/6/25
     * @author Chuck
     **/
    public  abstract   ArrayList<CustomBaseFragment2> getSubFragments();





}
