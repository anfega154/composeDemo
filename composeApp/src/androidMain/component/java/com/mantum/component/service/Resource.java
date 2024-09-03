package com.mantum.component.service;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.Serializable;

public class Resource implements Serializable {

    private String path;
    private transient File file;
    private final transient Context context;

    public Resource(@NonNull Context context, @NonNull Uri uri) {
        this(context, prepare(context, uri));
    }

    public Resource(@NonNull Context context, @NonNull String pathname) {
        this.context = context;
        this.path = pathname;
        this.file = new File(pathname);
    }

    public Resource(@NonNull Context context, @NonNull File file) {
        this.context = context;
        this.file = file;
        this.path = file.getAbsolutePath();
    }

    public String getName() {
        if (file == null) {
            file = new File(path);
        }
        return file.getName();
    }

    public String getNaturalName() {
        if (file == null) {
            file = new File(path);
        }

        return file.getName()
                .replace(".jpg", "")
                .replace(" ", "_");
    }

    public String getPath() {
        return path;
    }

    public boolean exists() {
        if (file == null) {
            file = new File(path);
        }
        return file.exists();
    }

    public boolean rename(@NonNull String name) {
        if (name.isEmpty()) {
            return false;

        }
        name = name.trim();
        if (!name.contains(".jpg")) {
            name = name + ".jpg";
        }

        File to = new File(
                context.getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/" + name
        );

        if (file == null) {
            file = new File(path);
        }

        if (file.renameTo(to)) {
            this.file = to;
            this.path = to.getAbsolutePath();
            return true;
        }

        return false;
    }

    public File getFile() {
        return file;
    }

    @NonNull
    @Override
    public String toString() {
        return path;
    }

    private static String prepare(@NonNull Context context, @NonNull Uri uri) {
        if (DocumentsContract.isDocumentUri(context, uri)) {
            if (isExternalStorageDocument(uri)) {
                return getExternalStorageDocument(uri);
            }

            if (isDownloadsDocument(uri)) {
                return getDownloadsDocument(context, uri);
            }

            if (isMediaDocument(uri)) {
                return getMediaDocument(context, uri);
            }

            if (isGoogleDriveUri(uri)) {
                return getContentFilePath(context, uri);
            }
        } else if (isContentDocument(uri)) {
            if (isGooglePhotosUri(uri)) {
                return uri.getLastPathSegment();
            }

            if (isProviderGooglePhotosUri(uri)) {
                return getContentFilePath(context, uri);
            }

            if (isGoogleDriveUri(uri)) {
                return getContentFilePath(context, uri);
            }
        }

        if (uri.getPath() == null || !fileExists(uri.getPath())) {
            return getContentFilePath(context, uri);
        }

        return uri.getPath();
    }

    // https://o7planning.org/12725/create-a-simple-file-chooser-in-android
    @NonNull
    private static String getContentFilePath(@NonNull Context context, Uri uri) {
        Uri returnUri = uri;
        ContentResolver contentResolver = context.getContentResolver();
        Cursor returnCursor = contentResolver.query(returnUri, null, null, null, null);
        if (returnCursor == null) {
            return "";
        }

        // Get the column indexes of the data in the Cursor,
        // move to the first row in the Cursor, get the data,
        // and display it.

        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        returnCursor.moveToFirst();

        String name = (returnCursor.getString(nameIndex));
        File file = new File(context.getCacheDir(), name);

        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            FileOutputStream outputStream = new FileOutputStream(file);

            int read;
            int maxBufferSize = 1024 * 1024;
            int bytesAvailable = inputStream.available();

            int bufferSize = Math.min(bytesAvailable, maxBufferSize);

            final byte[] buffers = new byte[bufferSize];
            while ((read = inputStream.read(buffers)) != -1) {
                outputStream.write(buffers, 0, read);
            }

            inputStream.close();
            outputStream.close();
            returnCursor.close();

        } catch (Exception e) {
            Log.e("Resource", "Get content file path", e);
        }

        return file.getPath();
    }

    @NonNull
    private static String getMediaDocument(@NonNull Context context, @NonNull Uri uri) {
        final String docId = DocumentsContract.getDocumentId(uri);
        final String[] split = docId.split(":");
        final String type = split[0];

        Uri contentUri = null;
        if ("image".equals(type)) {
            contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        } else if ("video".equals(type)) {
            contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        } else if ("audio".equals(type)) {
            contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        } else {
            contentUri = MediaStore.Files.getContentUri("external");
        }

        final String selection = "_id=?";
        final String[] selectionArgs = new String[]{
                split[1]
        };

        String result = getDataColumn(
                context, contentUri, "_data", selection, selectionArgs
        );

        if (result == null || result.isEmpty()) {
            return getContentFilePath(context, uri);
        }

        return result;
    }

    private static String getDownloadsDocument(@NonNull Context context, @NonNull Uri uri) {
        String fileName = getDataColumn(
                context, uri, MediaStore.MediaColumns.DISPLAY_NAME, null, null);

        if (fileName != null) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                String file = Environment.getExternalStorageDirectory().toString() + "/Download/" + fileName;
                if (fileExists(file)) {
                    return file;
                }
            }
            return getContentFilePath(context, uri);
        }

        String id = DocumentsContract.getDocumentId(uri);
        if (id.startsWith("raw:")) {
            id = id.replaceFirst("raw:", "");
            File file = new File(id);
            if (file.exists()) {
                return id;
            }
        }

        Uri contentUri = ContentUris.withAppendedId(
                Uri.parse("content://downloads/public_downloads"),
                Long.parseLong(id)
        );

        return getDataColumn(
                context, contentUri, "_data", null, null
        );
    }

    @NonNull
    private static String getExternalStorageDocument(@NonNull Uri uri) {
        final String docId = DocumentsContract.getDocumentId(uri);
        final String[] split = docId.split(":");
        final String type = split[0];

        if ("primary".equalsIgnoreCase(type)) {
            if (split.length > 1) {
                return Environment.getExternalStorageDirectory() + "/" + split[1];
            } else {
                return Environment.getExternalStorageDirectory() + "/";
            }
        } else {
            return "storage" + "/" + docId.replace(":", "/");
        }
    }

    @Nullable
    public static String getDataColumn(
            Context context, Uri uri, String column, String selection, String[] selectionArgs) {
        String[] projection = new String[]{column};
        try (Cursor cursor = context.getContentResolver().query(
                uri, projection, selection, selectionArgs, null
        )) {
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        }
        return null;
    }

    private static boolean isExternalStorageDocument(@NonNull Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    private static boolean isDownloadsDocument(@NonNull Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    private static boolean isMediaDocument(@NonNull Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    private static boolean isGooglePhotosUri(@NonNull Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    private static boolean isProviderGooglePhotosUri(@NonNull Uri uri) {
        return "com.google.android.apps.photos.contentprovider".equals(uri.getAuthority());
    }

    private static boolean isContentDocument(@NonNull Uri uri) {
        return "content".equalsIgnoreCase(uri.getScheme());
    }

    private static boolean isGoogleDriveUri(@NonNull Uri uri) {
        return "com.google.android.apps.docs.storage".equals(uri.getAuthority())
                || "com.google.android.apps.docs.storage.legacy".equals(uri.getAuthority());
    }

    private static boolean fileExists(String filePath) {
        File file = new File(filePath);
        return file.exists();
    }
}