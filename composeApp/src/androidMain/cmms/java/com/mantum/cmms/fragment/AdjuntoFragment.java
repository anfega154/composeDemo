package com.mantum.cmms.fragment;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mantum.R;
import com.mantum.cmms.entity.Adjuntos;
import com.mantum.component.Mantum;
import com.mantum.component.OnCompleteListener;
import com.mantum.component.OnSelected;
import com.mantum.component.adapter.SimpleAdapter;

import java.util.List;

public class AdjuntoFragment extends Mantum.Fragment {

    private final static String TAG = AdjuntoFragment.class.getSimpleName();

    public final static String KEY_TAB = "Adjuntos";

    private SimpleAdapter<Adjuntos> simpleAdapter;

    private OnCompleteListener onCompleteListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(com.mantum.component.R.layout.simple_layout_view, container, false);

        LinearLayoutManager layoutManager = new LinearLayoutManager(view.getContext());
        simpleAdapter = new SimpleAdapter<>(view.getContext());
        simpleAdapter.startAdapter(view, layoutManager);

        simpleAdapter.setOnAction(new OnSelected<Adjuntos>() {
            @Override
            public void onClick(Adjuntos value, int position) {
                try {
                    Intent implicit = new Intent(Intent.ACTION_VIEW, Uri.parse(value.getPath()));
                    startActivity(implicit);
                } catch (Exception e) {
                    Log.e(TAG, "onClick: ", e);
                }
            }

            @Override
            public boolean onLongClick(Adjuntos value, int position) {
                return false;
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        onCompleteListener.onComplete(KEY_TAB);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        onCompleteListener = (OnCompleteListener) context;
    }

    @NonNull
    @Override
    public String getKey() {
        return KEY_TAB;
    }

    @NonNull
    @Override
    public String getTitle(@NonNull Context context) {
        return context.getString(R.string.tab_adjuntos);
    }

    public void onLoad(List<Adjuntos> values) {
        if (simpleAdapter != null) {
            simpleAdapter.addAll(values);
        }

        if (getView() != null) {
            simpleAdapter.showMessageEmpty(getView());
        }
    }
}