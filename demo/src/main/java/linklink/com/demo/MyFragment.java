package linklink.com.demo;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import java.util.ArrayList;

import linklink.com.scrollview_within_recyclerview.base.CustomBaseFragment2;
import linklink.com.scrollview_within_recyclerview.ui.*;
import linklink.com.scrollview_within_recyclerview.utils.DensityUtil;

/**
 * MyFragment
 * 责任人:  Chuck
 * 修改人： Chuck
 * 创建/修改时间: 2018/6/25  16:43
 * Copyright : 2017-2018 深圳令令科技有限公司-版权所有
 **/
public class MyFragment extends CustomMainFragment {

    @Override
    protected void setAlpha(float alpha) {
        super.setAlpha(alpha);
        Log.i("MyFragment","ALPHA:"+alpha);
    }

    @Override
    public int getTitleBackgroundRes(){
        //设置透明控件的背景资源.
        return R.drawable.shape_blue_rect;
    }

    @Override
    public View getTitleView(){
        //设置顶部的title布局
        return LayoutInflater.from(getActivity()).inflate(R.layout.title, null);
    }

    @Override
    public int getTitleViewParentHeight(){
        //设置getTitleView()的父容器的高度值.返回title的实际高度 加上 getTitleViewMarginTop()就行
        return DensityUtil.dp2px(getActivity(),50)  +  getTitleViewMarginTop();//50是title布局里写死的50dp
    }


    @Override
    protected View getRightFloatView() {
        return LayoutInflater.from(getActivity()).inflate(R.layout.right_float_btn, null);

    }

    @Override
    public int getTitleViewMarginTop() {


        //设置TitleView的MarginTop,单位:像素.因为有些界面可能包含了顶部的状态栏.
        // 包含了状态栏,这个方法返回你获取的状态栏高度
        // 不包含状态栏,这个方法直接返回0
        return DensityUtil.dp2px(getActivity(),20);
    }

    @Override
    public View getHeadView(){
        //这里可以返回一个左右滑动的banner.滑动事件的分发逻辑已经处理过
        return LayoutInflater.from(getActivity()).inflate(R.layout.banner, null);
    }

    @Override
    public View getFloatView(){
        //设置悬浮控件,如果需要与viewpager绑定,可以定义一个成员变量,然后重写onActivityCreated,添加绑定逻辑
        return LayoutInflater.from(getActivity()).inflate(R.layout.float_view, null);
    }

    @Override
    public ArrayList<CustomBaseFragment2> getSubFragments(){
        //在viewpager里添加子碎片.CustomBaseFragment2
        ArrayList<CustomBaseFragment2> list =new ArrayList<>();
        SubFragment1 subFragment1=new SubFragment1();
        list.add(subFragment1);

        SubFragment2 subFragment2=new SubFragment2();
        list.add(subFragment2);
        return list;
    }

    @Override
    public MyViewPagerAdapter getMyViewPagerAdapter() {
        return new CustomViewPager(getChildFragmentManager());
    }

    /**
     * ViewPager适配器
     */
    public class CustomViewPager extends MyViewPagerAdapter {

        public CustomViewPager(FragmentManager fm) {
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
            return "测试";
        }
    }

}
