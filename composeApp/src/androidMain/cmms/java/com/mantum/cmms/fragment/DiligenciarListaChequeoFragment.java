package com.mantum.cmms.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ScrollView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mantum.demo.R;
import com.mantum.cmms.adapter.DiligenciarAdapter;
import com.mantum.cmms.entity.ListaChequeo;
import com.mantum.component.Mantum;
import com.mantum.component.OnCompleteListener;

public class DiligenciarListaChequeoFragment extends Mantum.Fragment {

    public final static String KEY_TAB = "Diligenciar_Lista_Chequeo";

    private RecyclerView recyclerView;
    private DiligenciarAdapter diligenciarAdapter;
    private OnCompleteListener onCompleteListener;

    @Override
    @SuppressLint("ClickableViewAccessibility")
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(
                R.layout.fragment_ruta_trabajo_diligenciar, container, false);

        ScrollView scrollView = view.findViewById(R.id.scrollView);
        scrollView.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
        scrollView.setFocusable(true);
        scrollView.setFocusableInTouchMode(true);
        scrollView.setOnTouchListener((View v, MotionEvent motionEvent) -> {
            v.requestFocusFromTouch();

            if (getActivity() != null) {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
            return false;
        });

        diligenciarAdapter = new DiligenciarAdapter(view.getContext(), false);
        recyclerView = diligenciarAdapter.startAdapter(view, new LinearLayoutManager(view.getContext()));
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
        return context.getString(R.string.tab_general);
    }

    public void onStart(@NonNull ListaChequeo value) {
        if (getView() == null) {
            return;
        }

        diligenciarAdapter.addAll(value.getEntidades(), true);
    }
}
