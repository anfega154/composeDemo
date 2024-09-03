package com.mantum.cmms.entity;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

import io.realm.RealmList;
import io.realm.RealmObject;

public class ListaChequeo extends RealmObject {
    private Long id;
    private Cuenta cuenta;
    private String codigo;
    private String nombre;
    private String especialidad;
    private String descripcion;
    private RealmList<Entidad> entidades;
    private RealmList<ClienteListaChequeo> clientes;
    private RealmList<EntidadesClienteListaChequeo> entidadescliente;
    private RealmList<PersonalListaChequeo> personal;

    private String idFirma;

    public ListaChequeo() {
        this.clientes = new RealmList<>();
        this.entidades = new RealmList<>();
        this.entidadescliente = new RealmList<>();
        this.personal = new RealmList<>();
    }

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

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEspecialidad() {
        return especialidad;
    }

    public void setEspecialidad(String especialidad) {
        this.especialidad = especialidad;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public RealmList<Entidad> getEntidades() {
        return entidades;
    }

    public void setEntidades(RealmList<Entidad> entidades) {
        this.entidades = entidades;
    }

    public RealmList<ClienteListaChequeo> getClientes() {
        return clientes;
    }

    public void setClientes(RealmList<ClienteListaChequeo> clientes) {
        this.clientes = clientes;
    }

    public RealmList<EntidadesClienteListaChequeo> getEntidadesCliente() {
        return entidadescliente;
    }

    public void setEntidadesCliente(RealmList<EntidadesClienteListaChequeo> entidadescliente) {
        this.entidadescliente = entidadescliente;
    }

    public RealmList<PersonalListaChequeo> getPersonal() {
        return personal;
    }

    public void setPersonal(RealmList<PersonalListaChequeo> personal) {
        this.personal = personal;
    }

    public String getIdFirma() {
        return idFirma;
    }

    public void setIdFirma(String idFirma) {
        this.idFirma = idFirma;
    }

    public static class Response {
        @SerializedName("aListaLc")
        private List<ListaChequeo> data;
        @SerializedName("iNextPage")
        private Integer next;
        @SerializedName("iPercent")
        private Integer percent;

        public Response() {
            data = new ArrayList<>();
        }

        public List<ListaChequeo> getData() {
            return data;
        }

        public void setData(List<ListaChequeo> data) {
            this.data = data;
        }

        public Integer getNext() {
            return next;
        }

        public void setNext(Integer next) {
            this.next = next;
        }

        public Integer getPercent() {
            return percent;
        }

        public void setPercent(Integer percent) {
            this.percent = percent;
        }
    }

    @Override
    public String toString() {
        return "ListaChequeo{" +
                "id=" + id +
                ", cuenta=" + cuenta +
                ", codigo='" + codigo + '\'' +
                ", nombre='" + nombre + '\'' +
                ", especialidad='" + especialidad + '\'' +
                ", descripcion='" + descripcion + '\'' +
                ", entidades=" + entidades +
                ", clientes=" + clientes +
                ", entidadescliente=" + entidadescliente +
                ", personal=" + personal +
                ", idfirma=" + idFirma +
                '}';
    }
}
