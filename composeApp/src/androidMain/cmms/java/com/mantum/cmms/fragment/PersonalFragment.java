package com.mantum.cmms.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.mantum.R;
import com.mantum.cmms.entity.Personal;
import com.mantum.component.Mantum;
import com.mantum.component.OnCompleteListener;
import com.mantum.component.adapter.AlphabetAdapter;

import java.util.List;

public class PersonalFragment extends Mantum.Fragment {

    public final static String KEY_TAB = "Personal";

    private OnCompleteListener onCompleteListener;

    private AlphabetAdapter<Personal> alphabet;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(com.mantum.component.R.layout.simple_layout_view,
                container, false);

        LinearLayoutManager layoutManager = new LinearLayoutManager(view.getContext());

        alphabet = new AlphabetAdapter<>(view.getContext());
        alphabet.startAdapter(view, layoutManager);
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
        return context.getString(R.string.tab_personal);
    }

    public void onRefresh(List<Personal> personal) {
        alphabet.addAll(personal);
    }
}
