package com.mantum.cmms.domain;

import com.mantum.cmms.entity.Validation;

import java.util.List;

public class ResponseInventarioActivos {
    private boolean success;
    private int code;
    private String message;
    private List<Validation> data;

    public ResponseInventarioActivos(boolean success, int code, String message, List<Validation> data) {
        this.success = success;
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<Validation> getValidations() {
        return data;
    }

    public void setValidations(List<Validation> data) {
        this.data = data;
    }
}
