package com.mantum.cmms.helper;

import androidx.annotation.NonNull;

import com.mantum.cmms.entity.Adjuntos;
import com.mantum.component.service.Photo;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AdjuntoHelper {

    private final List<Photo> files;

    private AdjuntoHelper(List<Photo> files) {
        this.files = files;
    }

    public List<Photo> getFiles() {
        return files;
    }

    @NonNull
    public List<Adjuntos> getAdjuntos() {
        List<Adjuntos> results = new ArrayList<>();
        if (files == null || files.isEmpty()) {
            return results;
        }

        for (Photo file : files) {
            if (file.getPath() != null && file.exists()) {
                Adjuntos adjuntos = new Adjuntos();
                adjuntos.setIdfile(new Random().nextLong());
                adjuntos.setPath(file.getPath());
                adjuntos.setExternal(false);
                results.add(adjuntos);
            }
        }

        return results;
    }
}