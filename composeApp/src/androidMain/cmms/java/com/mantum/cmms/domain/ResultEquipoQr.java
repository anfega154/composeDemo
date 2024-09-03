package com.mantum.cmms.domain;

public class ResultEquipoQr {
    private String entitytype;
    private String entityid;
    private String entitycode;
    private String entityname;
    private String entityfather;

    public ResultEquipoQr(String entitytype, String entityid, String entitycode, String entityname, String entityfather) {
        this.entitytype = entitytype;
        this.entityid = entityid;
        this.entitycode = entitycode;
        this.entityname = entityname;
        this.entityfather = entityfather;
    }

    public String getEntitytype() {
        return entitytype;
    }

    public String getEntityid() {
        return entityid;
    }

    public String getEntitycode() {
        return entitycode;
    }

    public String getEntityname() {
        return entityname;
    }

    public String getEntityfather() {
        return entityfather;
    }

    public void setEntitytype(String entitytype) {
        this.entitytype = entitytype;
    }

    public void setEntityid(String entityid) {
        this.entityid = entityid;
    }

    public void setEntitycode(String entitycode) {
        this.entitycode = entitycode;
    }

    public void setEntityname(String entityname) {
        this.entityname = entityname;
    }

    public void setEntityfather(String entityfather) {
        this.entityfather = entityfather;
    }
}
