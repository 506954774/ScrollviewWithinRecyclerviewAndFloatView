package linklink.com.scrollview_within_recyclerview;

import android.view.LayoutInflater;
import android.view.View;

import java.util.ArrayList;

import linklink.com.scrollview_within_recyclerview.base.CustomBaseFragment2;
import linklink.com.scrollview_within_recyclerview.ui.CustomMainFragment;
import linklink.com.scrollview_within_recyclerview.ui.SubFragment1;
import linklink.com.scrollview_within_recyclerview.ui.SubFragment2;
import linklink.com.scrollview_within_recyclerview.utils.DensityUtil;

/**
 * MyFragment
 * 责任人:  Chuck
 * 修改人： Chuck
 * 创建/修改时间: 2018/6/25  16:43
 * Copyright : 2017-2018 深圳令令科技有限公司-版权所有
 **/
public class MyFragment extends CustomMainFragment {

    public int getTitleBackgroundRes(){
        //透明控件的背景资源.
        return R.drawable.shape_blue_rect;
    }

    public View getTitleView(){
        return LayoutInflater.from(getActivity()).inflate(R.layout.title, null);
        //return null;
    }

    public int getTitleViewNewHeight(){
        //因为里面包含一个透明度可变化的imageview.注意,这里的高度是getTitleView()的dp值+ 20(因为有paddingTop=20dp) .例如你的title是50dp,则这里要返回70dp
        return DensityUtil.dp2px(getActivity(),70);
    }

    public View getHeadView(){
        //这里可以返回一个左右滑动的banner.滑动事件的分发逻辑已经处理过
        return LayoutInflater.from(getActivity()).inflate(R.layout.banner, null);
    }

    public View getFloatView(){
        //设置悬浮控件,如果需要与viewpager绑定,可以定义一个成员变量,然后重写onActivityCreated,添加绑定逻辑
        return LayoutInflater.from(getActivity()).inflate(R.layout.float_view, null);
    }

    public ArrayList<CustomBaseFragment2> getSubFragments(){
        ArrayList<CustomBaseFragment2> list =new ArrayList<>();
        SubFragment1 subFragment1=new SubFragment1();
        list.add(subFragment1);

        SubFragment2 subFragment2=new SubFragment2();
        list.add(subFragment2);
        return list;
    }
}
