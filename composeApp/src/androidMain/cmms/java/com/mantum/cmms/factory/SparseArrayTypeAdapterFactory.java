package com.mantum.cmms.factory;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import android.util.SparseArray;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class SparseArrayTypeAdapterFactory implements TypeAdapterFactory {
    public static final SparseArrayTypeAdapterFactory INSTANCE = new SparseArrayTypeAdapterFactory();

    private SparseArrayTypeAdapterFactory() {
    }

    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        // This factory only supports (de-)serializing SparseArray
        if (type.getRawType() != SparseArray.class) {
            return null;
        }

        // Get the type argument for the element type parameter `<E>`
        // Note: Does not support raw SparseArray type (i.e. without type argument)
        Type elementType = ((ParameterizedType) type.getType()).getActualTypeArguments()[0];
        TypeAdapter<?> elementAdapter = gson.getAdapter(TypeToken.get(elementType));

        // This is safe because check at the beginning made sure type is SparseArray
        @SuppressWarnings("unchecked")
        TypeAdapter<T> adapter = (TypeAdapter<T>) new SparseArrayTypeAdapter<>(elementAdapter);
        // call nullSafe() to make adapter automatically handle `null` SparseArrays
        return adapter.nullSafe();
    }

    private static class SparseArrayTypeAdapter<E> extends TypeAdapter<SparseArray<E>> {
        private final TypeAdapter<E> elementTypeAdapter;

        public SparseArrayTypeAdapter(TypeAdapter<E> elementTypeAdapter) {
            this.elementTypeAdapter = elementTypeAdapter;
        }

        @Override
        public void write(JsonWriter out, SparseArray<E> sparseArray) throws IOException {
            out.beginObject();

            int size = sparseArray.size();
            for (int i = 0; i < size; i++) {
                out.name(Integer.toString(sparseArray.keyAt(i)));
                elementTypeAdapter.write(out, sparseArray.valueAt(i));
            }

            out.endObject();
        }

        @Override
        public SparseArray<E> read(JsonReader in) throws IOException {
            in.beginObject();

            SparseArray<E> sparseArray = new SparseArray<>();
            while (in.hasNext()) {
                int key = Integer.parseInt(in.nextName());
                E value = elementTypeAdapter.read(in);
                // Use `append(...)` here because SparseArray is serialized in ascending
                // key order so `key` will be > previously added key
                sparseArray.append(key, value);
            }

            in.endObject();
            return sparseArray;
        }

    }
}