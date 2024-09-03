package com.mantum.cmms.util;

import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareUltralight;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import android.util.Log;
import android.view.View;

import com.mantum.component.Mantum;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static android.content.ContentValues.TAG;

public class NFC extends Mantum.Activity {

    private static final byte[] PWD = "1234".getBytes();

    private static final byte[] PACK = "cC".getBytes();

    public void handleIntent(Intent intent, NdefMessage ndefMessage, View view) {
        super.setIntent(intent);

        if (intent != null && ndefMessage != null) {

            boolean results = push(intent.getParcelableExtra(NfcAdapter.EXTRA_TAG), ndefMessage);

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(view.getContext());
            alertDialogBuilder.setPositiveButton(com.mantum.demo.R.string.accept, (dialog, which) -> dialog.dismiss());
            alertDialogBuilder.setMessage(results ? com.mantum.demo.R.string.write_success_nfc : com.mantum.demo.R.string.write_error_nfc);
            alertDialogBuilder.setCancelable(true);
            alertDialogBuilder.show();
        }
    }

    private boolean push(Tag tag, NdefMessage ndefMessage) {
        try {
            Log.d(TAG, "push: " + tag);
            MifareUltralight mifareUltralight = MifareUltralight.get(tag);
            if (mifareUltralight == null) {
                throw new Exception("El NFC no soporta la tecnología Mifare Ultralight");
            }

            mifareUltralight.connect();
            if (!mifareUltralight.isConnected()) {
                throw new Exception("No fue posible realizar la conexión con el NFC");
            }

            boolean isLogin = login(mifareUltralight);
            if (!isLogin) {
                Log.i(TAG, "No fue posible autentiar el NFC");

                // Se cierra la conexión ya que no es posible volver a registrar el usuario
                if (mifareUltralight.isConnected()) {
                    mifareUltralight.close();
                }

                mifareUltralight.connect();
                if (!mifareUltralight.isConnected()) {
                    throw new Exception("No fue posible realizar la conexión con el NFC");
                }

                boolean isWrite = write(mifareUltralight, ndefMessage);
                if (!isWrite) {
                    throw new Exception("No fue posible escribir en el NFC");
                }
            }

            // Escribe en el NFC
            boolean isWrite = write(mifareUltralight, ndefMessage);
            if (!isWrite) {
                throw new Exception("No fue posible escribir en el NFC");
            }

            // Obtiene la página 2Ah
            byte[] response = mifareUltralight.transceive(new byte[]{
                    (byte) 0x30, // Lectura
                    (byte) 0x2A
            });

            // Configura la etiqueta como protegida contra escritura con intentos de autenticación ilimitados
            if (response != null && response.length >= 16) {
                int attempts = 0; // Número de intentos
                mifareUltralight.transceive(new byte[]{
                        (byte) 0xA2, // Escritura
                        (byte) 0x2A,
                        (byte) ((response[0] & 0x078) | (0x000) | (attempts & 0x007)),
                        0, 0, 0
                });
            }

            response = mifareUltralight.transceive(new byte[]{
                    (byte) 0x30, // Lectura
                    (byte) 0x29
            });

            // Configura la etiqueta para proteger el almacenamiento desde la página 0 y superior
            if (response != null && response.length >= 16) {
                int auth0 = 0;
                mifareUltralight.transceive(new byte[]{
                        (byte) 0xA2, // Escritura
                        (byte) 0x29,
                        response[0], 0, response[2],
                        (byte) (auth0 & 0x0ff)
                });
            }

            // Incluye el token
            mifareUltralight.transceive(new byte[]{
                    (byte) 0xA2,
                    (byte) 0x2C,
                    PACK[0], PACK[1], 0, 0
            });

            // Incluye la contraseña
            mifareUltralight.transceive(new byte[]{
                    (byte) 0xA2,
                    (byte) 0x2B,
                    PWD[0], PWD[1], PWD[2], PWD[3]
            });

            mifareUltralight.close();

            return true;
        } catch (Exception e) {
            Log.e(TAG, "push: ", e);
            return false;
        }
    }

    private boolean login(@NonNull MifareUltralight mifareUltralight) {
        try {
            if (!mifareUltralight.isConnected()) {
                throw new Exception("Error al autenticar al NFC");
            }

            mifareUltralight.transceive(new byte[]{
                    (byte) 0x1B,
                    PWD[0], PWD[1], PWD[2], PWD[3]
            });

            return true;
        } catch (Exception e) {
            Log.e(TAG, "login: ", e);
            return false;
        }
    }

    private boolean write(@NonNull MifareUltralight mifareUltralight, @NonNull NdefMessage ndefMessage) {
        try {
            StringBuilder tagText = new StringBuilder();
            NdefRecord[] records = ndefMessage.getRecords();
            for (NdefRecord ndefRecord : records) {
                if (ndefRecord.getTnf() == NdefRecord.TNF_WELL_KNOWN && Arrays.equals(ndefRecord.getType(), NdefRecord.RTD_TEXT)) {
                    byte[] payload = ndefRecord.getPayload();

                    int languageCodeLength = payload[0] & 51;
                    String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16";
                    tagText.append(new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding));
                }
            }

            int total = tagText.length();
            for (int i = 0; i < 4; i++) {
                int page = i + 4;
                byte[] empty = "    ".getBytes(StandardCharsets.UTF_8);
                mifareUltralight.writePage(page, empty);

                int beginIndex = i * 4;
                if (beginIndex > total) {
                    break;
                }

                int endIndex = (i + 1) * 4;
                if (endIndex > total) {
                    endIndex = total;
                }

                StringBuilder text = new StringBuilder(tagText.substring(beginIndex, endIndex));
                if (text.length() != 4) {
                    for (int j = text.length(); j < 4; j++) {
                        text.append(" ");
                    }
                }

                byte[] data = text.toString().getBytes(StandardCharsets.UTF_8);
                mifareUltralight.writePage(page, data);
                Log.i(TAG, "write page: " + page);
            }

            return true;
        } catch (Exception e) {
            Log.e(TAG, "write: ", e);
            return false;
        }
    }

    public String read(Intent intent) {
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

        try {
            MifareUltralight mifareUltralight = MifareUltralight.get(tag);
            if (mifareUltralight == null) {
                throw new Exception("mifareUltralight == null");
            }

            mifareUltralight.connect();
            if (!mifareUltralight.isConnected()) {
                throw new Exception("No fue posible realizar la conexión con el NFC");
            }

            byte[] response = mifareUltralight.readPages(4);
            String value = new String(response, StandardCharsets.UTF_8);

            if (value.isEmpty()) {
                throw new Exception("El valor esta vacio");
            }

            mifareUltralight.close();
            return value.trim();
        } catch (Exception e) {
            Log.e(TAG, "read: ", e);
            return null;
        }
    }
}
