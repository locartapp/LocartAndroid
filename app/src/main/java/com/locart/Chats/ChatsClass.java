package com.locart.Chats;

import java.util.Date;

public class ChatsClass {

    private Date chat_date_sent;
    private Date chat_date_seen;
    private String chat_sender;
    private String chat_receiver;
    private String chat_message;
    private String chat_seen_chat;
    private String delete_sender;
    private String delete_receiver;
    private String message_type;
    private String chat_time;

    public ChatsClass() {

    }

    public ChatsClass(Date chat_date_sent, Date chat_date_seen, String chat_sender,
                      String chat_receiver, String chat_message, String chat_seen_chat,
                      String delete_sender, String delete_receiver, String message_type,
                      String chat_time) {
        this.chat_date_sent = chat_date_sent;
        this.chat_date_seen = chat_date_seen;
        this.chat_sender = chat_sender;
        this.chat_receiver = chat_receiver;
        this.chat_message = chat_message;
        this.chat_seen_chat = chat_seen_chat;
        this.delete_sender = delete_sender;
        this.delete_receiver = delete_receiver;
        this.message_type = message_type;
        this.chat_time = chat_time;
    }

    public Date getChat_date_sent() {
        return chat_date_sent;
    }

    public void setChat_date_sent(Date chat_date_sent) {
        this.chat_date_sent = chat_date_sent;
    }

    public Date getChat_date_seen() {
        return chat_date_seen;
    }

    public void setChat_date_seen(Date chat_date_seen) {
        this.chat_date_seen = chat_date_seen;
    }

    public String getChat_sender() {
        return chat_sender;
    }

    public void setChat_sender(String chat_sender) {
        this.chat_sender = chat_sender;
    }

    public String getChat_receiver() {
        return chat_receiver;
    }

    public void setChat_receiver(String chat_receiver) {
        this.chat_receiver = chat_receiver;
    }

    public String getChat_message() {
        return chat_message;
    }

    public void setChat_message(String chat_message) {
        this.chat_message = chat_message;
    }

    public String getChat_seen_chat() {
        return chat_seen_chat;
    }

    public void setChat_seen_chat(String chat_seen_chat) {
        this.chat_seen_chat = chat_seen_chat;
    }

    public String getDelete_sender() {
        return delete_sender;
    }

    public void setDelete_sender(String delete_sender) {
        this.delete_sender = delete_sender;
    }

    public String getDelete_receiver() {
        return delete_receiver;
    }

    public void setDelete_receiver(String delete_receiver) {
        this.delete_receiver = delete_receiver;
    }

    public String getMessage_type() {
        return message_type;
    }

    public void setMessage_type(String message_type) {
        this.message_type = message_type;
    }

    public String getChat_time() {
        return chat_time;
    }

    public void setChat_time(String chat_time) {
        this.chat_time = chat_time;
    }
}
