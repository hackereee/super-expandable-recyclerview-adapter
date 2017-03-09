package ee.hacc.sea.support;

/**
 * Created by hacceee - yangchundong on 2017/3/5.
 * adapter 的树形结构的节点，一边是孩子一边是兄弟，中间是parent
 */

import java.util.List;

/**
 *
 * @param <C> 孩子链表泛型
 * @param <P> 父母节点引用泛型
 */
public interface AdapterNode<C , P>  {

    public List<C> getChildren();
    public P getParent();

    /**
     * 默认是否折叠
     * @return false 为默认折叠， true为默认展开
     */
    public boolean initExpandable();

    /**
     * 默认是否可以点击折叠活开启
     * @return false为默认不可点击，true为默认可点击
     */
    public boolean initExpandClickable();

//
//    /**
//     * 是否点击
//     */
//    private boolean expandable = false;
//
//    public boolean expandAble(){
//        return expandable;
//    }

}
