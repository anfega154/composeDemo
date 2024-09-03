package com.mantum.component.service;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.util.SparseArray;

import androidx.annotation.NonNull;

import java.util.List;

public class PhotoAdapter implements Parcelable {

    private String path;
    private Integer idCategory;
    private String description;
    private boolean defaultImage;
    private boolean external = false;

    public PhotoAdapter() {
    }

    protected PhotoAdapter(@NonNull Parcel in) {
        path = in.readString();
        if (in.readByte() == 0) {
            idCategory = null;
        } else {
            idCategory = in.readInt();
        }
        description = in.readString();
        defaultImage = in.readByte() != 0;
        external = in.readByte() != 0;
    }

    public String getPath() {
        return path;
    }

    private void setPath(String path) {
        this.path = path;
    }

    public Integer getIdCategory() {
        return idCategory;
    }

    private void setIdCategory(Integer idCategory) {
        this.idCategory = idCategory;
    }

    public String getDescription() {
        return description;
    }

    private void setDescription(String description) {
        this.description = description;
    }

    public boolean isDefaultImage() {
        return defaultImage;
    }

    private void setDefaultImage(boolean defaultImage) {
        this.defaultImage = defaultImage;
    }

    public boolean isExternal() {
        return external;
    }

    public void setExternal(boolean external) {
        this.external = external;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(path);
        if (idCategory == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(idCategory);
        }
        dest.writeString(description);
        dest.writeByte((byte) (defaultImage ? 1 : 0));
        dest.writeByte((byte) (external ? 1 : 0));
    }

    public static final Creator<PhotoAdapter> CREATOR = new Creator<PhotoAdapter>() {
        @Override
        public PhotoAdapter createFromParcel(Parcel in) {
            return new PhotoAdapter(in);
        }

        @Override
        public PhotoAdapter[] newArray(int size) {
            return new PhotoAdapter[size];
        }
    };

    @NonNull
    public static PhotoAdapter factory(@NonNull Photo photo) {
        PhotoAdapter photoAdapter = new PhotoAdapter();
        if (photo.isExternal() && photo.getUri() != null) {
            photoAdapter.setPath(photo.getUri().toString());
        } else {
            photoAdapter.setPath(photo.getPath());
        }
        photoAdapter.setDefaultImage(photo.isDefaultImage());
        photoAdapter.setIdCategory(photo.getIdCategory());
        photoAdapter.setDescription(photo.getDescription());
        photoAdapter.setExternal(photo.isExternal());
        return photoAdapter;
    }

    @NonNull
    public static SparseArray<PhotoAdapter> factory(@NonNull List<Photo> photos) {
        SparseArray<PhotoAdapter> results = new SparseArray<>();
        for (int i = 0; i < photos.size(); i++) {
            results.append(i, PhotoAdapter.factory(photos.get(i)));
        }
        return results;
    }

    @NonNull
    @Override
    public String toString() {
        return "PhotoAdapter{" +
                "path='" + path + '\'' +
                ", idCategory=" + idCategory +
                ", description='" + description + '\'' +
                ", defaultImage=" + defaultImage +
                ", external='" + external + '\'' +
                '}';
    }
}