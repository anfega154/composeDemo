package com.mantum.cmms.net;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.Arrays;
import java.util.UUID;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

public class CertificateManager {

    private final static String TAG = CertificateManager.class.getSimpleName();

    private final SSLSocketFactory sslSocketFactory;

    private final X509TrustManager trustManager;

    private CertificateManager(SSLSocketFactory sslSocketFactory, X509TrustManager trustManager) {
        this.sslSocketFactory = sslSocketFactory;
        this.trustManager = trustManager;
    }

    public SSLSocketFactory getSSLSocketFactory() {
        return sslSocketFactory;
    }

    public X509TrustManager getTrustManager() {
        return trustManager;
    }

    public static class Builder {

        private Certificate server;

        private KeyStore client;

        public CertificateManager.Builder server(@NonNull InputStream inputStream) {
            try {
                CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
                server = certificateFactory.generateCertificate(inputStream);
                return this;
            } catch (Exception e) {
                Log.e(TAG, "server: ", e);
                throw new IllegalArgumentException("failed to create server certificate", e);
            }
        }

        public CertificateManager.Builder client(
                @NonNull InputStream inputStream, @NonNull String password) {
            try {
                client = KeyStore.getInstance("PKCS12");
                client.load(inputStream, password.toCharArray());
                return this;
            } catch (Exception e) {
                Log.e(TAG, "client: ", e);
                throw new IllegalArgumentException("failed to create client certificate", e);
            }
        }

        public CertificateManager build() throws Exception {
            try {
                if (server == null) {
                    throw new IllegalArgumentException("The server certificate is not defined");
                }

                if (client == null) {
                    throw new IllegalArgumentException("The client certificate is not defined");
                }

                KeyStore serverKeyStore = KeyStore.getInstance(KeyStore.getDefaultType());
                serverKeyStore.load(null, null);
                serverKeyStore.setCertificateEntry("ca", server);

                TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(
                        TrustManagerFactory.getDefaultAlgorithm());
                trustManagerFactory.init(serverKeyStore);

                KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(
                        KeyManagerFactory.getDefaultAlgorithm());
                keyManagerFactory.init(client, null);

                TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
                if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
                    throw new IllegalStateException("Unexpected default trust managers:"
                            + Arrays.toString(trustManagers));
                }

                X509TrustManager trustManager = (X509TrustManager) trustManagers[0];

                SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(keyManagerFactory.getKeyManagers(), new TrustManager[]{
                        trustManager
                }, null);

                SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
                return new CertificateManager(sslSocketFactory, trustManager);
            } catch (Exception error) {
                Log.e(TAG, "build: ", error);
                throw error;
            }
        }

        public static boolean check(@NonNull InputStream inputStream, @NonNull String password) {
            try {
                CertificateManager.Builder builder = new Builder()
                        .client(inputStream, password);

                System.out.println("I have loaded [" + builder.client.size() + "] certificates");
                return true;
            } catch (Exception e) {
                Log.e(TAG, "check: ", e);
                return false;
            }
        }

        @Nullable
        public static File copy(@NonNull Context context, @NonNull String path, @NonNull String extension) {
            return copy(context, Uri.parse(path), extension);
        }

        @Nullable
        public static File copy(@NonNull Context context, @NonNull Uri uri, @NonNull String extension) {
            try {
                ParcelFileDescriptor parcelFileDescriptor = context.getContentResolver()
                        .openFileDescriptor(uri, "r", null);

                FileInputStream fileInputStream = new FileInputStream(
                        parcelFileDescriptor.getFileDescriptor());

                String uniqueID = UUID.randomUUID().toString();

                File file = new File(context.getFilesDir(), uniqueID + extension);
                FileOutputStream outputStream = new FileOutputStream(file);

                copy(fileInputStream, outputStream);
                return file;
            } catch (Exception e) {
                Log.e(TAG, "copy: ", e);
                return null;
            }
        }

        public static String getExtension(@NonNull Context context, @NonNull Uri uri) {
            try (Cursor cursor = context.getContentResolver().query(
                    uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME);
                    String path = cursor.getString(index);
                    if (path != null) {
                        return path.substring(path.lastIndexOf(".") + 1);
                    }
                }
            }

            if (uri.getPath() != null) {
                String path = uri.getPath();
                if (path != null) {
                    return path.substring(path.lastIndexOf(".") + 1);
                }
            }

            return null;
        }

        private static void copy(FileInputStream fileInputStream, FileOutputStream fileOutputStream) {
            try {
                int len;
                byte[] buf = new byte[1024];
                while ((len = fileInputStream.read(buf)) > 0) {
                    fileOutputStream.write(buf, 0, len);
                }
            } catch (Exception e) {
                Log.e(TAG, "copy: ", e);
                throw new IllegalArgumentException("the certificate could not be copied", e);
            }
        }

        @Nullable
        public static InputStream read(@NonNull String path) {
            try {
                File file = new File(path);
                if (!file.exists()) {
                    throw new IllegalArgumentException("The file does not exists " + path);
                }

                return new FileInputStream(file);
            } catch (Exception e) {
                Log.e(TAG, "read: ", e);
                return null;
            }
        }
    }
}