package com.mantum.core.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Contiene los métodos necesarios para gestionar los datos
 * guardados en cache
 *
 * @author Jonattan Velásquez
 */
@Deprecated
public class Cache {

    private final Map<String, Object> cache;

    private static final Cache ourInstance = new Cache();

    /**
     * Obtiene la instancia actual del objeto
     * @return {@link Cache}
     */
    public static Cache getInstance() {
        return ourInstance;
    }

    /**
     * Obtiene una nueva instancia del objeti
     */
    private Cache() {
        this.cache = new HashMap<>();
    }

    /**
     * Agrega un nuevo objeto de cache
     *
     * @param key {@link String}
     * @param value {@link String}
     * @return {@link Cache}
     */
    public Cache add(String key, String value) {
        this.cache.put(key, value);
        return this;
    }

    public Cache add(String key, boolean value) {
        this.cache.put(key, value);
        return this;
    }

    /**
     * Agrega un nuevo objeto de cache
     *
     * @param key {@link String}
     * @param value {@link Integer}
     * @return {@link Cache}
     */
    public Cache add(String key, Integer value) {
        this.cache.put(key, value);
        return this;
    }

    /**
     * Obtiene el valor que coincida con la clave enviada
     *
     * @param key {@link String}
     * @param clazz {@link Class}
     */
    public <T> T get(String key, Class<T> clazz) {
        return clazz.cast(this.cache.get(key));
    }
}
