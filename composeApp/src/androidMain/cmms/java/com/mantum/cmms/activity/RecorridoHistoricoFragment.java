package com.mantum.cmms.activity;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mantum.demo.R;
import com.mantum.cmms.entity.RecorridoHistorico;
import com.mantum.component.Mantum;
import com.mantum.component.OnCompleteListener;
import com.mantum.component.adapter.TimeLineAdapter;

import java.util.List;

public class RecorridoHistoricoFragment extends Mantum.Fragment {

    public final static String KEY_TAB = "Proceso";

    private TimeLineAdapter<RecorridoHistorico> timeLineAdapter;

    private OnCompleteListener onCompleteListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(com.mantum.demo.R.layout.timeline_layout_view,
                container, false);

        timeLineAdapter = new TimeLineAdapter<>(view.getContext());

        LinearLayoutManager layoutManager = new LinearLayoutManager(view.getContext());
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemViewCacheSize(20);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(timeLineAdapter);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        onCompleteListener.onComplete(KEY_TAB);
    }

    @Override
    public void onAttach(@NonNull Context context) {
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
        return context.getString(R.string.tab_historico);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        timeLineAdapter.clear();
    }

    public void onRefresh(@NonNull List<RecorridoHistorico> values) {
        timeLineAdapter.clear();
        timeLineAdapter.addAll(values);
        showMessageEmpty();
    }

    public void showMessageEmpty() {
        if (getView() == null) {
            return;
        }

        RelativeLayout relativeLayout = getView().findViewById(R.id.empty);
        relativeLayout.setVisibility(timeLineAdapter.isEmpty() ? View.VISIBLE : View.GONE);

        TextView message = getView().findViewById(R.id.message);
        message.setText(getView().getContext().getString(R.string.historicos_mensaje_vacio));
    }
}