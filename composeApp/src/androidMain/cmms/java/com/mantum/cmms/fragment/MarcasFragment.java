package com.mantum.cmms.fragment;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mantum.demo.R;
import com.mantum.cmms.entity.Autorizaciones;
import com.mantum.cmms.entity.Personal;
import com.mantum.component.Mantum;
import com.mantum.component.OnCompleteListener;
import com.mantum.component.adapter.GroupAdapter;

import java.util.List;

public class MarcasFragment extends Mantum.Fragment {

    public final static String KEY_TAB = "Marcas";

    private GroupAdapter<Autorizaciones, Personal> groupAdapter;

    private OnCompleteListener onCompleteListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(com.mantum.demo.R.layout.group_layout_view,
                container, false);

        groupAdapter = new GroupAdapter<>(view.getContext());
        ExpandableListView expandableListView = view.findViewById(R.id.expandableListView);
        expandableListView.setAdapter(groupAdapter);

        showMessageEmpty();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        onCompleteListener.onComplete(KEY_TAB);
    }

    @NonNull
    @Override
    public String getKey() {
        return KEY_TAB;
    }

    @NonNull
    @Override
    public String getTitle(@NonNull Context context) {
        return context.getString(R.string.marcas);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        onCompleteListener = (OnCompleteListener) context;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        groupAdapter.clear();
        showMessageEmpty();
    }

    public void onRefresh(@NonNull List<Autorizaciones> autorizaciones) {
        Log.d("MAT", "onRefresh: +"+ autorizaciones);
        groupAdapter.addAll(autorizaciones, true);
        showMessageEmpty();
    }

    public void showMessageEmpty() {
        if (getView() == null) {
            return;
        }

        RelativeLayout relativeLayout = getView().findViewById(R.id.empty);
        relativeLayout.setVisibility(groupAdapter.isEmpty() ? View.VISIBLE : View.GONE);

        TextView message = getView().findViewById(R.id.message);
        message.setText(getView().getContext().getString(R.string.autorizaciones_mensaje_vacio));
    }
}