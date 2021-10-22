package com.locart.model;

import java.util.Date;

public class AlertMatch {

    String user_matches;
    String user_chat_start;
    Date user_matched;

    public AlertMatch() {
    }

    public AlertMatch(String user_matches, String user_chat_start, Date user_matched) {
        this.user_matches = user_matches;
        this.user_chat_start = user_chat_start;
        this.user_matched = user_matched;
    }

    public String getUser_matches() {
        return user_matches;
    }

    public void setUser_matches(String user_matches) {
        this.user_matches = user_matches;
    }

    public String getUser_chat_start() {
        return user_chat_start;
    }

    public void setUser_chat_start(String user_chat_start) {
        this.user_chat_start = user_chat_start;
    }

    public Date getUser_matched() {
        return user_matched;
    }

    public void setUser_matched(Date user_matched) {
        this.user_matched = user_matched;
    }
}
