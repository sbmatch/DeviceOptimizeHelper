package com.sbmatch.deviceopt.adapter;

import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.provider.CalendarContract;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.vipulasri.timelineview.TimelineView;
import com.google.android.material.color.MaterialColors;
import com.sbmatch.deviceopt.AppGlobals;
import com.sbmatch.deviceopt.R;
import com.sbmatch.deviceopt.bean.TimelineDataEmpty;
import com.sbmatch.deviceopt.databinding.ItemTimelineBinding;
import com.sbmatch.deviceopt.utils.VectorDrawableUtils;

import java.util.List;

import io.noties.markwon.Markwon;

public class SimpleTimelineAdapter extends RecyclerView.Adapter<SimpleTimelineAdapter.TimelineViewHolder> {

    private final List<TimelineDataEmpty> feedList;
    private LayoutInflater layoutInflater;

    @NonNull
    @Override
    public TimelineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (layoutInflater == null) layoutInflater = LayoutInflater.from(parent.getContext());

        ItemTimelineBinding binding = ItemTimelineBinding.inflate(layoutInflater, parent, false);

        return new TimelineViewHolder(binding, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull TimelineViewHolder holder, int position) {
        TimelineDataEmpty timeLineModel = feedList.get(position);
        holder.bind(position, timeLineModel);
    }

    @Override
    public int getItemCount() {
        return feedList.size();
    }

    public SimpleTimelineAdapter(List<TimelineDataEmpty> feedList) {
        this.feedList = feedList;
    }

    @Override
    public int getItemViewType(int position) {
        return TimelineView.getTimeLineViewType(position, getItemCount());
    }

    public static class TimelineViewHolder extends RecyclerView.ViewHolder {
        private final ItemTimelineBinding binding;
        private int viewType;

        public TimelineViewHolder(ItemTimelineBinding binding, int viewType) {
            super(binding.getRoot());
            this.binding = binding;
            this.viewType = viewType;
            binding.timeline.initLine(viewType);
        }

        public void bind(int position, TimelineDataEmpty model){

            binding.textTimelineVersion.setText(model.getTitle());
            Markwon markwon = Markwon.create(binding.getRoot().getContext());
            markwon.setMarkdown(binding.textTimelineDescription, model.getDescription());
            binding.textTimelineDate.setText(model.getDate());
            binding.timeline.setEndLineColor(Color.LTGRAY, viewType);
            binding.timeline.setStartLineColor(Color.LTGRAY, viewType);
            switch (position) {
                case 0 -> {
                    binding.timeline.setMarker(VectorDrawableUtils.getDrawable(R.drawable.ic_marker_inactive), MaterialColors.getColor(binding.getRoot(), com.google.android.material.R.attr.colorOnSurface));
                    binding.timeline.setEndLineStyle(TimelineView.LineStyle.DASHED);
                }
                case 1 -> {
                    binding.timeline.setMarker(VectorDrawableUtils.getDrawable(R.drawable.ic_marker_active),MaterialColors.getColor(binding.getRoot(), com.google.android.material.R.attr.colorOnSurfaceVariant));
                    binding.timeline.setStartLineStyle(TimelineView.LineStyle.DASHED);
                    binding.timeline.setEndLineStyle(TimelineView.LineStyle.NORMAL);
                }
                default -> {
                    binding.timeline.setMarker(VectorDrawableUtils.getDrawable(R.drawable.ic_marker), MaterialColors.getColor(binding.getRoot(), com.google.android.material.R.attr.colorOnSurfaceVariant));

                    binding.timeline.setStartLineStyle(TimelineView.LineStyle.NORMAL);
                    binding.timeline.setEndLineStyle(TimelineView.LineStyle.NORMAL);
                }
            }
        }

    }
}
