package com.mantum.cmms.helper;

import com.google.gson.annotations.SerializedName;
import com.mantum.cmms.entity.RecursoAdicional;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class RecursoAdicionalHelper implements Serializable {

    @SerializedName("recursosadicionales")
    private List<RecursoAdicional> recursosadicionales;

    public RecursoAdicionalHelper() {
        this.recursosadicionales = new ArrayList<>();
    }

    public List<RecursoAdicional> getRecursos() {
        return recursosadicionales;
    }

    public void setRecursos(List<RecursoAdicional> recursos) {
        this.recursosadicionales = recursos;
    }

    private void setRecursoAdicional(List<RecursoAdicional> value) {
        recursosadicionales = value;
    }

    public static RecursoAdicionalHelper adapter(List<RecursoAdicional> value) {
        if (value == null) {
            value = new ArrayList<>();
        }

        RecursoAdicionalHelper recursoAdicionalHelper = new RecursoAdicionalHelper();
        recursoAdicionalHelper.setRecursoAdicional(value);
        return recursoAdicionalHelper;
    }
}