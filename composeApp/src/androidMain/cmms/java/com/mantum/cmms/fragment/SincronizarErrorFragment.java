package com.mantum.cmms.fragment;

import android.content.Context;
import androidx.annotation.NonNull;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mantum.R;

public class SincronizarErrorFragment extends SincronizarFragment {

    public final static String KEY_TAB = "Sincronizar_Error";

    public SincronizarErrorFragment() {
        super(KEY_TAB);
    }

    @NonNull
    @Override
    public String getKey() {
        return KEY_TAB;
    }

    @NonNull
    @Override
    public String getTitle(@NonNull Context context) {
        return context.getString(R.string.tab_error);
    }

    @Override
    protected void showMessageEmpty() {
        if (getView() == null) {
            return;
        }

        RelativeLayout relativeLayout = getView().findViewById(R.id.empty);
        relativeLayout.setVisibility(informationAdapter.isEmpty() ? View.VISIBLE:  View.GONE);

        TextView message = getView().findViewById(R.id.message);
        message.setText(getView().getContext().getString(R.string.sincronizar_error_mensaje_vacio));
    }
}