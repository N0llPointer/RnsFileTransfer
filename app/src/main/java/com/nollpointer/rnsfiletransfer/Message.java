package com.nollpointer.rnsfiletransfer;

public class Message {
    private String message;
    private String additional;
    private long time;

    public Message(String message,String additional, long time) {
        this.message = message;
        this.additional = additional;
        this.time = time;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getAdditional() {
        return additional;
    }

    public void setAdditional(String additional) {
        this.additional = additional;
    }
}
