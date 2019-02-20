package edu.illinois.finalproject;

import edu.illinois.finalproject.Models.Author;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

class FirebaseUtil {
    public static DatabaseReference getBaseRef() {
        return FirebaseDatabase.getInstance().getReference();
    }

    public static String getCurrentUserId() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            return user.getUid();
        }
        return null;
    }

    public static Author getAuthor() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null|| user.isAnonymous()) return null;
        return new Author(user.getDisplayName(), user.getPhotoUrl().toString(), user.getUid());
    }

    public static DatabaseReference getPostsRef() {
        return getBaseRef().child("posts");
    }

    public static DatabaseReference getPeopleRef() {
        return getBaseRef().child("people");
    }

    public static DatabaseReference getCommentsRef() {
        return getBaseRef().child("comments");
    }

    public static DatabaseReference getLikesRef() {
        return getBaseRef().child("likes");
    }

}
