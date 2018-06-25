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
并不是继承了Scrollview,而是通过控制事件分发,使界面达到上图的效果.界面是Fragment,里面的viewPager</br>
里面添加子碎片.碎片里可以放Recyclerview.viewPager上面的headview可以添加左右滑动的广告banner控件,其</br>
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
           return R.drawable.shape_blue_rect;
       }

       public View getTitleView(){
           //设置title控件
           return LayoutInflater.from(getActivity()).inflate(R.layout.title, null);
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
           //返回子碎片.CustomBaseFragment2
           ArrayList<CustomBaseFragment2> list =new ArrayList<>();
           SubFragment1 subFragment1=new SubFragment1();
           list.add(subFragment1);

           SubFragment2 subFragment2=new SubFragment2();
           list.add(subFragment2);
           return list;
       }
   }
  ```