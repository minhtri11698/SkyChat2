package com.example.skychat.Model;

public class Conversation {
    Boolean seen;
    long time;

    public Conversation(Boolean seen, long time) {
        this.seen = seen;
        this.time = time;
    }

    public Conversation() {
    }

    public Boolean getSeen() {
        return seen;
    }

    public void setSeen(Boolean seen) {
        this.seen = seen;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
