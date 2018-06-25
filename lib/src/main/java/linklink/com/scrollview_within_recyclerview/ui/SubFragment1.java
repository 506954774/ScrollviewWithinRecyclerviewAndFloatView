package linklink.com.scrollview_within_recyclerview.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import linklink.com.scrollview_within_recyclerview.R;
import linklink.com.scrollview_within_recyclerview.base.CustomBaseFragment2;
import linklink.com.scrollview_within_recyclerview.utils.LogUtil;

/**
 * SubFragment1
 * 责任人:  Chuck
 * 修改人： Chuck
 * 创建/修改时间: 2018/6/25  14:39
 * Copyright : 2017-2018 深圳令令科技有限公司-版权所有
 **/
public class SubFragment1 extends CustomBaseFragment2 {


    private static  String TAG = "SubFragment1";

    protected RecyclerView mRecyclerView;


    private ArrayList<String> mDatas;


    //第一个item(包含headview)是否在顶部
    public boolean isHeadviewAtTopNow(){
        LogUtil.i(TAG,"mRecyclerView:"+mRecyclerView);

        View view = mRecyclerView.getChildAt(0);

        LogUtil.i(TAG,"view:"+view);

        if(mRecyclerView!=null&&view!=null){

            int[] outLocation=new int[2];
            view.getLocationOnScreen(outLocation);

            int[] outLocation2=new int[2];
            mRecyclerView.getLocationOnScreen(outLocation2);

            LogUtil.i(TAG,"第一个view在屏幕上的绝对位置,outLocation[1]:"+outLocation[1]);
            LogUtil.i(TAG,"recyclerview在屏幕上的绝对位置,outLocation2[1]:"+outLocation2[1]);


            //return mRecyclerViewHeadView.getTop()==0;
            return outLocation[1]==outLocation2[1];
        }
        else{
            LogUtil.i(TAG,"mRecyclerViewHeadView==null,return false");
            return false;
        }

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        TAG=this.getClass().getSimpleName();
        return inflater.inflate(R.layout.fragment_sub, container, false);
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initData();

        mRecyclerView = (RecyclerView) getView().findViewById(R.id.id_recyclerview);
        //设置布局管理器
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        //设置adapter
        HomeAdapter adapter = new HomeAdapter();
        mRecyclerView.setAdapter(adapter);
        //设置Item增加、移除动画
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        //添加分割线
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(),DividerItemDecoration.HORIZONTAL));
    }

    private class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.MyViewHolder>
    {
        //生成holder
        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
        {
            MyViewHolder holder = new MyViewHolder(LayoutInflater.from(
                    getActivity()).inflate(R.layout.item, parent,
                    false));
            return holder;
        }

        //绑定holder
        @Override
        public void onBindViewHolder(MyViewHolder holder, int position)
        {
            holder.tv.setText(mDatas.get(position));
        }

        @Override
        public int getItemCount()
        {
            return mDatas.size();
        }

        class MyViewHolder extends RecyclerView.ViewHolder
        {

            TextView tv;

            public MyViewHolder(View view)
            {
                super(view);
                tv = (TextView) view.findViewById(R.id.id_num);
            }
        }
    }

    protected void initData()
    {
        mDatas = new ArrayList<String>();
        for (int i = 'A'; i <= 'z'; i++)
        {
            mDatas.add(getFragmentMark() + (char) i);
        }
    }


    //为了界面上区分子页,加个前缀
    protected String getFragmentMark(){
        return  "碎片1 ";
    }
}
