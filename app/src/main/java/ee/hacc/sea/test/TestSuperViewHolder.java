package ee.hacc.sea.test;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import ee.hacc.sea.R;
import ee.hacc.sea.support.ExpandableWrapper;
import ee.hacc.sea.support.SuperViewHolder;

/**
 * Created by hacceee - yangchundong on 2017/3/8.
 */

public class TestSuperViewHolder extends SuperViewHolder<TestModel>{

    TextView tv = null;

    public TestSuperViewHolder(View itemView) {
        super(itemView);
        tv = (TextView) itemView.findViewById(R.id.testTv);
    }

    @Override
    public void bindView(ExpandableWrapper<TestModel> m) {
        tv.setText(m.getMine().title);
    }
}
