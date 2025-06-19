package com.zuomu.smartpen.base;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewbinding.ViewBinding;

public abstract class BaseFragment<VB extends ViewBinding, VM extends ViewModel> extends Fragment {
    
    protected VB binding;
    protected VM viewModel;
    
    // 获取ViewBinding
    protected abstract VB getViewBinding(LayoutInflater inflater, ViewGroup container);
    
    // 获取ViewModel Class
    protected abstract Class<VM> getViewModelClass();
    
    // 初始化视图
    protected abstract void initViews();
    
    // 初始化数据观察者
    protected abstract void initObservers();
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = getViewBinding(inflater, container);
        return binding.getRoot();
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // 初始化ViewModel
        viewModel = new ViewModelProvider(this).get(getViewModelClass());
        
        // 初始化视图和观察者
        initViews();
        initObservers();
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    
    // 显示Toast消息
    protected void showToast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }
    
    // 显示加载对话框
    protected void showLoading() {
        // TODO: 实现加载对话框
    }
    
    // 隐藏加载对话框
    protected void hideLoading() {
        // TODO: 实现隐藏加载对话框
    }
    
    // 处理错误信息
    protected void handleError(Throwable throwable) {
        showToast(throwable.getMessage());
    }
    
    // 检查设备连接状态
    protected boolean checkDeviceConnection() {
        // TODO: 实现设备连接状态检查
        return true;
    }
    
    // 共享ViewModel数据
    protected void shareData(String key, Object value) {
        // TODO: 实现数据共享
    }
}
