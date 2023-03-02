package com.example.chatbot;


public class ChatModel {
    public static final String user_key ="user";
    public static  final String bot_key ="robot";

    String message;
    String sentBy;

    public ChatModel(String message, String sentBy) {
        this.message = message;
        this.sentBy = sentBy;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSentBy() {
        return sentBy;
    }

    public void setSentBy(String sentBy) {
        this.sentBy = sentBy;
    }
}