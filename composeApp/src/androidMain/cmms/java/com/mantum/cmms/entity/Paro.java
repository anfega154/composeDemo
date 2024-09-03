package com.mantum.cmms.entity;

import android.os.Parcel;
import android.os.Parcelable;

import com.mantum.cmms.adapter.handler.HistoricoParoHandler;
import com.mantum.cmms.adapter.handler.ParoHandler;

import io.realm.RealmObject;

public class Paro extends RealmObject implements HistoricoParoHandler<Paro> {

    private Long id;

    private Cuenta cuenta;

    private Long idequipo;

    private String equipo;

    private String fechainicio;

    private String fechafin;

    private String duracion;

    private String tipo;

    private String tipoparo;

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

    public Long getIdequipo() {
        return idequipo;
    }

    public void setIdequipo(Long idequipo) {
        this.idequipo = idequipo;
    }

    public String getEquipo() {
        return equipo;
    }

    public void setEquipo(String equipo) {
        this.equipo = equipo;
    }

    public String getFechainicio() {
        return fechainicio;
    }

    public void setFechainicio(String fechainicio) {
        this.fechainicio = fechainicio;
    }

    public String getFechafin() {
        return fechafin;
    }

    public void setFechafin(String fechafin) {
        this.fechafin = fechafin;
    }

    public String getDuracion() {
        return duracion;
    }

    public void setDuracion(String duracion) {
        this.duracion = duracion;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getTipoparo() {
        return tipoparo;
    }

    public void setTipoparo(String tipoparo) {
        this.tipoparo = tipoparo;
    }

    @Override
    public boolean compareTo(Paro value) {
        return false;
    }

    public static class ParoHelper implements Parcelable, ParoHandler<ParoHelper> {
        private String horaInicio;

        private String horaFin;

        private String clasificacion;

        private Long tipo = 0L;

        public ParoHelper() {}

        public ParoHelper(String horaInicio, String horaFin) {
            this.horaInicio = horaInicio;
            this.horaFin = horaFin;
        }

        protected ParoHelper(Parcel in) {
            horaInicio = in.readString();
            horaFin = in.readString();
            clasificacion = in.readString();
            tipo = in.readLong();
        }

        public static final Creator<ParoHelper> CREATOR = new Creator<ParoHelper>() {
            @Override
            public ParoHelper createFromParcel(Parcel in) {
                return new ParoHelper(in);
            }

            @Override
            public ParoHelper[] newArray(int size) {
                return new ParoHelper[size];
            }
        };

        @Override
        public String getHoraInicio() {
            return horaInicio;
        }

        public void setHoraInicio(String horaInicio) {
            this.horaInicio = horaInicio;
        }

        @Override
        public String getHoraFin() {
            return horaFin;
        }

        public void setHoraFin(String horaFin) {
            this.horaFin = horaFin;
        }

        @Override
        public String getClasificacion() {
            return clasificacion;
        }

        public void setClasificacion(String clasificacion) {
            this.clasificacion = clasificacion;
        }

        @Override
        public Long getTipo() {
            return tipo;
        }

        public void setTipo(Long tipo) {
            this.tipo = tipo;
        }

        @Override
        public boolean compareTo(ParoHelper value) {
            return false;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(horaInicio);
            dest.writeString(horaFin);
            dest.writeString(clasificacion);
            dest.writeLong(tipo);
        }
    }
}
