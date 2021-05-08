package com.jacup101.esportstalk;

public class User {

    private String username;
    private String followed;

    public User(String username, String followed) {
        this.username = username;
        this.followed = followed;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFollowed() {
        return followed;
    }

    public void setFollowed(String followed) {
        this.followed = followed;
    }
}
