package linklink.com.scrollview_within_recyclerview.base;

import android.support.v4.app.Fragment;

/**
 * CustomBaseFragment2
 * 责任人:  Chuck
 * 修改人： Chuck
 * 创建/修改时间: 2018/6/25  11:44
 * Copyright : 2017-2018 深圳令令科技有限公司-版权所有
 **/
public abstract class CustomBaseFragment2 extends Fragment {


    public void setSelection(int position){
    }

    /**
     * @method name:isHeadviewAtTopNow
     * @des:返回:headview或者第一个item是否在顶部.  这个判断的返回值将作为事件分发的重要参考值.例如:
     * 当recyclerview在顶部时.如果此时headview或者第一个item是否在顶部,则再下滑时,就要把整个外部的headview拉下来,而不是再触发recyclerview的下拉刷新
     * @param :[]
     * @return type:boolean
     * @date 创建时间:2018/6/25
     * @author Chuck
     **/
    public abstract boolean isHeadviewAtTopNow();

}
