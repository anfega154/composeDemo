package com.mantum.cmms.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.mantum.cmms.activity.RecorridoActivity;
import com.mantum.component.Mantum;

public class ActividadTecnicoBroadcast extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getExtras() == null) {
            return;
        }

        Bundle bundle = new Bundle();
        bundle.putLong(Mantum.KEY_ID, intent.getExtras().getLong(Mantum.KEY_ID));
        bundle.putString(Mantum.KEY_UUID, intent.getExtras().getString(Mantum.KEY_UUID));
        bundle.putString(RecorridoActivity.KEY_ESTADO, intent.getAction());

        Intent newIntent = new Intent(context, RecorridoActivity.class);
        // newIntent.setFlags(Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY | Intent.FLAG_ACTIVITY_NEW_TASK);
        newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        newIntent.putExtras(bundle);

        context.sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
        context.startActivity(newIntent);
    }
}