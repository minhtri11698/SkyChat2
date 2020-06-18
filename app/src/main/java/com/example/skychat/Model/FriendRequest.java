package com.example.skychat.Model;

public class FriendRequest {
    String requestType;

    public FriendRequest() {

    }

    public FriendRequest(String requestType) {
        this.requestType = requestType;
    }

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }
}
