package com.locart.Message;

import java.util.Date;

public class MessageClass {


    Date user_date_sent;
    String user_sender;
    String user_receiver;
    String user_message;
    String user_unread;
    String message_type;
    String chat_time;

    public MessageClass() {
    }

    public MessageClass(Date user_date_sent, String user_sender, String user_receiver,
                        String user_message, String user_unread, String message_type,
                        String chat_time) {
        this.user_date_sent = user_date_sent;
        this.user_sender = user_sender;
        this.user_receiver = user_receiver;
        this.user_message = user_message;
        this.user_unread = user_unread;
        this.message_type = message_type;
        this.chat_time = chat_time;
    }

    public Date getUser_date_sent() {
        return user_date_sent;
    }

    public void setUser_date_sent(Date user_date_sent) {
        this.user_date_sent = user_date_sent;
    }

    public String getUser_sender() {
        return user_sender;
    }

    public void setUser_sender(String user_sender) {
        this.user_sender = user_sender;
    }

    public String getUser_receiver() {
        return user_receiver;
    }

    public void setUser_receiver(String user_receiver) {
        this.user_receiver = user_receiver;
    }

    public String getUser_message() {
        return user_message;
    }

    public void setUser_message(String user_message) {
        this.user_message = user_message;
    }

    public String getUser_unread() {
        return user_unread;
    }

    public void setUser_unread(String user_unread) {
        this.user_unread = user_unread;
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

