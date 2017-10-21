package com.laioffer.eventreporter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by guo on 2017/8/28.
 */

public class Event {
    /**
     * All data for a event.
     */
    private String title;
    private String address;
    private String description;

    private int like;
    private String id;
    private long time;
    private String username;

    private int CommentNumber;
    private Map<String, Boolean> map = new HashMap<>();

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("id", id);
        result.put("username", username);
        result.put("map", map);
        result.put("title", title);
        result.put("address", address);
        result.put("description", description);
        result.put("like", like);
        result.put("time", time);
        result.put("CommentNumber", CommentNumber);
        return result;
    }

    public Map<String, Boolean> getMap() {
        return map;
    }

    public void setMap(Map<String, Boolean> map) {
        this.map = map;
    }

    public int getCommentNumber() {
        return CommentNumber;
    }

    public void setCommentNumber(int commentNumber) {
        this.CommentNumber = commentNumber;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getLike() {
        return like;
    }

    public void setLike(int like) {
        this.like = like;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getImgUri() {
        return imgUri;
    }

    public void setImgUri(String imgUri) {
        this.imgUri = imgUri;
    }

    private String imgUri;

    public Event() {}

    /**
     * Constructor
     */
    public Event(String title, String address, String description) {
        this.title = title;
        this.address = address;
        this.description = description;
    }

    /**
     * Getters for private attributes of Event class.
     */
    public String getTitle() { return this.title; }
    public String getAddress() { return this.address; }
    public String getDescription() { return this.description; }
}
