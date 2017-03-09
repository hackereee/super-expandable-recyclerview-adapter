package ee.hacc.sea.test;

import android.test.suitebuilder.TestMethod;

import junit.framework.Test;

import java.util.ArrayList;
import java.util.List;

import ee.hacc.sea.support.AdapterNode;

/**
 * Created by hacceee - yangchudnong on 2017/3/8.
 */

public class TestModel implements AdapterNode<TestModel, TestModel>{
    public String title;
    public TestModel(String title, TestModel parent){
        this.title = title;
        this.parent = parent;
    }

    TestModel parent;


    List<TestModel> children = new ArrayList<>();

    boolean  isClickExpandable;
    boolean initExpandable;

    public void setClickExpandable(boolean clickExpandable){
        this.isClickExpandable = clickExpandable;
    }

    public void setChildren(List<TestModel> children){
        this.children = children;
    }

    public void addChild(TestModel child){
        this.children.add(child);
    }

    public void setInitExpandable(boolean initExpandable){
        this.initExpandable = initExpandable;
    }




    @Override
    public List<TestModel> getChildren() {
        return children;
    }

    @Override
    public TestModel getParent() {
        return parent;
    }

    @Override
    public boolean initExpandable() {
        return initExpandable;
    }

    @Override
    public boolean initExpandClickable() {
        return isClickExpandable;
    }
}
