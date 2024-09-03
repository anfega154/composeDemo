package com.mantum.component.service;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.util.Log;
import android.util.SparseArray;
import android.webkit.MimeTypeMap;

import com.mantum.component.adapter.handler.ViewGalleryAdapter;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Photo implements ViewGalleryAdapter<Photo>, Serializable {

    private static final String TAG = Photo.class.getSimpleName();

    private final Resource file;
    private final Uri uri;

    private boolean defaultImage;

    private Integer idCategory;

    private String description;
    private boolean external = false;

    private final transient Context context;

    public Photo(@NonNull Context context, @NonNull Uri uri) {
        this(context, new Resource(context, uri));
    }

    public Photo(@NonNull Context context, @NonNull File file, Integer idCategory) {
        this(context, file, false, idCategory, "");
    }

    public Photo(@NonNull Context context, @NonNull File file) {
        this.context = context;
        this.file = new Resource(context, file);
        this.uri = null;
        this.defaultImage = false;
        this.idCategory = extractIdCategory();
    }

    public Photo(@NonNull Context context, @NonNull Resource resource) {
        this.context = context;
        this.file = resource;
        this.defaultImage = false;
        this.uri = null;
        this.idCategory = extractIdCategory();
    }

    public Photo(
            @NonNull Context context, @NonNull File file, boolean defaultImage, Integer idCategory, String description) {
        this.context = context;
        this.file = new Resource(context, file);
        this.uri = null;
        this.defaultImage = defaultImage;
        this.idCategory = idCategory;
        this.description = description;
        if (this.idCategory != null) {
            if (!rename()) {
                Log.e(TAG, "Photo: No fue posible incluir la categoria");
            }
        }
    }

    public Photo(
            @NonNull Context context, @NonNull Uri uri, boolean defaultImage, Integer idCategory, String description) {
        this.context = context;
        this.file = null;
        this.uri = uri;
        this.defaultImage = defaultImage;
        this.idCategory = idCategory;
        this.description = description;
        if (this.idCategory != null) {
            if (!rename()) {
                Log.e(TAG, "Photo: No fue posible incluir la categoria");
            }
        }
    }

    @NonNull
    private static String normalize(String value) {
        value = Normalizer.normalize(value, Normalizer.Form.NFD);
        value = value.replaceAll("[^\\p{ASCII}]", "");
        return value;
    }

    @Nullable
    public String getName() {
        if (file != null) {
            return file.getName();
        }
        return null;
    }

    @Nullable
    public String getNaturalName() {
        if (file != null) {
            return file.getNaturalName();
        }
        return null;
    }

    @Nullable
    public String getMime() {
        String name = getName();
        if (name != null) {
            return mime(getName());
        }
        return null;
    }

    public String getPath() {
        if (file != null) {
            return file.getPath();
        }
        return null;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @NonNull
    public Context getContext() {
        return context;
    }

    public boolean isDefaultImage() {
        return defaultImage;
    }

    public void setDefaultImage(boolean defaultImage) {
        this.defaultImage = defaultImage;
    }

    @Nullable
    public Integer getIdCategory() {
        return idCategory;
    }

    public void setIdCategory(@Nullable Integer idCategory) {
        this.idCategory = idCategory;
    }

    public void setIdCategory(@Nullable Long idCategory) {
        if (idCategory == null) {
            this.idCategory = null;
            return;
        }
        this.idCategory = idCategory.intValue();
    }

    public boolean isExternal() {
        return external;
    }

    public void setExternal(boolean external) {
        this.external = external;
    }

    public boolean exists() {
        if (file != null) {
            return file.exists();
        }
        return false;
    }

    public boolean rename() {
        if (getIdCategory() == null || file.getName() == null) {
            return false;
        }

        String newName = file.getName().replace(".jpg", "");
        int index = newName.indexOf("_categoria_");

        if (index >= 0) {
            newName = newName.substring(0, index);
        }

        newName += "_categoria_" + getIdCategory();
        return file.rename(newName);
    }

    public boolean rename(String name) {
        if (file != null) {
            return file.rename(name);
        }
        return false;
    }

    @NonNull
    public static ArrayList<String> paths(@NonNull List<Photo> photos) {
        ArrayList<String> response = new ArrayList<>();
        for (Photo photo : photos) {
            if (photo != null && photo.exists()) {
                response.add(photo.getPath());
            }
        }
        return response;
    }

    @NonNull
    public static Set<String> getPathSet(@NonNull List<Photo> photos) {
        return new HashSet<>(paths(photos));
    }

    public static ArrayList<Photo> setPathSet(@NonNull Context context, @NonNull Set<String> photos) {
        ArrayList<Photo> response = new ArrayList<>();
        for (String photo : photos) {
            response.add(new Photo(context, new File(photo)));
        }
        return response;
    }

    public File getFile() {
        if (file != null) {
            return file.getFile();
        }
        return null;
    }

    @Nullable
    @Override
    public Uri getUri() {
        return this.uri;
    }

    @NonNull
    @Override
    public Photo getValue() {
        return this;
    }

    @Override
    public boolean compareTo(Photo value) {
        return false;
    }

    @Nullable
    private Integer extractIdCategory() {
        if (file == null) {
            return null;
        }

        String name = file.getName()
                .replace(".jpg", "");
        if (name.contains("_categoria_")) {
            String[] value = name.split("_categoria_", 2);
            if (value.length == 2) {
                return Integer.valueOf(value[1].trim());
            }
        }
        return null;
    }

    public static String mime(@NonNull String name) {
        name = normalize(name.replace(" ", "%20"));
        String extension = MimeTypeMap.getFileExtensionFromUrl(name);

        if (extension != null && !extension.isEmpty()) {
            return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }

        return "image/jpeg";
    }

    public Bitmap scaled(@NonNull Bitmap bitmap) {
        if (bitmap.getHeight() >= 3000 || bitmap.getWidth() >= 3000) {
            int height = (int) (bitmap.getHeight() * (512.0 / bitmap.getWidth()));
            return Bitmap.createScaledBitmap(bitmap, 512, height, true);
        }
        return bitmap;
    }

    public Bitmap modifyOrientation() throws IOException {
        ExifInterface exifInterface = new ExifInterface(getPath());
        int orientation = exifInterface.getAttributeInt(
                ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        Bitmap bitmap = BitmapFactory.decodeFile(getPath());
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotate(bitmap, 90);

            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotate(bitmap, 180);

            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotate(bitmap, 270);

            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                return flip(bitmap, true, false);

            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                return flip(bitmap, false, true);

            default:
                return bitmap;
        }
    }

    @NonNull
    public static List<Photo> factory(@NonNull Context context, @NonNull SparseArray<PhotoAdapter> photos) {
        ArrayList<Photo> results = new ArrayList<>();
        for (int i = 0; i < photos.size(); i++) {
            PhotoAdapter adapter = photos.get(i);
            if (!adapter.isExternal()) {
                File file = new File(adapter.getPath());
                if (file.exists()) {
                    Photo photo = new Photo(
                            context, file, adapter.isDefaultImage(), adapter.getIdCategory(), adapter.getDescription());
                    photo.setExternal(false);
                    results.add(photo);
                }
            } else {
                Uri uri = Uri.parse(adapter.getPath());
                Photo photo = new Photo(
                        context, uri, adapter.isDefaultImage(), adapter.getIdCategory(), adapter.getDescription());
                photo.setExternal(true);
                results.add(photo);
            }
        }
        return results;
    }

    private static Bitmap rotate(Bitmap bitmap, float degrees) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);
        return Bitmap.createBitmap(
                bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    private static Bitmap flip(Bitmap bitmap, boolean horizontal, boolean vertical) {
        Matrix matrix = new Matrix();
        matrix.preScale(horizontal ? -1 : 1, vertical ? -1 : 1);
        return Bitmap.createBitmap(
                bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }
}