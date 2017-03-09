package ee.hacc.sea.support;

import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import java.util.List;
import java.util.Objects;

/**
 * Created by haceee - yangchundong on 2017/3/5.
 * 三级adapter
 *
 */

public abstract   class SuperAdapter<G extends AdapterNode<P,  ?>, P extends AdapterNode<C, G>, C extends  AdapterNode<?, P>, GVH extends SuperViewHolder<G>, PVH extends  SuperViewHolder<P>, CHV extends SuperViewHolder<C>> extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements  SuperViewHolder.OnItemExpandListener{

    private static final String TAG  = "SuperAdapter";

    private List<ExpandableWrapper> mInflateItems = new ArrayList<>();
    private List<ExpandableWrapper<G>> mGrandWrappers = new ArrayList<>();

    private static final int GRAND_TYPE = 0;
    private static final int PARENT_TYPE = 1;
    private static final int CHILDREN_TYPE = 2;

    public SuperAdapter(List<G> gs){
        generanteNodeList(gs);
    }


    /**
     * 这里将树结构拆开，组装成链式结构
     * @param grandParents 树的爷爷辈节点链表
     */
    @UiThread
    public void refresh(@NonNull List<G> grandParents){
        mInflateItems.clear();
        generanteNodeList(grandParents);
        notifyDataSetChanged();
    }


