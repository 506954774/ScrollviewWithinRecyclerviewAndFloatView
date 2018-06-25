# ScrollviewWithinRecyclerviewAndFloatView
安卓滑动控件里嵌套Recyclerview,带浮动效果


### 添加依赖:</br>
dependencies {</br>
    // your dependencies ...</br>
    compile 'com.linklink.views:ScrollviewWithinRecyclerviewAndFloatView:1.0.0'</br>
}</br>

### 效果图:</br>

 ![img](https://raw.githubusercontent.com/506954774/ScrollviewWithinRecyclerviewAndFloatView/master/scrollview_within_recyclerview.gif)


  ## MainActivity.java : </br>
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