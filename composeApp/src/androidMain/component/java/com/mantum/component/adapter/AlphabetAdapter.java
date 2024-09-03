package com.mantum.component.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.futuremind.recyclerviewfastscroll.SectionTitleProvider;
import com.mantum.component.Mantum;
import com.mantum.component.OnSelected;
import com.mantum.component.R;
import com.mantum.component.adapter.handler.ViewAdapter;

import java.text.Normalizer;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AlphabetAdapter<T extends ViewAdapter<T>> extends Mantum.Adapter<T, AlphabetAdapter.ViewHolder> implements SectionTitleProvider {

    @SuppressWarnings("WeakerAccess")
    protected boolean summary;

    @SuppressWarnings("WeakerAccess")
    protected final boolean showletter;

    @SuppressWarnings("WeakerAccess")
    protected boolean bMultiple = false;

    public void setbMultiple(boolean bMultiple) {
        this.bMultiple = bMultiple;
    }

    @SuppressWarnings("WeakerAccess")
    protected OnSelected<T> onSelected;

    public AlphabetAdapter(@NonNull Context context) {
        super(context);
        this.summary = true;
        this.showletter = true;
    }

    @SuppressWarnings("unused")
    public AlphabetAdapter(@NonNull Context context, boolean showletter) {
        super(context);
        this.summary = true;
        this.showletter = showletter;
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
    public AlphabetAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.alphabet_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final AlphabetAdapter.ViewHolder holder, int position) {
        final T adapter = getItemPosition(position);
        if (adapter == null) {
            return;
        }

        holder.letter.setText("");
        holder.letter.setVisibility(View.VISIBLE);
        holder.favorite.setVisibility(View.GONE);
        holder.separator.setVisibility(View.GONE);

        if (showletter) {
            int total = 0;
            String letter = getCharacter(cleanedName(adapter.getTitle()));
            holder.letter.setVisibility(View.VISIBLE);

            if (holder.getAdapterPosition() < total) {
                if (holder.getAdapterPosition() == 0) {
                    holder.letter.setVisibility(View.GONE);
                    holder.favorite.setVisibility(View.VISIBLE);
                    holder.favorite.setColorFilter(Color.parseColor(getColor()));
                }
            } else {
                ViewAdapter before = getItemPosition(holder.getAdapterPosition() - 1);
                if (holder.getAdapterPosition() == 0 || holder.getAdapterPosition() == total ||
                        (before != null && !letter.equals(getCharacter(cleanedName(before.getTitle()))))) {
                    if (holder.getAdapterPosition() > 0) {
                        holder.separator.setVisibility(View.VISIBLE);
                    }

                    holder.letter.setText(letter);
                    holder.letter.setTextColor(Color.parseColor(getColor()));
                }
            }
        } else {
            holder.letter.setVisibility(View.GONE);
        }

        holder.title.setText(adapter.getTitle());

        holder.subtitle.setText("");
        if (adapter.getSubtitle() != null) {
            holder.subtitle.setText(adapter.getSubtitle());
        }

        holder.summary.setText("");
        if (summary && adapter.getSummary() != null) {
            holder.summary.setText(adapter.getSummary());
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

        if(this.bMultiple) {
            if (super.selectedIds.contains(position)) {
                holder.container.getRootView().setBackground(new ColorDrawable(ContextCompat.getColor(context, R.color.colorPrimary_2)));
            } else {
                holder.container.getRootView().setBackground(new ColorDrawable(ContextCompat.getColor(context, android.R.color.transparent)));
            }
        }

        if (onSelected != null) {
            holder.container.setOnClickListener(
                    view -> onSelected.onClick(adapter, holder.getAdapterPosition()));

            holder.container.setOnLongClickListener(
                    view -> onSelected.onLongClick(adapter, holder.getAdapterPosition()));
        }
    }

    private Comparator<T> comparator() {
        return (v1, v2) -> cleanedName(v1.getTitle()).compareTo(cleanedName(v2.getTitle()));
    }

    public void sort() {
        if (getOriginal().isEmpty()) {
            return;
        }
        Collections.sort(getOriginal(), comparator());
    }

    public void sort(@NonNull List<T> values) {
        if (values.isEmpty()) {
            return;
        }
        Collections.sort(values, comparator());
    }

    @NonNull
    private String getCharacter(@NonNull String value) {
        value = value.toUpperCase();

        try {
            value = value.substring(0, 1);
        } catch (Exception ignored) {
        }

        return value;
    }

    @Override
    public void addAll(@NonNull List<T> values) {
        super.addAll(values);
    }

    @SuppressWarnings("unused")
    public void refresh() {
        notifyDataSetChanged();
    }

    @SuppressWarnings("unused")
    public void hiddenSummary() {
        this.summary = false;
    }

    public void setOnAction(@Nullable OnSelected<T> onSelected) {
        this.onSelected = onSelected;
    }

    public void showMessageEmpty(@NonNull View view) {
        showMessageEmpty(view, 0, 0);
    }

    public void showMessageEmpty(@NonNull View view, @StringRes int message) {
        showMessageEmpty(view, message, 0);
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

    @SuppressWarnings("WeakerAccess")
    @NonNull
    protected String cleanedName(@NonNull String value) {
        value = Normalizer.normalize(value, Normalizer.Form.NFD);
        value = value.replaceAll("[^A-Za-z0-9]", "");
        value = value.trim();
        return value;
    }

    @Override
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
