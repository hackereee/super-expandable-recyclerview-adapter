package ee.hacc.sea.support;

import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by hacceee - yangchundong on 2017/3/6.
 */


/**
 * 每个viewholder都带着目前需要bind的数据模型
 */
public abstract class SuperViewHolder<T extends AdapterNode<?, ?>>  extends RecyclerView.ViewHolder implements View.OnClickListener{

    private ExpandableWrapper<T> mModel;
    private OnItemExpandListener mOnItemExpandListener;


    public SuperViewHolder(View itemView) {
        super(itemView);
    }


    public void bindData(ExpandableWrapper<T> m){
        this.mModel = m;
        bindView(mModel);
        if(m.isExpandClickable()){
            itemView.setOnClickListener(this);
        }else{
            itemView.setOnClickListener(null);
        }
    }

    public abstract  void bindView(ExpandableWrapper<T> m);


    public ExpandableWrapper<T> getModel(){
        return this.mModel;
    }

    @Override
    public void onClick(View v) {
        setExpand(!mModel.isExpandable());

    }

    private void setExpand(boolean expand){
        this.mModel.setExpandable(expand);
        if(mOnItemExpandListener != null){
            if(expand){
                mOnItemExpandListener.onItemExpand(mModel, getAdapterPosition());
            }else{
                mOnItemExpandListener.onItemCollsing(mModel, getAdapterPosition());
            }
        }
    }

    public  interface  OnItemExpandListener{
        void onItemExpand(ExpandableWrapper e, int position);
        void onItemCollsing(ExpandableWrapper e, int position);
    }

    public void setOnItemExpandListener(OnItemExpandListener listener){
        this.mOnItemExpandListener = listener;
    }
}
