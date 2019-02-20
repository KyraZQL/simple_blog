

package edu.illinois.finalproject.Models;

import java.util.Map;

public class User {
    private String displayName;
    private String photoUrl;
    private Map<String, Boolean> posts;
    private Map<String, Object> following;

    public User() {

    }

    public User(String displayName, String profile_picture) {
        this.displayName = displayName;
        this.photoUrl = photoUrl;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public Map<String, Boolean> getPosts() {
        return posts;
    }

    public Map<String, Object> getFollowing() {
        return following;
    }
}
