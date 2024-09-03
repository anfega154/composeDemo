package com.mantum.component.adapter;

import android.content.Context;
import android.graphics.Color;

import androidx.annotation.MenuRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mantum.component.Mantum;
import com.mantum.component.OnCall;
import com.mantum.component.OnDrawable;
import com.mantum.component.OnInvoke;
import com.mantum.component.OnSelected;
import com.mantum.component.R;
import com.mantum.component.adapter.handler.ViewInformationAdapter;
import com.mantum.component.adapter.handler.ViewInformationChildrenAdapter;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class InformationAdapter<T extends ViewInformationAdapter<T, K>, K extends ViewInformationChildrenAdapter<K>>
        extends Mantum.Adapter<T, InformationAdapter.ViewHolder> {

    private Integer menu;

    private OnCall<T> onCall;

    private T selected;

    private OnDrawable<T> OnDrawable;

    private OnSelected<T> onSelected;

    private List<T> mutipleSelection;

    public InformationAdapter(@NonNull Context context) {
        super(context);
    }

    @NonNull
    @Override
    public InformationAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.information_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InformationAdapter.ViewHolder holder, int position) {
        final T value = getItemPosition(position);
        if (value == null) {
            return;
        }

        holder.title.setText(value.getTitle());

        holder.summary.setText("");
        holder.containerSummary.setVisibility(View.GONE);
        if (value.getSummary() != null) {
            holder.containerSummary.setVisibility(View.VISIBLE);
            holder.summary.setText(value.getSummary());
            if (value.getColorSummary() != null) {
                holder.summary.setTextColor(value.getColorSummary());
            } else {
                holder.summary.setTextColor(Color.DKGRAY);
            }
        }

        holder.subtitle.setText("");
        if (value.getSubtitle() != null) {
            holder.subtitle.setText(value.getSubtitle());
        }

        holder.state.setText("");
        if (value.getState() != null) {
            holder.state.setText(value.getState());
        }

        holder.description.setText("");
        if (value.getDescription() != null) {
            holder.description.setText(value.getDescription());
        }

        InformationItemAdapter<K> informationItemAdapter = new InformationItemAdapter<>(context);
        if (value.getChildren() != null) {
            informationItemAdapter.addAll(value.getChildren());
            if (onSelected != null) {
                informationItemAdapter.setOnAction(new OnSelected<K>() {

                    @Override
                    public void onClick(K temp, int position) {
                        onSelected.onClick(value, holder.getAdapterPosition());
                    }

                    @Override
                    public boolean onLongClick(K temp, int position) {
                        return onSelected.onLongClick(value, holder.getAdapterPosition());
                    }
                });
            }
        }

        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        holder.children.setLayoutManager(layoutManager);
        holder.children.setItemViewCacheSize(20);
        holder.children.setDrawingCacheEnabled(true);
        holder.children.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        holder.children.setHasFixedSize(true);
        holder.children.setAdapter(informationItemAdapter);

        holder.more.setVisibility(View.GONE);
        if (menu != null) {
            holder.more.setVisibility(View.VISIBLE);
            holder.more.setOnClickListener(v -> {
                PopupMenu popupMenu = new PopupMenu(context, holder.more);
                popupMenu.getMenuInflater().inflate(menu, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(item -> onCall.onSelected(item, value));
                int total = popupMenu.getMenu().size();
                for (int i = 0; i < total; i++) {
                    MenuItem item = popupMenu.getMenu().getItem(i);
                    boolean visible = menuVisibility.get(item.getItemId());
                    if (visible) {
                        item.setVisible(true);
                    }
                }
                popupMenu.show();
            });
        }

        if (onSelected != null) {
            holder.container.setOnClickListener(view -> onSelected.onClick(value, holder.getAdapterPosition()));
            holder.container.setOnLongClickListener(v -> onSelected.onLongClick(value, holder.getAdapterPosition()));
        }

        holder.icon.setVisibility(View.GONE);
        if (OnDrawable != null) {
            Integer resources = OnDrawable.call(value);
            if (resources != null) {
                holder.icon.setVisibility(View.VISIBLE);
                holder.icon.setBackgroundResource(resources);
            }
        }

        holder.selected.setVisibility(View.GONE);
        if (selected != null && value.compareTo(selected)) {
            holder.selected.setVisibility(View.VISIBLE);
        }

        if (mutipleSelection != null) {
            for (T entity : mutipleSelection) {
                if (value.compareTo(entity))
                    holder.selected.setVisibility(View.VISIBLE);
            }
        }

        holder.action.setVisibility(View.GONE);
        if (value.isShowAction()) {
            holder.action.setVisibility(View.VISIBLE);
            holder.action.setText(value.getActionName());
            holder.action.setOnClickListener(v -> {
                OnInvoke<T> action = value.getAction(context);
                if (action != null) {
                    action.invoke(value);
                }
            });
        }
    }

    public void setDrawable(OnDrawable<T> OnDrawable) {
        this.OnDrawable = OnDrawable;
    }

    public void setOnCall(@NonNull OnCall<T> onCall) {
        this.onCall = onCall;
    }

    public void setMenu(@MenuRes int menu) {
        this.menu = menu;
    }

    public void setOnAction(@Nullable OnSelected<T> onSelected) {
        this.onSelected = onSelected;
    }

    public void setSelected(@Nullable T selected) {
        this.selected = selected;
    }

    public void setSelectedMultiple(List<T> mutipleSelection) {
        this.mutipleSelection = mutipleSelection;
    }

    public void sort(Comparator<T> comparator) {
        if (getOriginal().isEmpty()) {
            return;
        }
        Collections.sort(getOriginal(), comparator);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        final CardView container;

        final TextView title;

        final TextView subtitle;

        final LinearLayout containerSummary;

        final TextView summary;

        final TextView description;

        final RecyclerView children;

        final ImageButton more;

        final ImageView icon;

        final TextView state;

        final FrameLayout selected;

        final Button action;

        public ViewHolder(View itemView) {
            super(itemView);
            container = itemView.findViewById(R.id.container);
            containerSummary = itemView.findViewById(R.id.container_summary);
            title = itemView.findViewById(R.id.title);
            summary = itemView.findViewById(R.id.summary);
            subtitle = itemView.findViewById(R.id.subtitle);
            description = itemView.findViewById(R.id.description);
            children = itemView.findViewById(R.id.children);
            more = itemView.findViewById(R.id.more);
            icon = itemView.findViewById(R.id.icon);
            state = itemView.findViewById(R.id.state);
            selected = itemView.findViewById(R.id.selected);
            action = itemView.findViewById(R.id.action);
        }
    }
}