    private void generanteNodeList (@NonNull List<G> grandParents){
        for(G g : grandParents){
            ExpandableWrapper<G> grandWrapper = new ExpandableWrapper<>(null, g, GRAND_TYPE);
            mInflateItems.add(grandWrapper);
            if(g.getChildren() != null && !g.getChildren().isEmpty()/* && g.initExpandable() */){
                for(P p : g.getChildren()){
                    ExpandableWrapper<P> parentWrapper = new ExpandableWrapper<>(grandWrapper, p, PARENT_TYPE);
                    grandWrapper.addChildrenWrapper(parentWrapper);
                    if(grandWrapper.isExpandable()) {
                        mInflateItems.add(parentWrapper);
                        grandWrapper.setExpanded(true);
                    }
                    if(p.getChildren() != null && !p.getChildren().isEmpty() /*&& p.initExpandable()*/){
                        for(C c : p.getChildren()){
                            ExpandableWrapper<C> childWrapper = new ExpandableWrapper<>(parentWrapper, c, CHILDREN_TYPE);
                            parentWrapper.addChildrenWrapper(childWrapper);
                            if(parentWrapper.isExpandable() && grandWrapper.isExpandable()){
                                mInflateItems.add(childWrapper);
                                parentWrapper.setExpanded(true);
                            }
                        }
                    }
                }
            }
            mGrandWrappers.add(grandWrapper);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position, List<Object> payloads) {
        super.onBindViewHolder(holder, position, payloads);
    }

    /**
     * 拖动移动的时候，可以顺带改变model的位置
     * @param fromPosition 拖动起始位置
     * @param toPosition 拖动替换位置
     *  @param allowDiffrentParent 是否允许不同父亲间的转换
     */
    @UiThread
    public boolean moveWithModelsMove(int fromPosition, int toPosition, boolean allowDiffrentParent){
        if(fromPosition < 0 || fromPosition >= mInflateItems.size() || toPosition < 0 || toPosition >= mInflateItems.size()){
            Log.e(TAG, "this move position is out position");
            return false;
        }
        int type, leftType = getItemViewType(fromPosition);
        int rightType = getItemViewType(toPosition);
        //确定是同一类型，不然不给移动
        if(leftType != rightType){
            return false;
        }

        ExpandableWrapper fromWrapper = mInflateItems.get(fromPosition);
        ExpandableWrapper toWrapper = mInflateItems.get(toPosition);

        if(fromWrapper.getmParent() != null && toWrapper.getmParent() != null && fromWrapper.getmParent() != toWrapper.getmParent()){
            if(!allowDiffrentParent){
                return false;
            }
            //换爹
            int fwPosition = fromWrapper.getmParent().indexChildOf(fromWrapper);
            int twPosition = toWrapper.getmParent().indexChildOf(toWrapper);
            fromWrapper.getmParent().removeChildAt(fwPosition);
            fromWrapper.getmParent().addChildrenWrapper(fwPosition, toWrapper);
            toWrapper.getmParent().removeChildAt(twPosition);
            toWrapper.getmParent().addChildrenWrapper(twPosition, fromWrapper);
        }

        //两个item之间的差值
//        int itemCx = fromPosition - toPosition;
        boolean upToDown = fromPosition < toPosition;
        boolean fromTempExpanded = fromWrapper.isExpandable();
        boolean toTempExpanded = toWrapper.isExpandable();
//        //将两个wrapper的所有子集都设置为可关闭的
        fromWrapper.setExpandable(false);
        toWrapper.setExpandable(false);
        int fOff = notifyClossingChildren(fromPosition, fromWrapper);
        if(upToDown) toPosition -= fOff;
        int toOff = notifyClossingChildren(toPosition, toWrapper);
        if(!upToDown) fromPosition -= toOff;
        // 此时from已经为to ， to已经为from，这里要注意，要以相反的逻辑使用两组变量
        mInflateItems.remove(fromPosition);
        mInflateItems.add(fromPosition, toWrapper);
        mInflateItems.remove(toPosition);
        mInflateItems.add(toPosition, fromWrapper);
        notifyItemMoved(fromPosition, toPosition);
        fromWrapper.setExpandable(fromTempExpanded);
        toWrapper.setExpandable(toTempExpanded);
        toOff = notifyExpandChildren(toPosition, fromWrapper);
        if(!upToDown) fromPosition += toOff;
        notifyExpandChildren(fromPosition, toWrapper);
//        if()
        //如果不是同一个爹，那换爹
//        if(fromWrapper.getmParent() != null && toWrapper.getmParent() != null && toWrapper.getmParent() != fromWrapper.getmParent()){
//            ExpandableWrapper<?> tancestors = toWrapper.getAncestors();
//        }
        return  true;

    }

//    @UiThread
//    public void moveGrandWithModelsMove(int fromListPosition, int toListPosition)

    @UiThread
    private int clossingChilds(int fPosition, ExpandableWrapper<?> expandableWrapper){
       int listPosition = fPosition,   offset = 0;
        if(expandableWrapper.getChildren() == null/* || expandableWrapper.isExpandable()*/){
            return 0;
        }
            //如果自己或者祖先要求折叠，并且自己已经展开，那么执行折叠
        if((!expandableWrapper.isExpandable() || !expandableWrapper.getAncestors().isExpandable()) && expandableWrapper.isExpanded()) {
            for (int index = 0; index < expandableWrapper.getChildren().size(); index++) {
                ExpandableWrapper p = expandableWrapper.getChildren().get(index);
//                listPosition ++;
                offset++;
                mInflateItems.remove(listPosition + 1);
                int count = clossingChilds(listPosition, p);
//                listPosition -= count;
                offset += count;

//                listPosition --;
            }
            expandableWrapper.setExpanded(false);
        }
        return offset;

    }

    @UiThread
    private int notifyClossingChildren(int listPosition, ExpandableWrapper<?> expandableWrapper){
        int position = listPosition + 1;
        int offset = clossingChilds(listPosition, expandableWrapper);
        notifyItemRangeRemoved(position, offset);
        return offset;
    }


    private int expandableChildren(int listPosition, ExpandableWrapper<?> expandableWrapper){
        if(expandableWrapper.getChildren() == null){
            return 0;
        }
        int expandPosition = listPosition, offset = 0;
        //如果自己要求展开并且还没有展开，那么执行展开
        if(expandableWrapper.isExpandable() && !expandableWrapper.isExpanded() && expandableWrapper.getAncestors().isExpandable()) {
            for (int index = 0; index < expandableWrapper.getChildren().size(); index++) {
                ExpandableWrapper p = expandableWrapper.getChildren().get(index);
                int count = expandableChildren(expandPosition, p);
                expandPosition++;
                mInflateItems.add(expandPosition, p);
                expandPosition += count;
                offset += count;
                offset++;
            }
            expandableWrapper.setExpanded(true);
        }
        return offset;
    }

    @UiThread
    private int notifyExpandChildren(int listPosition, ExpandableWrapper<?> expandableWrapper){
        int count = expandableChildren(listPosition, expandableWrapper);
        notifyItemRangeInserted(listPosition + 1, count);
        return count;
    }

    /**
     * 插入新的爷爷节点
     * @param g 爷爷节点
     * @param grandPosition 插入到哪个节点之后
     */
    public boolean  notifyInsert(G g, int grandPosition){
        if(grandPosition < 0 || grandPosition > mGrandWrappers.size()){
            throw  new IndexOutOfBoundsException("this grandPosition is out of wrappers size");
        }
        ExpandableWrapper<G> wrapper = new ExpandableWrapper<>(null, g, GRAND_TYPE);
        ExpandableWrapper<G> insertWrapper = mGrandWrappers.get(grandPosition);
        int insertPosition = findListPosition(insertWrapper);
        if(insertPosition < 0){
            insertPosition = mInflateItems.size();
        }else{
            insertPosition += 1;
        }

        if(g.getChildren() != null && g.getChildren().size() > 0){
            for(P p : g.getChildren()){
                ExpandableWrapper<P> pw = new ExpandableWrapper<>(wrapper, p, PARENT_TYPE);
                wrapper.addChildrenWrapper(pw);
                if(p.getChildren() != null && p.getChildren().size() > 0){
                    for(C c : p.getChildren()){
                        ExpandableWrapper<C> cw = new ExpandableWrapper<>(pw, c, CHILDREN_TYPE);
                        pw.addChildrenWrapper(cw);
                    }
                }
            }
        }
        mGrandWrappers.add(grandPosition + 1, wrapper);
        mInflateItems.add(insertPosition, wrapper);
        int insertCount = expandableChildren(insertPosition, wrapper);
        notifyItemRangeInserted(insertPosition, insertCount + 1);
        return true;
    }

    /**
     * 插入新的父亲节点
     * @param p
     * @param grandPosition
     * @param parentPosition
     */
    public boolean notifyInsert(P p, int grandPosition, int parentPosition){
        if(grandPosition < 0 || grandPosition >= mGrandWrappers.size()){
            throw  new IndexOutOfBoundsException("this grandPosition is out of wrappers size");
        }
        ExpandableWrapper<G> gWrapper = mGrandWrappers.get(grandPosition);
//        List<ExpandableWrapper<G, P, ?>> pWrappers = gWrapper.getChildren();
        int size = gWrapper.getChildrenCount();
        if(parentPosition < 0  || parentPosition >= size){
            throw  new IndexOutOfBoundsException("this parentPosition is out of parent wrappers size");
        }

        ExpandableWrapper<P> addParentWrapper = new ExpandableWrapper<>(gWrapper, p, PARENT_TYPE);
        if(p.getChildren() != null && p.getChildren().size() > 0){
            for(C c : p.getChildren()){
                ExpandableWrapper<C> cw = new ExpandableWrapper<>(addParentWrapper, c, CHILDREN_TYPE);
                addParentWrapper.addChildrenWrapper(cw);
            }
        }
        gWrapper.addChildrenWrapper(parentPosition + 1, addParentWrapper);

        if(gWrapper.isExpanded()){
            ExpandableWrapper<?> findParentWrapper = gWrapper.getChildAt(parentPosition);
            int findPosition = findListPosition(findParentWrapper);
            int insertOff;
            if(findPosition < 0 ){
                insertOff = size;
            }else{
                insertOff =  findPosition + 1;
            }
            mInflateItems.add(insertOff, addParentWrapper);
            int count = expandableChildren(findPosition, addParentWrapper);
            notifyItemRangeInserted(insertOff, count + 1);
        }
        return true;

    }

    /**
     * 插入新的孩子节点
     * @param c 孩子
     * @param grandPosition 爷爷节点
     * @param parentPosition 爹节点
     * @param childPosition 儿子节点
     */
    public boolean notifyInsert(C c, int grandPosition, int parentPosition, int childPosition){
        if(grandPosition < 0 || grandPosition >= mGrandWrappers.size()){
            throw new IndexOutOfBoundsException("this grandPosition is out of grand wrappers size");
        }
        ExpandableWrapper<G> grandWrapper = mGrandWrappers.get(grandPosition);
        if(parentPosition < 0 || parentPosition >= grandWrapper.getChildrenCount()){
            throw  new IndexOutOfBoundsException("this parentPosition is out of parent wrappers size");
        }
        ExpandableWrapper<P> parentWrapper = (ExpandableWrapper<P>) grandWrapper.getChildAt(parentPosition);
        if(childPosition < 0 || childPosition >= parentWrapper.getChildrenCount()){
            throw  new IndexOutOfBoundsException("this childPosition is out of child wrappers size");
        }
        ExpandableWrapper<C> inserChildWrapper = new ExpandableWrapper<>(parentWrapper, c, CHILDREN_TYPE);
        parentWrapper.addChildrenWrapper(childPosition + 1, inserChildWrapper);
        if(parentWrapper.isExpanded()){
            ExpandableWrapper<?> findWrapper = parentWrapper.getChildAt(childPosition);
            int findPosition = findListPosition(findWrapper);
            int inserOff;
            if(findPosition < 0){
                inserOff = parentWrapper.getChildrenCount();
            }else{
                inserOff = findPosition + 1;
            }
            mInflateItems.add(inserOff, inserChildWrapper);
            int count = expandableChildren(findPosition, inserChildWrapper);
            notifyItemRangeInserted(inserOff, count + 1);
        }

        return true;

    }

    private void notifyInsert(List<G> gs, int grandPosition){
        if(grandPosition < 0 || grandPosition > mGrandWrappers.size()){
            throw  new IndexOutOfBoundsException("this grandPosition is out of wrappers size");
        }
        List<ExpandableWrapper<G>> gWrappers = new ArrayList<>();
        for(G g: gs){
            ExpandableWrapper<G> wrapper = new ExpandableWrapper<>(null, g, GRAND_TYPE);
            gWrappers.add(wrapper);
        }
        ExpandableWrapper<G> insertWrapper = mGrandWrappers.get(grandPosition);
        int insertPosition = findListPosition(insertWrapper);
        if(insertPosition < 0){
            insertPosition = mInflateItems.size();
        }else{
            insertPosition += 1;
        }
        mInflateItems.addAll(insertPosition, gWrappers);
        mGrandWrappers.addAll(gWrappers);
        int count = gWrappers.size();
        int insertOff = insertPosition;
        for(ExpandableWrapper g : gWrappers){
            count += expandableChildren(insertOff, g);
            insertOff += count;
        }
        notifyItemRangeInserted(insertPosition, count);
    }

    private void notifyInsert(List<P> ps, int grandPosition, int parentPosition){
        if(grandPosition < 0 || grandPosition >= mGrandWrappers.size()){
            throw  new IndexOutOfBoundsException("this grandPosition is out of wrappers size");
        }
        ExpandableWrapper<G> gWrapper = mGrandWrappers.get(grandPosition);
        int size = gWrapper.getChildrenCount();
        if(parentPosition < 0  || parentPosition >= size){
            throw  new IndexOutOfBoundsException("this parentPosition is out of parent wrappers size");
        }

        List<ExpandableWrapper<?>> addParentWrappers = new ArrayList<>();
        for(P p : ps){
            ExpandableWrapper<P> addParentWrapper = new ExpandableWrapper<>(gWrapper, p, PARENT_TYPE);
            addParentWrappers.add(addParentWrapper);
        }

        gWrapper.addChildrenWrappers(parentPosition + 1, addParentWrappers);
        if(gWrapper.isExpandable()){
            ExpandableWrapper<?> findParentWrapper = gWrapper.getChildAt(grandPosition);
            int findPosition = findListPosition(findParentWrapper);
            int insertPosition = 0;
            if(findPosition < 0 ){
                insertPosition = size;
            }else{
                insertPosition += 1;
            }
            mInflateItems.addAll(insertPosition, addParentWrappers);
            int offset = insertPosition, count = addParentWrappers.size();
            for(ExpandableWrapper p : addParentWrappers){
                count += expandableChildren(offset, p);
                offset += count;
            }
            notifyItemRangeInserted(insertPosition, count);
        }

    }

    private void notifyInsert(List<C> cs, int grandPosition, int parentPosition, int childPosition){
        if(grandPosition < 0 || grandPosition >= mGrandWrappers.size()){
            throw new IndexOutOfBoundsException("this grandPosition is out of grand wrappers size");
        }
        ExpandableWrapper<G> grandWrapper = mGrandWrappers.get(grandPosition);
        if(parentPosition < 0 || parentPosition >= grandWrapper.getChildrenCount()){
            throw  new IndexOutOfBoundsException("this parentPosition is out of parent wrappers size");
        }
        ExpandableWrapper<P> parentWrapper = (ExpandableWrapper<P>) grandWrapper.getChildAt(parentPosition);
        if(childPosition < 0 || childPosition >= parentWrapper.getChildrenCount()){
            throw  new IndexOutOfBoundsException("this childPosition is out of child wrappers size");
        }

        List<ExpandableWrapper<?>> insetChildWrappers = new ArrayList<>();
        for(C c : cs){
            ExpandableWrapper<C> inserChildWrapper = new ExpandableWrapper<>((ExpandableWrapper<P>) parentWrapper, c, CHILDREN_TYPE);
            insetChildWrappers.add(inserChildWrapper);
        }

        parentWrapper.addChildrenWrappers(childPosition + 1, insetChildWrappers);
        if(parentWrapper.isExpandable()){
            ExpandableWrapper<?> findWrapper = parentWrapper.getChildAt(childPosition);
            int findPosition = findListPosition(findWrapper);
            int insertPosition = 0;
            if(findPosition < 0){
                insertPosition = parentWrapper.getChildrenCount();
            }else{
                insertPosition = findPosition + 1;
            }
            mInflateItems.addAll(insertPosition, insetChildWrappers);
        }

    }

    /**
     * 删除孩子节点
     * @param grandPosition
     * @param parentPosition
     * @param childPosition
     */
    public boolean notifyRemove(int grandPosition, int parentPosition, int childPosition){
        if(grandPosition < 0 || grandPosition >= mGrandWrappers.size()){
            return false;
        }

        ExpandableWrapper g = mGrandWrappers.get(grandPosition);
        if(parentPosition < 0 || parentPosition >= g.getChildrenCount()){
            return false;
        }
        ExpandableWrapper p = g.getChildAt(parentPosition);

        if(childPosition < 0 || childPosition >= p.getChildrenCount()){
            return false;
        }

        ExpandableWrapper c = p.getChildAt(childPosition);

        c.setExpandable(false);
        int listPosition = findListPosition(c);
        if(listPosition < 0){
            return false;
        }
        mInflateItems.remove(listPosition);
        notifyItemRangeRemoved(listPosition, 1);
        return  true;

    }

    /**
     * 删除父亲节点
     * @param grandPosition 爷爷节点在何处
     * @param parentPosition 查找到父亲节点
     */
    public boolean notifyRemove(int grandPosition, int parentPosition){
        if(grandPosition < 0 || grandPosition >= mGrandWrappers.size()){
            return false;
        }
        ExpandableWrapper g = mGrandWrappers.get(grandPosition);
        if(parentPosition < 0 || parentPosition >= g.getChildrenCount()){
            return false;
        }
        ExpandableWrapper p = g.getChildAt(parentPosition);
        int listPosition = findListPosition(p);
        if(listPosition < 0){
            return false;
        }

        mInflateItems.remove(listPosition);
        p.setExpandable(false);
        int count = clossingChilds(listPosition, p) + 1;
        g.removeChildAt(parentPosition);
         notifyItemRangeRemoved(listPosition, count);
        return  true;

    }

    /**
     * 删除爷爷节点
     * @param grandPosition
     * @return
     */
    public boolean notifyRemove(int grandPosition){
        if(grandPosition < 0 || grandPosition >= mGrandWrappers.size()){
            return false;
        }
        ExpandableWrapper g = mGrandWrappers.get(grandPosition);
        int listPosition = findListPosition(g);
        if(listPosition < 0){
            return false;
        }

        g.setExpandable(false);
        List<ExpandableWrapper> ps = g.getChildren();
        for(ExpandableWrapper p : ps){
            p.setExpandable(false);
        }
        mGrandWrappers.remove(grandPosition);
        mInflateItems.remove(listPosition);
        int count = clossingChilds(listPosition, g) + 1;
        notifyItemRangeRemoved(listPosition, count);
        return true;
    }



    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        SuperViewHolder<?> vh = null;
        switch (viewType){
            case GRAND_TYPE:
                vh = onCreateGrandViewHolder(parent, viewType);
                break;
            case PARENT_TYPE:
                vh = onCreateParentViewHolder(parent, viewType);
                break;
            case CHILDREN_TYPE:
                vh = onCreateChildViewHolder(parent, viewType);
                break;
            default:
                break;
        }
        if(vh != null){
            vh.setOnItemExpandListener(this);
        }
        return vh;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int type = getItemViewType(position);
        switch (type){
            case GRAND_TYPE:
                GVH gholder = (GVH)holder;
                ExpandableWrapper<G> gw = mInflateItems.get(position);
                int gp = findGrandPosition(gw);
                onBindGrandViewHolder(gp, position, (GVH) holder, gw);
                break;
            case PARENT_TYPE:
                PVH phodler = (PVH)holder;
                ExpandableWrapper<P> pw = mInflateItems.get(position);
                int pp = findParentPosition(pw);
                int gpp = findGrandPosition((ExpandableWrapper<G>) pw.getmParent());
                onBindParentViewHolder(gpp, pp, position, (PVH) holder, pw);
                break;
            case CHILDREN_TYPE:
                CHV cholder = (CHV)holder;
                ExpandableWrapper<C> cw = mInflateItems.get(position);
                int cp = findChildtPosition(cw);
                int cpp = findParentPosition((ExpandableWrapper<P>) cw.getmParent());
                int cgp = findGrandPosition((ExpandableWrapper<G>) cw.getmParent().getmParent());
                onBindChildViewHolder(cgp, cpp, cp, position, (CHV) holder, cw);
                break;
        }
    }



