

package edu.illinois.finalproject.Models;

public class Author {
    private String userName;
    private String profile_picture;

    private String uid;

    public Author() {

    }

    public Author(String userName, String profile_picture, String uid) {
        this.userName = userName;
        this.profile_picture = profile_picture;
        this.uid = uid;
    }

    public String getUid() {
        return uid;
    }

    public String getUserName() {
        return userName;
    }

    public String getProfile_picture() {
        return profile_picture;
    }
}
