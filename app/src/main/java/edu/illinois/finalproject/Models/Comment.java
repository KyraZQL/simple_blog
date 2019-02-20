
package edu.illinois.finalproject.Models;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

public class Comment {
    private Author author;
    private String text;
    private Object timestamp;

    private Post post;

    public Comment() {
    }

    public Comment(Author author, String text, Object timestamp) {
        this.author = author;
        this.text = text;
        this.timestamp = timestamp;
    }

    public Comment(Author author, String text, Object timestamp, Post post) {
        this.author = author;
        this.text = text;
        this.timestamp = timestamp;
        this.post = post;
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

    public Post getPost() {
        return post;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("author", author);
        result.put("text", text);
        result.put("time", timestamp);
        result.put("post", post);

        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Comment comment = (Comment) o;

        if (author != null ? !author.equals(comment.author) : comment.author != null) return false;
        if (text != null ? !text.equals(comment.text) : comment.text != null) return false;
        if (timestamp != null ? !timestamp.equals(comment.timestamp) : comment.timestamp != null)
            return false;
        return post != null ? post.equals(comment.post) : comment.post == null;
    }

    @Override
    public int hashCode() {
        int result = author != null ? author.hashCode() : 0;
        result = 31 * result + (text != null ? text.hashCode() : 0);
        result = 31 * result + (timestamp != null ? timestamp.hashCode() : 0);
        result = 31 * result + (post != null ? post.hashCode() : 0);
        return result;
    }

}
