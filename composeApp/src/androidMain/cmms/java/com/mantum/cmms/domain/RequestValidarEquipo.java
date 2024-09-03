package com.mantum.cmms.domain;


import com.mantum.cmms.Multipart;

public class RequestValidarEquipo{
    private String code;
    private String name_equipment;
    private long id_validation;
    private long id_person_count;
    private String counting_method;
    private boolean qr_code_associated;

    public RequestValidarEquipo(String code, String name_equipment, long id_validation, long id_person_count, String counting_method, boolean qr_code_associated) {
        this.code = code;
        this.name_equipment = name_equipment;
        this.id_validation = id_validation;
        this.id_person_count = id_person_count;
        this.counting_method = counting_method;
        this.qr_code_associated = qr_code_associated;
    }

    public String getNameEquipment() {
        return name_equipment;
    }

    public void setNameEquipment(String nameEquipment) {
        this.name_equipment = nameEquipment;
    }

    public String getCode() {
        return code;
    }

    public long getIdValidation() {
        return id_validation;
    }

    public long getIdPersonCount() {
        return id_person_count;
    }

    public String getCountingMethod() {
        return counting_method;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setIdValidation(long id_validation) {
        this.id_validation = id_validation;
    }

    public void setIdPersonCount(long id_person_count) {
        this.id_person_count = id_person_count;
    }

    public boolean isQrCodeAssociated() {
        return qr_code_associated;
    }

    public void setQrCodeAssociated(boolean qr_code_associated) {
        this.qr_code_associated = qr_code_associated;
    }

    public void setCountingMethod(String counting_method) {
        this.counting_method = counting_method;
    }
}
