package com.locart.model;

import java.util.Date;

public class LinkClass {
    String user_links;
    String user_super;
    Date user_linked;

    public LinkClass() {
    }

    public LinkClass(String user_links, String user_super, Date user_linked) {
        this.user_links = user_links;
        this.user_super = user_super;
        this.user_linked = user_linked;
    }

    public String getUser_links() {
        return user_links;
    }

    public void setUser_links(String user_links) {
        this.user_links = user_links;
    }

    public String getUser_super() {
        return user_super;
    }

    public void setUser_super(String user_super) {
        this.user_super = user_super;
    }

    public Date getUser_linked() {
        return user_linked;
    }

    public void setUser_linked(Date user_linked) {
        this.user_linked = user_linked;
    }
}
