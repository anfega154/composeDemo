package com.mantum.component.adapter.handler;

import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;

public interface ViewGalleryAdapter<T extends ViewGalleryAdapter> extends ViewAdapterHandler<T> {

    @Nullable
    File getFile();

    @Nullable
    Uri getUri();

    @NonNull
    T getValue();
}