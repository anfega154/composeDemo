package com.mantum.component.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.mantum.component.Mantum;
import com.mantum.component.OnSelected;
import com.mantum.component.R;
import com.mantum.component.adapter.handler.ViewAdapter;

public class SimpleAdapter<T extends ViewAdapter<T>> extends Mantum.Adapter<T, SimpleAdapter.ViewHolder> {

    private OnSelected<T> onSelected;

    public SimpleAdapter(@NonNull Context context) {
        super(context);
    }

    @NonNull
    @Override
    public SimpleAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SimpleAdapter.ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.simple_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull SimpleAdapter.ViewHolder holder, int position) {
        final T adapter = getItemPosition(position);
        if (adapter == null) {
            return;
        }

        holder.title.setText(adapter.getTitle());

        holder.subtitle.setText("");
        if (adapter.getSubtitle() != null) {
            holder.subtitle.setText(adapter.getSubtitle());
        }

        if (adapter.getIcon() != null) {
            holder.icon.setVisibility(View.VISIBLE);
            Glide.with(context)
                    .load(adapter.getIcon())
                    .fitCenter()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.icon);
        }

        if (adapter.getDrawable() != null) {
            holder.icon.setVisibility(View.VISIBLE);
            holder.icon.setBackgroundResource(adapter.getDrawable());
        }

        if (onSelected != null) {
            holder.container.setOnClickListener(
                    view -> onSelected.onClick(adapter, holder.getAdapterPosition()));

            holder.container.setOnLongClickListener(
                    view -> onSelected.onLongClick(adapter, holder.getAdapterPosition()));
        }
    }

    public void setOnAction(@Nullable OnSelected<T> onSelected) {
        this.onSelected = onSelected;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        final LinearLayout container;

        final TextView title;

        final TextView subtitle;

        final ImageView icon;

        public ViewHolder(View itemView) {
            super(itemView);
            container = itemView.findViewById(R.id.container);
            title = itemView.findViewById(R.id.title);
            subtitle = itemView.findViewById(R.id.subtitle);
            icon = itemView.findViewById(R.id.icon);
        }
    }
}