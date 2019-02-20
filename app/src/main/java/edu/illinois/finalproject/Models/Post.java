

package edu.illinois.finalproject.Models;


import android.net.Uri;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

public class Post {
    private Author author;
    private String text;
    private Object timestamp;
    private Map<User, Boolean> usersLiking;
    private Map<Comment, Boolean> comments;
    private String picture;

    public Post() {
    }
    public Post(Author author, String text, Object timestamp) {
        this.author = author;
        this.text = text;
        this.timestamp = timestamp;
    }
    public Post(Author author, String text,
                Object timestamp, Map<User,Boolean> usersLiking, Map<Comment, Boolean> comments) {
        this.author = author;
        this.text = text;
        this.timestamp = timestamp;
        this.usersLiking = usersLiking;
        this.comments = comments;
    }

    public Post(Author author, String text, Object timestamp, String picture) {
        this.author = author;
        this.text = text;
        this.timestamp = timestamp;
        this.picture = picture;
    }

    public Author getAuthor() {
        return author;
    }

    public String getText() {
        return text;
    }

    public Object getTimestamp() {
        return timestamp;
    }

    public Map<User, Boolean> getUsersLiking() {
        return usersLiking;
    }

    public Map<Comment, Boolean> getComments() {
        return comments;
    }

    public String getPicture() {
        return picture;
    }


    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("author", author);
        result.put("text", text);
        result.put("time", timestamp);
        if (picture != null ) {
            result.put("picture", picture);
        }
//        result.put("usersLiking", usersLiking);
//        result.put("comments", comments);

        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Post post = (Post) o;

        if (author != null ? !author.equals(post.author) : post.author != null) return false;
        if (text != null ? !text.equals(post.text) : post.text != null) return false;
        if (timestamp != null ? !timestamp.equals(post.timestamp) : post.timestamp != null)
            return false;
        if (usersLiking != null ? !usersLiking.equals(post.usersLiking) : post.usersLiking != null)
            return false;
        return comments != null ? comments.equals(post.comments) : post.comments == null;
    }

    @Override
    public int hashCode() {
        int result = author != null ? author.hashCode() : 0;
        result = 31 * result + (text != null ? text.hashCode() : 0);
        result = 31 * result + (timestamp != null ? timestamp.hashCode() : 0);
        result = 31 * result + (usersLiking != null ? usersLiking.hashCode() : 0);
        result = 31 * result + (comments != null ? comments.hashCode() : 0);
        return result;
    }
}
