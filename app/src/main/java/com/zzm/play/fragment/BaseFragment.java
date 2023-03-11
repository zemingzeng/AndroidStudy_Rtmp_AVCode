package com.zzm.play.fragment;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zzm.play.utils.l;

/**
 * 官方文档：http://www.android-doc.com/reference/android/support/v4/app/Fragment.html
 * 生命周期：
 * onAttach(当Fragment与Activity发生关联时调用)--->
 * onCreate(创建Fragment时被回调)-->
 * onCreateView(每次创建、绘制该Fragment的View组件时回调该方法，Fragment将会显示该方法返回的View组件)-->
 * onActivityCreated(当 Fragment 所在的Activity被启动完成后回调该方法)-->
 * onStart(启动 Fragment 时被回调，此时Fragment可见)-->
 * onResume(恢复 Fragment 时被回调，获取焦点时回调)-->
 * onPause(暂停 Fragment 时被回调，失去焦点时回调)-->
 * onStop(停止 Fragment 时被回调，Fragment不可见时回调)-->
 * onDestroyView(销毁与Fragment有关的视图，但未与Activity解除绑定)-->
 * onDestroy(销毁 Fragment 时被回调)-->
 * onDetach(与onAttach相对应，当Fragment与Activity关联被取消时调用)。
 *
 * 创建Fragment：
 * onAttach() —> onCreate() —> onCreateView() —> onActivityCreated() —> onStart() —> onResume()

 * 按下Home键回到桌面 / 锁屏：
 * onPause() —> onStop()
 *
 * 从桌面回到Fragment / 解锁
 * onStart() —> onResume()
 *
 * 切换到其他Fragment
 * onPause() —> onStop() —> onDestroyView()
 *
 * 切换回本身的Fragment
 * onCreateView() —> onActivityCreated() —> onStart() —> onResume()
 *
 * 按下Back键退出
 * onPause() —> onStop() —> onDestroyView() —> onDestroy() —> onDetach()
 */

public abstract class BaseFragment extends Fragment {


    protected abstract int getLayoutResource();

    protected View layout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        l.i(getFragmentName() + " onCreateView");
        layout = inflater.inflate(getLayoutResource(), container, false);
        init();
        return layout;
    }


    //懒加载必须view布局创建好了才加载
    private boolean isViewCreated = false;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        l.i(getFragmentName() + " onViewCreated");

        isViewCreated = true;

    }

    //当fragment可见时才加载
    private boolean isShow = false;

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        l.i(getFragmentName() + " setUserVisibleHint:" + isVisibleToUser);

        isShow = isVisibleToUser;
        lazyLoadData();
    }

    protected abstract void init();

    protected abstract void loadData();

    private void lazyLoadData() {
        if (isShow && isViewCreated) {
            loadData();

            //确保加载一次
            isShow = false;
            isViewCreated = false;
        }
    }


    /**
     * @return fragment name
     */
    protected abstract String getFragmentName();

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        l.i(getFragmentName() + " onDestroyView:");
        //isShow = false;
        //isViewCreated = false;
    }

}
