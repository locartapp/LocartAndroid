package com.locart.model;

import java.util.Date;

public class LikeClass {

    String user_likes;
    Date user_liked;
    String user_super;

    public LikeClass() {
    }

    public LikeClass(String user_likes, Date user_liked, String user_super) {
        this.user_likes = user_likes;
        this.user_liked = user_liked;
        this.user_super = user_super;
    }

    public String getUser_likes() {
        return user_likes;
    }

    public void setUser_likes(String user_likes) {
        this.user_likes = user_likes;
    }

    public Date getUser_liked() {
        return user_liked;
    }

    public void setUser_liked(Date user_liked) {
        this.user_liked = user_liked;
    }

    public String getUser_super() {
        return user_super;
    }

    public void setUser_super(String user_super) {
        this.user_super = user_super;
    }
}
