package com.mantum.cmms.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.mantum.demo.R;
import com.mantum.cmms.adapter.handler.BusquedaHandler;
import com.mantum.component.Mantum;
import com.mantum.component.OnSelected;

import java.util.List;

public class BusquedaAlphabetAdapter<T extends BusquedaHandler<T>> extends Mantum.Adapter<T, BusquedaAlphabetAdapter.ViewHolder>{

    protected OnSelected<T> onSelected;

    public BusquedaAlphabetAdapter(@NonNull Context context) {
        super(context);
    }

    @Nullable
    @Override
    public T getItemPosition(int position) {
        return super.getItemPosition(position);
    }

    @Override
    public void setSelectedIds(List<Integer> selectedIds) {
        super.setSelectedIds(selectedIds);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.busqueda_alphabet_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final T adapter = getItemPosition(position);
        if (adapter == null) {
            return;
        }

        holder.letter.setText("");
        holder.favorite.setVisibility(View.GONE);
        holder.separator.setVisibility(View.GONE);

        int total = 0;
        String letter = adapter.getCompleteType();

        if (holder.getAdapterPosition() < total) {
            if (holder.getAdapterPosition() == 0) {
                holder.letter.setVisibility(View.GONE);
                holder.favorite.setVisibility(View.VISIBLE);
                holder.favorite.setColorFilter(Color.parseColor(getColor()));
            }
        } else {
            BusquedaHandler<T> before = getItemPosition(holder.getAdapterPosition() - 1);
            if (holder.getAdapterPosition() == 0 || holder.getAdapterPosition() == total ||
                    (before != null && !letter.equals(before.getCompleteType()))) {
                holder.separator.setVisibility(View.VISIBLE);
                holder.letter.setVisibility(View.VISIBLE);
                holder.letter.setText(letter);
                holder.letter.setTextColor(Color.parseColor(getColor()));
            }
        }

        holder.title.setText(adapter.getTitle());

        holder.subtitle.setText("");
        if (adapter.getSubtitle() != null) {
            holder.subtitle.setText(adapter.getSubtitle());
        }

        holder.summary.setText("");
        holder.summary.setText(adapter.getSummary());

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

    @Override
    public void addAll(@NonNull List<T> values) {
        super.addAll(values);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void refresh() {
        notifyDataSetChanged();
    }

    public void setOnAction(@Nullable OnSelected<T> onSelected) {
        this.onSelected = onSelected;
    }

    public void showMessageEmpty(@NonNull View view) {
        showMessageEmpty(view, 0, 0);
    }

    public void showMessageEmpty(@NonNull View view, @StringRes int message, @DrawableRes int icon) {
        RelativeLayout empty = view.findViewById(R.id.empty);
        if (empty != null) {
            empty.setVisibility(isEmpty() ? View.VISIBLE : View.GONE);
        }

        if (isEmpty()) {
            TextView container = view.findViewById(R.id.message);
            if (message != 0) {
                container.setText(view.getContext().getString(message));
            }

            if (icon != 0) {
                container.setCompoundDrawablesWithIntrinsicBounds(0, icon, 0, 0);
            }
        }
    }

    public String getSectionTitle(int position) {
        T value = getItemPosition(position);
        return value != null ? value.getTitle().substring(0, 1) : null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        final LinearLayout container;

        final TextView letter;

        final ImageView favorite;

        final TextView title;

        final TextView subtitle;

        final TextView summary;

        final View separator;

        final ImageView icon;

        public ViewHolder(View itemView) {
            super(itemView);
            container = itemView.findViewById(R.id.container);
            letter = itemView.findViewById(R.id.letter);
            favorite = itemView.findViewById(R.id.favorite);
            title = itemView.findViewById(R.id.title);
            subtitle = itemView.findViewById(R.id.subtitle);
            summary = itemView.findViewById(R.id.summary);
            separator = itemView.findViewById(R.id.separator);
            icon = itemView.findViewById(R.id.icon);
        }
    }
}