    @Override
    public int getItemCount() {
        return mInflateItems.size();
    }

    @Override
    @SuppressWarnings("unchecked")
    public int getItemViewType(int position) {
//       return mInflateItems.get(position).getmHierarchy();
        ExpandableWrapper<?> ew = mInflateItems.get(position);
        int gp , pp, cp;
        gp = pp = cp = 0;
        int type = ew.getmHierarchy();
        switch (type){
            case GRAND_TYPE:
//                ExpandableWrapper<?, G, P> gw = ew;
                gp = findGrandPosition((ExpandableWrapper<G>) ew);
                return getGrandItemViewType(gp, position, (ExpandableWrapper<G>) ew);
            case PARENT_TYPE:
                pp = findParentPosition((ExpandableWrapper<P>) ew);
                gp = findGrandPosition((ExpandableWrapper<G>) ew.getmParent());
                return getParentItemViewType(gp, pp, position, (ExpandableWrapper<P>) ew);
            case CHILDREN_TYPE:
                cp = findChildtPosition((ExpandableWrapper<C>) ew);
                pp = findParentPosition((ExpandableWrapper<P>) ew.getmParent());
                gp = findGrandPosition((ExpandableWrapper<G>) ew.getmParent().getmParent());
                return getChileItemViewType(gp, pp, cp, position, (ExpandableWrapper<C>) ew);
            default:
                return 10086;

        }
    }



