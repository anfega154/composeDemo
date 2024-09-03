package com.mantum.cmms.entity;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mantum.cmms.database.Model;
import com.mantum.cmms.entity.parameter.UserPermission;
import com.mantum.component.Mantum;

import java.io.Serializable;
import java.util.Date;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Cuenta extends RealmObject implements Model, Serializable {

    @PrimaryKey
    private String UUID;

    private Long id;

    private Servidor servidor;

    private Date timestamp;

    private String username;

    private String password;

    private String name;

    private String lastname;

    private String image;

    private boolean active;

    private Long idCalendario;

    private boolean disponible;

    private boolean activeDirectory;

    public boolean isActiveDirectory() {
        return activeDirectory;
    }

    public void setActiveDirectory(boolean activeDirectory) {
        this.activeDirectory = activeDirectory;
    }

    public Cuenta() {
        this.disponible = true;
    }

    public Long getIdCalendario() {
        return idCalendario;
    }

    public void setIdCalendario(Long idCalendario) {
        this.idCalendario = idCalendario;
    }

    public String getUUID() {
        return UUID;
    }

    public void setUUID(String UUID) {
        this.UUID = UUID;
    }

    public Long getId() {
        return id;
    }

    public Cuenta setId(Long id) {
        this.id = id;
        return this;
    }

    public Servidor getServidor() {
        return servidor;
    }

    public void setServidor(Servidor servidor) {
        this.servidor = servidor;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public Cuenta setName(String name) {
        this.name = name;
        return this;
    }

    public String getLastname() {
        return lastname;
    }

    public Cuenta setLastname(String lastname) {
        this.lastname = lastname;
        return this;
    }

    public String getImage() {
        return this.image;
    }

    public Cuenta setImage(String image) {
        this.image = image;
        return this;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getToken(String seed) {
        return Mantum.md5(getUsername() + seed + getPassword());
    }

    @Nullable
    public String getToken(@NonNull Context context) {
        Bundle bundle = Mantum.bundle(context);
        if (bundle == null) {
            return null;
        }

        String seed = bundle.getString("Mantum.Authentication.Token");
        return Mantum.md5(getUsername() + seed + getPassword());
    }

    public boolean isDisponible() {
        return disponible;
    }

    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }

    public static class Request {

        private Long id;

        private String name;

        private String lastname;

        private String image;

        private Parametro.Request params;

        private boolean activeDirectory;

        public boolean getActiveDirectory() {
            return activeDirectory;
        }

        public void setActiveDirectory(boolean activeDirectory) {
            this.activeDirectory = activeDirectory;
        }

        public Long getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getLastname() {
            return lastname;
        }

        public String getImage() {
            return this.image;
        }

        public void setParams(Parametro.Request param) {
            this.params = param;
        }

        public Parametro.Request getParams() {
            return this.params;
        }

        private RealmList<UserPermission> permissions;

        public RealmList<UserPermission> getPermissions() {
            return permissions;
        }

        public void setPermissions(RealmList<UserPermission> permissions) {
            this.permissions = permissions;
        }
    }
}