package com.mantum.component.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.mantum.component.OnSelected;
import com.mantum.demo.R;
import com.mantum.component.adapter.handler.ViewAdapter;
import com.mantum.component.adapter.handler.ViewGroupAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static java.text.Normalizer.Form.NFD;
import static java.text.Normalizer.normalize;

public class GroupAdapter<T extends ViewGroupAdapter<T, K>, K extends ViewAdapter<K>> extends BaseExpandableListAdapter {

    private final Context context;

    private final List<T> original;

    private OnSelected<K> onSelected;

    public GroupAdapter(@NonNull Context context) {
        this.context = context;
        this.original = new ArrayList<>();
    }

    public void add(@NonNull T value) {
        boolean include = true;
        int total = getGroupCount();
        for (int i = 0; i < total; i++) {
            if (original.get(i).compareTo(value)) {
                include = false;
                original.set(i, value);
                i = total;
            }
        }

        if (include) {
            original.add(value);
        }
    }

    public void addAll(@NonNull List<T> values, boolean sort) {
        if (!values.isEmpty()) {
            for (T value : values) {
                add(value);
            }
            refresh(sort);
        }
    }

    public void refresh(boolean sort) {
        if (sort && getGroupCount() > 0) {
            Collections.sort(original, sort());
        }

        notifyDataSetChanged();
    }

    public void clear() {
        original.clear();
    }

    @NonNull
    private Comparator<T> sort() {
        return (v1, v2) -> normalize(v1.getTitle(), NFD).trim().compareTo(normalize(v2.getTitle(), NFD).trim());
    }

    @Override
    public int getGroupCount() {
        return original.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return original.get(groupPosition).getChildren().size();
    }

    @Override
    public T getGroup(int groupPosition) {
        return original.get(groupPosition);
    }

    @Override
    public K getChild(int groupPosition, int childPosition) {
        return original.get(groupPosition).getChildren().get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.group_layout, null);
        }

        T value = getGroup(groupPosition);
        if (value.getIcon() != null) {
            Glide.with(context)
                    .load(value.getIcon())
                    .fitCenter()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into((ImageView) convertView.findViewById(R.id.icon));
        }

        if (value.getDrawable() != null) {
            ImageView image = convertView.findViewById(R.id.icon);
            image.setBackgroundResource(value.getDrawable());
        }

        TextView title = convertView.findViewById(R.id.title);
        title.setText(value.getTitle().trim());

        if (value.getSubtitle() != null && !value.getSubtitle().isEmpty()) {
            TextView subtitle = convertView.findViewById(R.id.subtitle);
            subtitle.setText(value.getSubtitle().trim());
        }


        TextView state = convertView.findViewById(R.id.state);
        if (value.getState() != null) {
            state.setText(value.getState().trim());
        } else {
            state.setText(null);
        }


        ImageView expand = convertView.findViewById(R.id.expand);
        expand.setVisibility(getChildrenCount(groupPosition) > 0 ? View.VISIBLE : View.GONE);
        expand.setImageResource(isExpanded ? R.drawable.expand_less : R.drawable.expand_more);

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.group_item_layout, null);
        }

        K value = getChild(groupPosition, childPosition);

        TextView title = convertView.findViewById(R.id.title);
        title.setText(value.getTitle());

        TextView subtitle = convertView.findViewById(R.id.subtitle);
        subtitle.setText(value.getSubtitle());

        TextView summary = convertView.findViewById(R.id.summary);
        if (value.getSummary() == null)
            summary.setVisibility(View.GONE);
        else
            summary.setText(value.getSummary());

        if (value.getIcon() != null) {
            ImageView imageView = convertView.findViewById(R.id.icon);
            imageView.setVisibility(View.VISIBLE);
            Glide.with(context)
                    .load(value.getIcon())
                    .fitCenter()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(imageView);
        }

        if (value.getDrawable() != null) {
            ImageView imageView = convertView.findViewById(R.id.icon);
            imageView.setVisibility(View.VISIBLE);
            imageView.setBackgroundResource(value.getDrawable());
        }

        LinearLayout body = convertView.findViewById(R.id.body);
        if (onSelected != null) {
            body.setOnClickListener(view -> onSelected.onClick(value, childPosition));
            body.setOnLongClickListener(view -> onSelected.onLongClick(value, childPosition));
        }

        return convertView;
    }

    private int setBackgoundWithState(@NonNull String state){
        switch (state) {
            case "En Operación":
                return R.color.positive_event;
            case "Parado por Mantenimiento":
            case "Parado por Fallo":
                return R.color.negative_event;
        }
        return R.color.background_gray;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    public void setOnAction(@Nullable OnSelected<K> onSelected) {
        this.onSelected = onSelected;
    }
}