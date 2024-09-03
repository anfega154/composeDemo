package com.mantum.component.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
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
import com.mantum.demo.R;
import com.mantum.component.adapter.handler.ViewInformationChildrenAdapter;

public class InformationItemAdapter<T extends ViewInformationChildrenAdapter<T>> extends Mantum.Adapter<T, InformationItemAdapter.ViewHolder> {

    private OnSelected<T> onSelected;

    public InformationItemAdapter(@NonNull Context context) {
        super(context);
    }

    @NonNull
    @Override
    public InformationItemAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.information_item_layout, parent, false);
        return new InformationItemAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        T value = getItemPosition(position);
        if (value == null) {
            return;
        }

        if (value.getFile() != null) {
            Glide.with(context)
                    .load(value.getFile())
                    .fitCenter()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.image);
        }

        if (value.getDrawable() != null) {
            Glide.with(context)
                    .load(value.getDrawable())
                    .fitCenter()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.image);
        }

        if (value.getImageColor() != null) {
            holder.image.setColorFilter(context.getResources().getColor(value.getImageColor()));
        }

        holder.title.setText(value.getName());
        if (value.getTextColor() != null) {
            holder.title.setTextColor(ContextCompat.getColor(context, value.getTextColor()));
        }

        if (onSelected != null) {
            holder.background.setOnClickListener((view) -> onSelected.onClick(value, position));
        }
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        if (holder.image != null) {
            Glide.clear(holder.image);
            holder.image.setImageDrawable(null);
        }

        if (holder.background != null) {
            holder.background.setOnClickListener(null);
        }

        super.onViewRecycled(holder);
    }

    public void setOnAction(@Nullable OnSelected<T> onSelected) {
        this.onSelected = onSelected;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public final LinearLayout background;

        public final ImageView image;

        public final TextView title;

        public ViewHolder(View itemView) {
            super(itemView);
            background = itemView.findViewById(R.id.background);
            image = itemView.findViewById(R.id.image);
            title = itemView.findViewById(R.id.title);
        }
    }
}