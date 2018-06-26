# ScrollviewWithinRecyclerviewAndFloatView
安卓滑动控件里嵌套Recyclerview,带浮动效果


### 添加依赖:</br>
dependencies {</br>
    // your dependencies ...</br>
    compile 'com.linklink.views:ScrollviewWithinRecyclerviewAndFloatView:1.0.0'</br>
}</br>

### 效果图:</br>

 ![img](https://raw.githubusercontent.com/506954774/ScrollviewWithinRecyclerviewAndFloatView/master/scrollview_within_recyclerview.gif)

### 概述:</br>
这个控件并不是继承了Scrollview,而是通过两个自定义控件,控制事件分发,使界面达到上图的效果.界面是Fragment,里</br>
面的viewPager可以添加多个子碎片,子碎片里可以放Recyclerview.子碎片必须实现public boolean isHeadviewAtTopNow(),这</br>
个方法的返回值将作为滑动事件分发的重要参考值.ViewPager上面的headview可以添加左右滑动的广告banner控件,其</br>
事件分发逻辑已经处理过:左右滑动,则banner消费事件,上下滑动则父容器消费事件</br>

  ## MainActivity.java : </br>
  ```Java
  package linklink.com.demo;

  import android.os.Bundle;
  import android.support.v4.app.Fragment;
  import android.support.v4.app.FragmentActivity;

  public class MainActivity extends FragmentActivity {

      @Override
      protected void onCreate(Bundle savedInstanceState) {
          super.onCreate(savedInstanceState);
          setContentView(R.layout.activity_main);

          Fragment fragment=new MyFragment();
          if(fragment!=null){
              getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commitAllowingStateLoss();
          }
      }
  }
 ```

 ## MyFragment.java : </br>
   ```Java
   package linklink.com.demo;

   import android.view.LayoutInflater;
   import android.view.View;

   import java.util.ArrayList;

   import linklink.com.scrollview_within_recyclerview.base.CustomBaseFragment2;
   import linklink.com.scrollview_within_recyclerview.ui.CustomMainFragment;
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
               return linklink.com.scrollview_within_recyclerview.R.drawable.shape_blue_rect;
           }

           public View getTitleView(){
               //设置顶部的title布局
               return LayoutInflater.from(getActivity()).inflate(linklink.com.scrollview_within_recyclerview.R.layout.title, null);
           }

           public int getTitleViewParentHeight(){
               //设置getTitleView()的父容器的高度值.返回title的实际高度 加上 getTitleViewMarginTop()就行
               return DensityUtil.dp2px(getActivity(),50)  +  getTitleViewMarginTop();//50是title布局里写死的50dp
           }


           @Override
           public int getTitleViewMarginTop() {
               //设置TitleView的MarginTop,单位:像素.因为有些界面可能包含了顶部的状态栏.
               // 包含了状态栏,这个方法返回你获取的状态栏高度
               // 不包含状态栏,这个方法直接返回0
               return DensityUtil.dp2px(getActivity(),20);
           }

           public View getHeadView(){
               //这里可以返回一个左右滑动的banner.滑动事件的分发逻辑已经处理过
               return LayoutInflater.from(getActivity()).inflate(linklink.com.scrollview_within_recyclerview.R.layout.banner, null);
           }

           public View getFloatView(){
               //设置悬浮控件,如果需要与viewpager绑定,可以定义一个成员变量,然后重写onActivityCreated,添加绑定逻辑
               return LayoutInflater.from(getActivity()).inflate(linklink.com.scrollview_within_recyclerview.R.layout.float_view, null);
           }

           public ArrayList<CustomBaseFragment2> getSubFragments(){
               //在viewpager里添加子碎片.CustomBaseFragment2
               ArrayList<CustomBaseFragment2> list =new ArrayList<>();
               SubFragment1 subFragment1=new SubFragment1();
               list.add(subFragment1);

               SubFragment2 subFragment2=new SubFragment2();
               list.add(subFragment2);
               return list;
           }
   }
  ```