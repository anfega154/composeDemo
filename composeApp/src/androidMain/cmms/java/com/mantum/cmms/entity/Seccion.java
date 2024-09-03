package com.mantum.cmms.entity;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;
import com.mantum.component.adapter.handler.ViewGroupSelectedAdapter;

import java.util.ArrayList;
import java.util.List;

import io.realm.RealmList;
import io.realm.RealmObject;

public class Seccion extends RealmObject implements ViewGroupSelectedAdapter<Seccion, Checklist> {

    private Long id;
    private Cuenta cuenta;
    @SerializedName("nombrecorto")
    private String code;
    @SerializedName("descripcion")
    private String spanish;
    @SerializedName("descripcioningles")
    private String english;
    private RealmList<Checklist> checklist;
    private Boolean checked;

    @NonNull
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Cuenta getCuenta() {
        return cuenta;
    }

    public void setCuenta(Cuenta cuenta) {
        this.cuenta = cuenta;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getSpanish() {
        return spanish;
    }

    public void setSpanish(String spanish) {
        this.spanish = spanish;
    }

    public String getEnglish() {
        return english;
    }

    public void setEnglish(String english) {
        this.english = english;
    }

    public RealmList<Checklist> getChecklist() {
        return checklist;
    }

    public void setChecklist(RealmList<Checklist> checklist) {
        this.checklist = checklist;
    }

    public void setChecklist(List<Checklist> checklist) {
        RealmList<Checklist> realmList = new RealmList<>();
        realmList.addAll(checklist);
        this.checklist = realmList;
    }

    public Boolean getChecked() {
        return checked;
    }

    public void setChecked(Boolean checked) {
        this.checked = checked;
    }

    @NonNull
    @Override
    public String toString() {
        return "Seccion{" +
                "id=" + id +
                ", cuenta=" + cuenta +
                ", code='" + code + '\'' +
                ", spanish='" + spanish + '\'' +
                ", english='" + english + '\'' +
                ", checklist=" + checklist +
                ", checked=" + checked +
                '}';
    }

    @Override
    public Boolean isSelected() {
        return checked;
    }

    @Override
    public boolean compareTo(@NonNull Seccion value) {
        return value.getId().equals(getId());
    }

    @NonNull
    @Override
    public String getTitle() {
        return spanish;
    }

    @Nullable
    @Override
    public String getSubtitle() {
        return null;
    }

    @Nullable
    @Override
    public String getIcon() {
        return null;
    }

    @Nullable
    @Override
    public Integer getDrawable() {
        return null;
    }

    @Override
    public List<Checklist> getChildren() {
        return checklist;
    }

    @NonNull
    @Override
    public String getState() {
        return "";
    }

    @NonNull
    public static List<Seccion> convert(@NonNull List<Pregunta> pregunta) {
        List<Seccion> results = new ArrayList<>();
        for (Pregunta value : pregunta) {
            results.add(convert(value));
        }
        return results;
    }

    @NonNull
    public static Seccion convert(@NonNull Pregunta pregunta) {
        Log.e("TAG", "convert: " + pregunta );
        Seccion seccion = new Seccion();
        seccion.setId(pregunta.getId());
        seccion.setCode(pregunta.getCode());
        seccion.setEnglish(pregunta.getEnglish());
        seccion.setSpanish(pregunta.getSpanish());
        seccion.setChecked(pregunta.getChecked());
        if (pregunta.getRespuestas() != null) {
            seccion.setChecklist(Checklist.convert(pregunta.getRespuestas()));
        }
        return seccion;
    }

    public static class Pregunta {
        private Long id;
        @SerializedName("nombrecorto")
        private String code;
        @SerializedName("descripcion")
        private String spanish;
        @SerializedName("descripcioningles")
        private String english;
        private Boolean checked;
        private List<Checklist.Respuesta> respuestas;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getSpanish() {
            return spanish;
        }

        public void setSpanish(String spanish) {
            this.spanish = spanish;
        }

        public String getEnglish() {
            return english;
        }

        public void setEnglish(String english) {
            this.english = english;
        }

        public Boolean getChecked() {
            return checked;
        }

        public void setChecked(Boolean checked) {
            this.checked = checked;
        }

        public List<Checklist.Respuesta> getRespuestas() {
            return respuestas;
        }

        public void setRespuestas(List<Checklist.Respuesta> respuestas) {
            this.respuestas = respuestas;
        }

        public boolean isValid() {
            boolean valid = true;
            for (Checklist.Respuesta respuesta : getRespuestas()) {
                if (respuesta.getChecked() == null) {
                    valid = false;
                    break;
                }
            }

            return valid;
        }

        @NonNull
        @Override
        public String toString() {
            return "Pregunta{" +
                    "id=" + id +
                    ", code='" + code + '\'' +
                    ", spanish='" + spanish + '\'' +
                    ", english='" + english + '\'' +
                    ", checked=" + checked +
                    ", respuestas=" + respuestas +
                    '}';
        }
    }
}
