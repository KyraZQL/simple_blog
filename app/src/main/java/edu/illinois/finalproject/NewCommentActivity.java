package edu.illinois.finalproject;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import edu.illinois.finalproject.Models.Author;
import edu.illinois.finalproject.Models.Comment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

/**
 * Created by kyraz on 12/6/2017.
 */

public class NewCommentActivity extends BaseActivity {
    DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    private Context context = NewCommentActivity.this;
    public static final String POST_KEY_EXTRA = "post_key";
    EditText mEditText;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final DatabaseReference commentsRef = mDatabase.child("comments");

        final String postKey = getIntent().getStringExtra(POST_KEY_EXTRA);

        final String commentText = getIntent().getStringExtra("comment content");

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null|| user.isAnonymous()) {
            onCommentUploaded(getString(R.string.user_logged_out_error));
        }

        Author author = new Author(user.getDisplayName(),
                user.getPhotoUrl().toString(), user.getUid());

        Comment comment = new Comment(author, commentText.toString(),
                ServerValue.TIMESTAMP);
        commentsRef.child(postKey).push().setValue(comment, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError error, DatabaseReference firebase) {
                if (error != null) {

                    mEditText.setText(commentText);
                    onCommentUploaded("Error posting comment.");
                }
                onCommentUploaded(null);
            }
        });
    }

    public void onCommentUploaded(final String error) {
        NewCommentActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                dismissProgressDialog();
                if (error == null) {
                    Toast.makeText(context, "Comment created!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(context, error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}
