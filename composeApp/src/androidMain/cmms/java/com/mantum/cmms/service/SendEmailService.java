package com.mantum.cmms.service;

import static com.mantum.cmms.security.Security.TAG;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.StrictMode;
import android.util.Log;

import androidx.core.content.FileProvider;

import com.mantum.demo.R;
import com.mantum.cmms.entity.Cuenta;
import com.mantum.cmms.entity.Transaccion;
import com.mantum.component.Mantum;
import com.mantum.core.util.Storage;

import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

public class SendEmailService {

    public static String name;

    public static void shareTransactionDetail(Context context, Cuenta cuenta, Transaccion value) {
        try {
            JSONObject jsonObject = new JSONObject();
            WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            jsonObject.put("url", value.getUrl());
            jsonObject.put("versionApp", Mantum.versionName(context));
            jsonObject.put("versionTransaccion", value.getVersion());
            jsonObject.put("user", cuenta.getUsername());
            jsonObject.put("hash", cuenta.getPassword());
            jsonObject.put("wifi", wifiManager.getConnectionInfo().getSSID());
            jsonObject.put("dispositivo", Build.MODEL);

            String body = "Enviado:"
                    + Mantum.toPrettyFormat(value.getValue())
                    + "\n\nRespuesta:"
                    + (value.getRespuesta() != null ? Mantum.toPrettyFormat(value.getRespuesta()) : "{ }")
                    + "\n\nAPP Data:"
                    + Mantum.toPrettyFormat(jsonObject.toString())
                    + "\n\nFecha creación: "
                    + value.getCreation()
                    + "\nFecha envío: "
                    + value.getSend()
                    + "\nFecha respuesta: "
                    + value.getFecharespuesta()
                    + "\nEstado: "
                    + value.getEstado();

            name = String.format("%s_%s_%s_%s.json", "transaction", value.getModulo(), value.getAccion(), System.currentTimeMillis());

            File file;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                File folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "mantum/transacciones");
                if (!folder.exists()) {
                    folder.mkdirs();
                }

                file = new File(folder, name);
            } else {
                String path = String.format("%s/%s", Storage.path(context), name);
                file = new File(path);
            }

            FileWriter fileWriter;
            BufferedWriter bufferedWriter;

            if (file.createNewFile()) {
                fileWriter = new FileWriter(file.getAbsoluteFile());
                bufferedWriter = new BufferedWriter(fileWriter);
                bufferedWriter.write(body);
                bufferedWriter.close();
            }

            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());

            String[] aEmailList = { "" };

            Intent emailIntent = new Intent(Intent.ACTION_SEND);
            emailIntent.setType("text/plain");
            emailIntent.putExtra(Intent.EXTRA_EMAIL, aEmailList);
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.asunto_correo_detalle) + (value.getMessage() != null ? value.getMessage() : ""));
            emailIntent.putExtra(Intent.EXTRA_TEXT, context.getString(R.string.cuerpo_correo_detalle));
            emailIntent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", file));

            context.startActivity(Intent.createChooser(emailIntent, "Compartir a través de:"));
        } catch (Exception e) {
            Log.e(TAG, "shareTransactionDetail: ", e);
        }
    }
}
