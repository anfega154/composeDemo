package com.mantum.cmms.domain;

public class QR {

    private final String url;

    private final String login;

    private final String pass;

    private final Integer base;

    private final String basename;

    public QR(String url, String login, String pass, Integer base, String basename) {
        this.url = url;
        this.login = login;
        this.pass = pass;
        this.base = base;
        this.basename = basename;
    }

    public String getUrl() {
        return url;
    }

    public String getLogin() {
        return login;
    }

    public String getPass() {
        return pass;
    }

    public Integer getBase() {
        return base;
    }

    public String getBasename() {
        return basename;
    }

    @Override
    public String toString() {
        return "QR{" +
                "url='" + url + '\'' +
                ", login='" + login + '\'' +
                ", pass='" + pass + '\'' +
                ", base=" + base +
                ", basename='" + basename + '\'' +
                '}';
    }
}