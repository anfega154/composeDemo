package com.mantum.component.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.vipulasri.timelineview.TimelineView;
import com.mantum.component.Mantum;
import com.mantum.component.R;
import com.mantum.component.adapter.handler.ViewTimeLineAdapter;

public class TimeLineAdapter<T extends ViewTimeLineAdapter<T>> extends Mantum.Adapter<T, TimeLineAdapter.ViewHolder> {

    public TimeLineAdapter(@NonNull Context context) {
        super(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.timeline_layout, parent, false);
        return new TimeLineAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final T value = getItemPosition(position);
        if (value == null) {
            return;
        }

        holder.date.setText(value.getDate());
        holder.title.setText(value.getTitle());
        holder.message.setText(value.getMessage());

        holder.timelineView.setMarker(position == 0
                ? getDrawable(R.drawable.marker_active)
                : getDrawable(R.drawable.marker_inactive));
    }

    private Drawable getDrawable(@DrawableRes int id) {
        return Build.VERSION.SDK_INT > Build.VERSION_CODES.M
                ? context.getResources().getDrawable(id, context.getTheme())
                : ContextCompat.getDrawable(context, id);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private final AppCompatTextView date;

        private final AppCompatTextView title;

        private final AppCompatTextView message;

        private final TimelineView timelineView;

        ViewHolder(View itemView) {
            super(itemView);
            this.date = itemView.findViewById(R.id.text_timeline_date);
            this.title = itemView.findViewById(R.id.text_timeline_title);
            this.message = itemView.findViewById(R.id.text_timeline_message);
            this.timelineView = itemView.findViewById(R.id.time_marker);
        }
    }
}