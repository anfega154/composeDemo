package com.mantum.cmms.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;
import com.mantum.component.adapter.handler.ViewEntityAdapter;
import com.mantum.component.adapter.handler.ViewSelectedAdapter;

import java.util.ArrayList;
import java.util.List;

import io.realm.RealmObject;

public class Checklist extends RealmObject implements ViewSelectedAdapter<Checklist>, ViewEntityAdapter<Checklist> {

    private Long id;
    @SerializedName("descripcion")
    private String spanish;
    @SerializedName("descripcioningles")
    private String english;
    private Boolean checked;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    @Override
    public Boolean isSelected() {
        return checked;
    }

    @Override
    public void setSelected(Boolean value) {
        this.checked = value;
    }

    @NonNull
    @Override
    public String getTitle() {
        return getSpanish();
    }

    @Nullable
    @Override
    public String getSubtitle() {
        return null;
    }

    @Nullable
    @Override
    public String getSummary() {
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
    public boolean compareTo(@NonNull Checklist value) {
        return value.getId().equals(getId());
    }

    @Override
    public String toString() {
        return "Checklist{" +
                "id=" + id +
                ", spanish='" + spanish + '\'' +
                ", english='" + english + '\'' +
                ", checked=" + checked +
                '}';
    }

    @NonNull
    public static List<Checklist> convert(@NonNull List<Respuesta> respuestas) {
        List<Checklist> results = new ArrayList<>();
        for (Respuesta respuesta : respuestas) {
            results.add(convert(respuesta));
        }
        return results;
    }

    @NonNull
    public static Checklist convert(@NonNull Respuesta respuesta) {
        Checklist checklist = new Checklist();
        checklist.setId(respuesta.getId());
        checklist.setSpanish(respuesta.getSpanish());
        checklist.setEnglish(respuesta.getEnglish());
        checklist.setChecked(respuesta.getChecked());
        return checklist;
    }

    public static class Respuesta {
        private Long id;
        @SerializedName("descripcion")
        private String spanish;
        @SerializedName("descripcioningles")
        private String english;
        private Boolean checked;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
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

        @NonNull
        @Override
        public String toString() {
            return "Respuesta{" +
                    "id=" + id +
                    ", spanish='" + spanish + '\'' +
                    ", english='" + english + '\'' +
                    ", checked=" + checked +
                    '}';
        }
    }
}
