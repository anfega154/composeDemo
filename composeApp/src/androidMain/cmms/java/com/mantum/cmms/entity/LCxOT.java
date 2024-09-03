package com.mantum.cmms.entity;

import io.realm.RealmObject;

public class LCxOT extends RealmObject {

    private Long idrt;
    private Long idot;
    private Long idgrupoam;

    public Long getIdrt() {
        return idrt;
    }

    public void setIdrt(Long idrt) {
        this.idrt = idrt;
    }

    public Long getIdot() {
        return idot;
    }

    public void setIdot(Long idot) {
        this.idot = idot;
    }

    public Long getIdgrupoam() { return idgrupoam; }

    public void setIdgrupoam(Long idgrupoam) { this.idgrupoam = idgrupoam; }

}
