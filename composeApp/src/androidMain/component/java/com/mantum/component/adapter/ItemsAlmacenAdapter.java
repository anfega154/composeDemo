package com.mantum.component.adapter;

import android.content.Context;
import androidx.annotation.DrawableRes;
import androidx.annotation.MenuRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.futuremind.recyclerviewfastscroll.SectionTitleProvider;
import com.mantum.component.Mantum;
import com.mantum.component.OnCall;
import com.mantum.component.OnValueChange;
import com.mantum.component.R;
import com.mantum.component.adapter.handler.ViewTrasladoAdapter;

import java.text.Normalizer;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ItemsAlmacenAdapter<T extends ViewTrasladoAdapter<T>> extends Mantum.Adapter<T, ItemsAlmacenAdapter.ViewHolder>
        implements SectionTitleProvider {

    @SuppressWarnings("WeakerAccess")
    protected boolean summary;

    @SuppressWarnings("WeakerAccess")
    protected final boolean showletter;

    private OnCall<T> onCall;

    @SuppressWarnings("WeakerAccess")
    protected OnValueChange<T> onValueChange;

    @SuppressWarnings("WeakerAccess")
    protected boolean actionTransaction = true;

    protected boolean allOptions = false;

    protected boolean showOutputType = false;

    private Integer menu;

    protected boolean multipleActions = false;

    public ItemsAlmacenAdapter(@NonNull Context context) {
        super(context);
        this.summary = true;
        this.showletter = true;
    }

    @SuppressWarnings("unused")
    public ItemsAlmacenAdapter(@NonNull Context context, boolean showletter) {
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
    public ItemsAlmacenAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.items_almacen_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final ItemsAlmacenAdapter.ViewHolder holder, int position) {
        final T adapter = getItemPosition(position);
        if (adapter == null) {
            return;
        }

        holder.title.setText(adapter.getTitle());
        holder.subtitle.setText(adapter.getSubtitle());
        holder.summary.setText(adapter.getSummary());
        holder.summary2.setText(adapter.getNombeBodega());
        holder.summary3.setText(adapter.getPadreItem());

        if(adapter.getQuantity() != null) {
            holder.cantidad.setText(adapter.getQuantity().toString());
        }

        if(!actionTransaction) {
            holder.delete.setVisibility(View.GONE);
            holder.cantidad.setFocusable(false);
        }

        if(this.multipleActions) {
            holder.btn_more.setVisibility(View.VISIBLE);
            if (menu != null) {
                holder.btn_more.setOnClickListener(v -> {
                    PopupMenu popupMenu = new PopupMenu(context, holder.btn_more);
                    popupMenu.getMenuInflater().inflate(menu, popupMenu.getMenu());
                    popupMenu.setOnMenuItemClickListener(item -> onCall.onSelected(item, adapter));
                    int total = popupMenu.getMenu().size();
                    for (int i = 0; i < total; i++) {
                        MenuItem item = popupMenu.getMenu().getItem(i);
                        if (allOptions && adapter.getItemType().equals("Activo"))
                            item.setVisible(true);

                        if (showOutputType && (i == 4 || i == 1 || i == 3)) {
                            item.setVisible(true);
                        }
                    }
                    popupMenu.show();
                });
            }
        } else {
            holder.btn_delete.setVisibility(View.VISIBLE);
            holder.btn_delete.setOnClickListener(
                    view -> {
                        onValueChange.onClick(adapter, holder.getAdapterPosition());
                    });
        }

        holder.cantidad.setOnEditorActionListener(
                (v, keyCode, event) -> onValueChange.onChange(adapter, v));

        holder.cantidad.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                onValueChange.onTextChange(s.length() == 0 ? 0 : Float.parseFloat(s.toString()), holder.getAdapterPosition());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
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

    public void setOnCall(@NonNull OnCall<T> onCall) {
        this.onCall = onCall;
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

    public void setOnAction(@Nullable OnValueChange<T> onSelected) {
        this.onValueChange = onSelected;
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

    public void setMenu(@MenuRes int menu) { this.menu = menu; }

    public void setMultipleActions(boolean multipleActions) { this.multipleActions = multipleActions; }

    public void setAllOptions(boolean allOptions) { this.allOptions = allOptions; }

    public void setShowOutputType(boolean showOutputType) {
        this.showOutputType = showOutputType;
    }

    public void setActionTransaction(boolean actionTransaction) { this.actionTransaction = actionTransaction; }

    public class ViewHolder extends RecyclerView.ViewHolder {

        final LinearLayout container;
        final RelativeLayout delete;
        final TextView title;
        final TextView subtitle;
        final TextView summary;
        final EditText cantidad;
        final ImageButton btn_delete;
        final ImageButton btn_more;
        final TextView summary2;
        final TextView summary3;

        public ViewHolder(View itemView) {
            super(itemView);
            container = itemView.findViewById(R.id.container);
            delete = itemView.findViewById(R.id.delete);
            title = itemView.findViewById(R.id.title);
            subtitle = itemView.findViewById(R.id.subtitle);
            summary = itemView.findViewById(R.id.summary);
            summary2 = itemView.findViewById(R.id.summary_2);
            summary3 = itemView.findViewById(R.id.summary_3);
            cantidad = itemView.findViewById(R.id.cantidad);
            btn_delete = itemView.findViewById(R.id.btn_delete);
            btn_more = itemView.findViewById(R.id.btn_more);
        }
    }
}
