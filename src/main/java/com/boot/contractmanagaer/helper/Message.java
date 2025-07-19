package com.boot.contractmanagaer.helper;

public class Message {
    private String content;
    private String type; // e.g., "alert-success" or "alert-danger"

    public Message(String content, String type) {
        this.content = content;
        this.type = type;
    }

    // Getters and Setters
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}

