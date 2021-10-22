package com.locart.Chats;

import java.util.Date;

public class ChatUserClass {

    Date user_date_sent;
    String user_receiver;
    String user_sender;
    String user_message;
    String user_unread;

    public ChatUserClass() {
    }

    public ChatUserClass(Date user_date_sent, String user_receiver, String user_sender,
                         String user_message, String user_unread) {
        this.user_date_sent = user_date_sent;
        this.user_receiver = user_receiver;
        this.user_sender = user_sender;
        this.user_message = user_message;
        this.user_unread = user_unread;
    }

    public Date getUser_date_sent() {
        return user_date_sent;
    }

    public void setUser_date_sent(Date user_date_sent) {
        this.user_date_sent = user_date_sent;
    }

    public String getUser_receiver() {
        return user_receiver;
    }

    public void setUser_receiver(String user_receiver) {
        this.user_receiver = user_receiver;
    }

    public String getUser_sender() {
        return user_sender;
    }

    public void setUser_sender(String user_sender) {
        this.user_sender = user_sender;
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
}
