package ee.hacc.sea;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import junit.framework.Test;

import java.util.ArrayList;
import java.util.List;

import ee.hacc.sea.support.ExpandableWrapper;
import ee.hacc.sea.support.SuperAdapter;
import ee.hacc.sea.test.TestModel;
import ee.hacc.sea.test.TestSuperViewHolder;

/**
 * 测试类
 * @author  hacceee - yangchundong
 *
 */

public class MainActivity extends AppCompatActivity {

    private List<TestModel> grandParents = new ArrayList<>();
    RecyclerView mRecyclerView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        ItemTouchHelper itp = new ItemTouchHelper(new ItemTouchHelper.Callback() {
            @Override
            public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                if (recyclerView.getLayoutManager() instanceof LinearLayoutManager && viewHolder instanceof TestSuperViewHolder) {
                    int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN
                            | ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
                    return makeMovementFlags(dragFlags, 0);
                }
                return 0;
            }

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
//                return false;
                RecyclerView.Adapter adapter = recyclerView.getAdapter();
                if(adapter instanceof  MineAdapter){
                    return ((MineAdapter) adapter).moveWithModelsMove(viewHolder.getAdapterPosition(), target.getAdapterPosition(), true);
                }else{
                    return false;
                }
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

            }
        });
        itp.attachToRecyclerView(mRecyclerView);
        initTest();
        mRecyclerView.setAdapter(new MineAdapter(grandParents));
    }

    private  void initTest(){
        int grandCount = 4;
        for(int i = 0; i < grandCount; i++){
            TestModel grand = new TestModel("我是爷爷，第" + i + "个爷爷", null);
            if(i % 2 != 0){
                grand.setInitExpandable(true);
                grand.setClickExpandable(true);
            }else {
                grand.setClickExpandable(true);
            }
            for(int pI = 0; pI < 2; pI++){
                TestModel parent = new TestModel("我是第" + i + "个爷爷的第" + pI + "个爸爸", grand);
                if(pI % 2 == 0){
                    parent.setInitExpandable(true);
                    parent.setClickExpandable(false);
                }else{
                    parent.setClickExpandable(true);
                }
                for(int cI = 0; cI < 2; cI++){
                    TestModel child = new TestModel("我是第" + i + "个爷爷的第"  + pI + "个爸爸的第" + cI + "个儿子", parent);
                    parent.addChild(child);
                }
                grand.addChild(parent);
            }
            grandParents.add(grand);
        }
    }

    class  MineAdapter extends SuperAdapter<TestModel, TestModel, TestModel, TestSuperViewHolder, TestSuperViewHolder, TestSuperViewHolder>{


        public MineAdapter(List<TestModel> testModels) {
            super(testModels);
        }

        @Override
        public TestSuperViewHolder onCreateGrandViewHolder(ViewGroup viewParent, int type) {
            View v = LayoutInflater.from(MainActivity.this).inflate(R.layout.item_test_supervh, viewParent, false);
            return new TestSuperViewHolder(v);
        }

        @Override
        public TestSuperViewHolder onCreateParentViewHolder(ViewGroup viewParent, int type) {
            View v = LayoutInflater.from(MainActivity.this).inflate(R.layout.item_test_supervh, viewParent, false);
            return new TestSuperViewHolder(v);
        }

        @Override
        public TestSuperViewHolder onCreateChildViewHolder(ViewGroup viewParent, int type) {
            View v = LayoutInflater.from(MainActivity.this).inflate(R.layout.item_test_supervh, viewParent, false);
            return new TestSuperViewHolder(v);
        }

        @Override
        public void onBindGrandViewHolder(int grandPosition, int listPosition, TestSuperViewHolder grandHolder, ExpandableWrapper<TestModel> g) {
            grandHolder.bindData(g);
        }

        @Override
        public void onBindParentViewHolder(int grandPosition, int parentPosition, int listPosition, TestSuperViewHolder parentHolder, ExpandableWrapper<TestModel> p) {
            parentHolder.bindData(p);
        }

        @Override
        public void onBindChildViewHolder(int grandPosition, int parentPosition, int childPosition, int listPosition, TestSuperViewHolder childHolder, ExpandableWrapper<TestModel> c) {
            childHolder.bindData(c);
        }
    }
}
