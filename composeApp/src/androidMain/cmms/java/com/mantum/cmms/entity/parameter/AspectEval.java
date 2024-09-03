package com.mantum.cmms.entity.parameter;

import com.mantum.cmms.database.Model;

import java.io.Serializable;

import io.realm.RealmObject;

public class AspectEval extends RealmObject implements Model, Serializable {

    private Long id;

    private String nombre;

    private String descripcion;

    private int calificacion;

    /**
     * Obtiene el id de los aspectos a evaluar
     * @return {@link Long}
     */
    public Long getId() {
        return id;
    }

    /**
     * Setea el id de los aspectos a evaluar
     * @param id {@link Long}
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Obtiene el nombre de los aspectos a evaluar
     * @return {@link String}
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Setea el nombre de los aspectos a evaluar
     * @param nombre {@link String}
     */
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    /**
     * Obtiene la descripcion de los aspectos a evaluar
     * @return {@link String}
     */
    public String getDescripcion() {
        return descripcion;
    }

    /**
     * Setea la descripcion de los aspectos a evaluar
     * @param descripcion {@link String}
     */
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    /**
     * Obtiene la calificacion de los aspectos a evaluar
     */
    public int getCalificacion() {
        return calificacion;
    }

    /**
     * Setea la califaciion de los aspectos a evaluar
     */
    public void setCalificacion(int calificacion) {
        this.calificacion = calificacion;
    }

    @Override
    public String toString() {
        return "AspectEval{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", descripcion='" + descripcion + '\'' +
                ", calificacion=" + calificacion +
                '}';
    }
}
