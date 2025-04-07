package com.sbmatch.deviceopt.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sbmatch.deviceopt.BuildConfig;
import com.sbmatch.deviceopt.adapter.SimpleTimelineAdapter;
import com.sbmatch.deviceopt.bean.TimelineDataEmpty;
import com.sbmatch.deviceopt.databinding.FragmentAdMobBinding;

import java.util.ArrayList;
import java.util.Arrays;

public class TimeLineFragment extends Fragment {
    private static final String TAG = "TimeLineFragment";
    private FragmentAdMobBinding binding;
    private SimpleTimelineAdapter adapter;
    private ArrayList<TimelineDataEmpty> dataList;
    private LinearLayoutManager layoutManager;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentAdMobBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        requireActivity().setTitle(getArguments().getString("title_"));

        dataList = new ArrayList<>(Arrays.asList(

                new TimelineDataEmpty("NextVersion", """
                        - 新增一些未知BUG。
                        """, "null"),
                new TimelineDataEmpty("当前版本("+ BuildConfig.VERSION_NAME +")", """
                        - 新增启动页
                        - 优化版本历史时间线界面在深色模式下颜色异常的问题
                        - 新增首次启动显示隐私政策确认框
                        """, "2025-4-7"),
                new TimelineDataEmpty("2.4.8", """
                        - 修复未安装Dhizuku应用时无法进入设置的问题。
                        """, "2025-3-31"),
                new TimelineDataEmpty("2.4.6", """
                        - 优化了`no_fun`策略的描述。
                        - 新增版本更新历史页面。
                        - 移除了`Post_Notification`权限。
                        """, "2025-3-30"),
                new TimelineDataEmpty("2.4.5", """
                        - 修复一键执行模式下遇到崩溃后无法继续执行的问题。
                        """, "2025-2-25"),
                new TimelineDataEmpty("2.4.4", """
                        - 修复aab打包时丢失字符串资源的问题。
                        """, "2025-2-20"),
                new TimelineDataEmpty("2.4.3", """
                        - 修复了部分已知问题。
                        """, "2025-2-5")
        ));
        initRecyclerView();
    }


    private void initRecyclerView() {
        layoutManager = new LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false);
        binding.recyclerView.setLayoutManager(layoutManager);
        adapter = new SimpleTimelineAdapter(dataList);
        binding.recyclerView.setAdapter(adapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}