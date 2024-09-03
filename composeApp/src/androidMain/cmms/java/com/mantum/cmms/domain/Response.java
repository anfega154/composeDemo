package com.mantum.cmms.domain;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;

import com.google.gson.Gson;

import java.util.List;

public class Response {

    private Integer version;

    private final String message;

    private final Object body;

    private Object extra;

    private final List<Detalle> error;

    public Response(String message, Object body, List<Detalle> error) {
        this.message = message;
        this.body = body;
        this.error = error;
    }

    public void setVersion(String version) {
        this.version = version == null ? null : Integer.parseInt(version);
    }

    public Integer getVersion() {
        return version;
    }

    public String getMessage() {
        return message;
    }

    public boolean isValid() {
        return this.body != null;
    }

    public String getBody() {
        return new Gson().toJson(this.body);
    }

    public <T> T getBody(Class<T> clazz) {
        return new Gson().fromJson(new Gson().toJson(this.body), clazz);
    }

    public <T> T getBody(Class<T> clazz, Gson gson) {
        return gson.fromJson(gson.toJson(this.body), clazz);
    }

    public List<Detalle> getError() {
        return error;
    }

    public Object getExtra() {
        return extra;
    }

    public void setExtra(Object extra) {
        this.extra = extra;
    }

    @NonNull
    public static String buildMessage(@Nullable Response content) {
        if (content == null) {
            return "No fue posible obtener el mensaje";
        }

        try {
            if (content.getError() == null || content.getError().isEmpty() || content.getError().get(0) == null
                    || content.getError().get(0).getMessages() == null || content.getError().get(0).getMessages().isEmpty()) {
                String message = content.getMessage();
                if (message == null || message.isEmpty()) {
                    message = "No fue posible obtener el mensaje";
                }
                return message;
            }
            return TextUtils.join(", ", content.getError().get(0).getMessages());
        } catch (Exception e) {
            String message = content.getMessage();
            if (message == null || message.isEmpty()) {
                message = "No fue posible obtener el mensaje";
            }
            return message;
        }
    }

    public static class Detalle {

        private final String title;

        private final List<String> messages;

        public Detalle(String title, List<String> messages) {
            this.title = title;
            this.messages = messages;
        }

        public String getTitle() {
            return title;
        }

        public List<String> getMessages() {
            return messages;
        }
    }
}
