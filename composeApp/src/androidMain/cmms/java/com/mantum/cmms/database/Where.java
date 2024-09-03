package com.mantum.cmms.database;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Contiene todos los métodos para configurar los parametros
 * de busqueda en la base de datos
 *
 * @author Jonattan Velásquez
 */
@Deprecated
public class Where {

    private final Map<String, Object> equalTo;

    private final ArrayList<String> isNotNull;

    private final Map<String, Object> contains;

    private final Map<String, Object> greaterThanOrEqualTo;

    /**
     * Obtiene una nueva instancia del objeto
     */
    public Where() {
        this.equalTo = new HashMap<>();
        this.contains = new HashMap<>();
        this.isNotNull = new ArrayList<>();
        this.greaterThanOrEqualTo = new HashMap<>();
    }

    /**
     * Agrega que un campo contiene el parametro enviado como argumento
     *
     * @param field {@link String}
     * @param value {@link String}
     * @return {@link Where}
     */
    public Where contains(String field, String value) {
        this.contains.put(field, value);
        return this;
    }

    /**
     * Agrega que un campo contiene el parametro enviado como argumento
     *
     * @param field {@link String}
     * @param value {@link Long}
     * @return {@link Where}
     */
    public Where contains(String field, Long value) {
        this.contains.put(field, value);
        return this;
    }

    /**
     * Agrega que un campo debe de ser igual al parametro enviado como argumento
     *
     * @param field {@link String}
     * @param value {@link String}
     * @return {@link Where}
     */
    public Where equalTo(String field, String value) {
        this.equalTo.put(field, value);
        return this;
    }

    public Where equalTo(String field, Date value) {
        this.equalTo.put(field, value);
        return this;
    }

    /**
     * Agrega que un campo debe de ser igual al parametro enviado como argumento
     *
     * @param field {@link String}
     * @param value {@link Integer}
     * @return {@link Where}
     */
    public Where equalTo(String field, Integer value) {
        this.equalTo.put(field, value);
        return this;
    }

    /**
     * Agrega que un campo debe de ser igual al parametro enviado como argumento
     *
     * @param field {@link String}
     * @param value {@link Long}
     * @return {@link Where}
     */
    public Where equalTo(String field, Long value) {
        this.equalTo.put(field, value);
        return this;
    }

    /**
     * Agrega que un campo debe de ser igual al parametro enviado como argumento
     *
     * @param field {@link String}
     * @param value {@link Boolean}
     * @return {@link Where}
     */
    public Where equalTo(String field, Boolean value) {
        this.equalTo.put(field, value);
        return this;
    }

    /**
     * Indica que la columna no puede ser nula
     *
     * @param field {@link String}
     * @return {@link Where}
     */
    public Where isNotNull(String field) {
        this.isNotNull.add(field);
        return this;
    }

    public Where greaterThanOrEqualTo(String field, Date value) {
        this.greaterThanOrEqualTo.put(field, value);
        return this;
    }

    /**
     * Obtiene el contenedor que maneja las validaciones de igual a
     * @return {@link Map}
     */
    public Map<String, Object> equalTo() {
        return this.equalTo;
    }

    /**
     * Obtiene el contenedor que maneja la validación de no nulos
     * @return {@link ArrayList}
     */
    public ArrayList<String> isNotNull() {
        return this.isNotNull;
    }

    /**
     * Obtiene el contenedor que maneja las validaciones que contiene
     * @return {@link Map}
     */
    public Map<String, Object> contains() {
        return this.contains;
    }

    public Map<String, Object> greaterThanOrEqualTo() {
        return this.greaterThanOrEqualTo;
    }

    @Override
    public String toString() {
        return "Where{" +
                "equalTo=" + equalTo +
                ", isNotNull=" + isNotNull +
                ", contains=" + contains +
                ", greaterThanOrEqualTo=" + greaterThanOrEqualTo +
                '}';
    }
}