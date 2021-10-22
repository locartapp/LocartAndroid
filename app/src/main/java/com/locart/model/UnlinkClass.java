package com.locart.model;

import java.util.Date;

public class UnlinkClass {

    String user_unlink;
    Date user_unlinked;

    public UnlinkClass() {
    }

    public UnlinkClass(String user_unlink, Date user_unlinked) {
        this.user_unlink = user_unlink;
        this.user_unlinked = user_unlinked;
    }

    public String getUser_unlink() {
        return user_unlink;
    }

    public void setUser_unlink(String user_unlink) {
        this.user_unlink = user_unlink;
    }

    public Date getUser_unlinked() {
        return user_unlinked;
    }

    public void setUser_unlinked(Date user_unlinked) {
        this.user_unlinked = user_unlinked;
    }
}