    private int findGrandPosition(ExpandableWrapper<G> g){
        return mGrandWrappers.indexOf(g);
    }

    private int findParentPosition(ExpandableWrapper<P> p){
        ExpandableWrapper<?> grandWrapper = p.getmParent();
        return grandWrapper.getChildren().indexOf(p);
    }

    private int findChildtPosition(ExpandableWrapper<C> c){
       ExpandableWrapper<P> p = (ExpandableWrapper<P>) c.getmParent();
        return p.getChildren().indexOf(c);
    }

    private int findListPosition(ExpandableWrapper wrapper){
        return mInflateItems.indexOf(wrapper);
    }

    @Override
    public void onItemExpand(ExpandableWrapper e, int position) {
        notifyExpandChildren(position, e);
    }

    @Override
    public void onItemCollsing(ExpandableWrapper e, int position) {
        notifyClossingChildren(position, e);
    }

    public int getGrandItemViewType(int grandPosition, int listPosition, ExpandableWrapper<G> g){
        return GRAND_TYPE;
    }

    public int getParentItemViewType(int grandPosition, int parentPosition, int listPosition, ExpandableWrapper<P> p){
        return PARENT_TYPE;
    }

    public  int getChileItemViewType(int grandPosition, int parentPosition, int childPosition, int listPosition, ExpandableWrapper<C> c){
        return CHILDREN_TYPE;
    }





    /**
     * 抽象方法，用于创建三级item
     * @param type
     *
     */
    public abstract  GVH onCreateGrandViewHolder(ViewGroup viewParent, int type);
    public abstract  PVH onCreateParentViewHolder(ViewGroup viewParent, int type);
    public abstract  CHV onCreateChildViewHolder(ViewGroup viewParent, int type);
    public abstract  void onBindGrandViewHolder(int grandPosition, int listPosition, GVH grandHolder, ExpandableWrapper<G> g);
    public abstract  void onBindParentViewHolder(int grandPosition, int parentPosition, int listPosition, PVH parentHolder, ExpandableWrapper<P> p);
    public abstract  void onBindChildViewHolder(int grandPosition, int parentPosition, int childPosition, int listPosition, CHV childHolder, ExpandableWrapper<C> c);

}
