package com.example.namastesoicalnetwork.model_class;

public class Posts
{
    public String date, time, description, username, postimage, profileimage, userid ;

    public Posts() {
    }

    public Posts(String date, String time, String description, String username, String postimage, String profileimage, String userid) {
        this.date = date;
        this.time = time;
        this.description = description;
        this.username = username;
        this.postimage = postimage;
        this.profileimage = profileimage;
        this.userid = userid;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPostimage() {
        return postimage;
    }

    public void setPostimage(String postimage) {
        this.postimage = postimage;
    }

    public String getProfileimage() {
        return profileimage;
    }

    public void setProfileimage(String profileimage) {
        this.profileimage = profileimage;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }
}
