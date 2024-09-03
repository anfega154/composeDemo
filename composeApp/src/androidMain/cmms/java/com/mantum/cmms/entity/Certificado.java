package com.mantum.cmms.entity;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Certificado extends RealmObject {

    @PrimaryKey
    private String key;

    private Date creation;

    private String client;

    private String server;

    private String password;

    private String url;

    private String username;

    private String database;

    public String getKey() {
        return key;
    }

    public Certificado setKey(String key) {
        this.key = key;
        return this;
    }

    public Date getCreation() {
        return creation;
    }

    public Certificado setCreation(Date creation) {
        this.creation = creation;
        return this;
    }

    public String getClient() {
        return client;
    }

    public Certificado setClient(String client) {
        this.client = client;
        return this;
    }

    public String getServer() {
        return server;
    }

    public Certificado setServer(String server) {
        this.server = server;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public Certificado setUrl(String url) {
        this.url = url;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public Certificado setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public Certificado setPassword(String password) {
        this.password = password;
        return this;
    }

    public String getDatabase() {
        return database;
    }

    public Certificado setDatabase(String database) {
        this.database = database;
        return this;
    }
}