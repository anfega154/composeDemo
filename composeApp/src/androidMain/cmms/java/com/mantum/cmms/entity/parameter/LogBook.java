package com.mantum.cmms.entity.parameter;

import com.mantum.cmms.entity.TipoTiempo;
import com.mantum.cmms.entity.Cuenta;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class LogBook extends RealmObject {

    @PrimaryKey
    private String UUID;

    private Cuenta cuenta;

    private RealmList<EventType> eventtype;

    private RealmList<TipoTiempo> tiempos;

    private boolean turnosmanualesbitacora;

    public LogBook() {
        this.eventtype = new RealmList<>();
        this.tiempos = new RealmList<>();
        this.turnosmanualesbitacora = false;
    }

    public String getUUID() {
        return UUID;
    }

    public void setUUID(String UUID) {
        this.UUID = UUID;
    }

    public Cuenta getCuenta() {
        return cuenta;
    }

    public void setCuenta(Cuenta cuenta) {
        this.cuenta = cuenta;
    }

    public RealmList<EventType> getEventtype() {
        return eventtype;
    }

    public void setEventtype(RealmList<EventType> eventtype) {
        this.eventtype = eventtype;
    }

    public RealmList<TipoTiempo> getTiempos() {
        return tiempos;
    }

    public void setTiempos(RealmList<TipoTiempo> tiempos) {
        this.tiempos = tiempos;
    }


    public boolean isTurnosmanualesbitacora() {
        return turnosmanualesbitacora;
    }

    public void setTurnosmanualesbitacora(boolean turnosmanualesbitacora) {
        this.turnosmanualesbitacora = turnosmanualesbitacora;
    }
}