package com.example.shreyesh.gochat;

public class Messages {
    String message;
    Boolean seen;
    String type;
    Long time;

    public Messages() {
    }


    public Messages(String message, Boolean seen, String type, Long time) {
        this.message = message;
        this.seen = seen;
        this.type = type;
        this.time = time;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Boolean getSeen() {
        return seen;
    }

    public void setSeen(Boolean seen) {
        this.seen = seen;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }
}
