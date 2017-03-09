package ee.hacc.sea.support;

import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hacceee - yangchundong on 2017/3/6.
 */

/**
 * adapter 折叠树使用的model
 * @param <M> 自己类型
 */
public class ExpandableWrapper </*P extends  AdapterNode<?, ?>, */M extends AdapterNode <?, ?>/*, C extends  AdapterNode<?,?>*/> {

    private static final String TAG = "ExpandableWrapper";

    private M mMine;
    private List<ExpandableWrapper<?>> mChildrenWrapper;

    /**
     * 层级
     */
    private int mHierarchy;

    /**
     * 是否可以执行折叠
     */
    private boolean isExpandable;
    private boolean isExpandableClick;
    private ExpandableWrapper<?> mParent;

    /**
     * 是否已经折叠
     */
    private boolean isExpanded;


    /**
     *
     * @param mine adapter bind数据模型
     * @param hierarchy 层级
     */
    public ExpandableWrapper(  ExpandableWrapper<?> parent, @NonNull  M mine, @NonNull int hierarchy){
//        this.mParent = mine.getParent();
        this.mMine = mine;
        this.mHierarchy = hierarchy;
        this.mParent = parent;
        init();
    }

    private void init(){
        /**
         * 爹为null的情况说明已经为最高节点了
         */
//        if(mParent == null){
            isExpandable = mMine.initExpandable();
//        }else{
//            isExpandable = mParent.isExpandable();
//        }
//        isExpandable = mMine.initExpandable();
        isExpandableClick = mMine.initExpandClickable();
        mChildrenWrapper = new ArrayList<>();
//        mChildren = mMine.getChildren();
    }


    public void addChildrenWrapper(ExpandableWrapper<?> childrenwrapper){
        mChildrenWrapper.add(childrenwrapper);
    }

    public void addChildrenWrapper(int position, ExpandableWrapper<?> childrenWrapper){
        if(position < 0 || position > mChildrenWrapper.size()){
            Log.e(TAG, "out of index!");
            return ;
        }
        mChildrenWrapper.add(position, childrenWrapper);
    }

    public void addChildrenWrappers(int position, List<ExpandableWrapper<?>> childrenWrappers){
        mChildrenWrapper.addAll(position, childrenWrappers);
    }


    public List<ExpandableWrapper<?>> getChildren(){
        return this.mChildrenWrapper;
    }

    public ExpandableWrapper<?> getChildAt(int index){
        return this.mChildrenWrapper.get(index);
    }

    public int indexChildOf(ExpandableWrapper<?> wrapper){
        return this.mChildrenWrapper.indexOf(wrapper);
    }

    public boolean removeChildAt(int index){
         mChildrenWrapper.remove(index);
        return true;
    }

    public  boolean removeChildWithObject(ExpandableWrapper c){
        mChildrenWrapper.remove(c);
        return true;
    }



    public int getChildrenCount(){
        return mChildrenWrapper.size();
    }

//    public List<C> getChildren(){
//        return this.mChildren;
//    }

    public int getmHierarchy(){
        return this.mHierarchy;
    }


    /**
     * 开启或关闭折叠
     * @param isExpandable false 为关闭
     */
    public void setExpandable(boolean isExpandable){
        this.isExpandable = isExpandable;
//        if(withChilds) {
//            for (ExpandableWrapper c : mChildrenWrapper) {
//                c.setExpandable(isExpandable, withChilds);
//            }
//        }
    }

    public boolean isExpandable(){
        return this.isExpandable;
    }

    public void setExpanded(boolean isExpanded){
        this.isExpanded = isExpanded;
    }

    public  boolean isExpanded(){
        return  this.isExpanded;
    }

    public void setExpandClickable(boolean isExpandClickable){
        this.isExpandableClick = isExpandClickable;
    }

    public boolean isExpandClickable(){
        return this.isExpandableClick;
    }

    public  ExpandableWrapper<?> getmParent(){
        return mParent;
    }

    /**
     * 获取到祖先节点，也就是最高级别节点
     * @return
     */
    public ExpandableWrapper<?> getAncestors(){
        if(getmParent() != null){
            return getmParent();
        }else{
            return this;
        }
    }

    public M getMine(){
        return  this.mMine;
    }


}
