package edu.illinois.finalproject;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import edu.illinois.finalproject.Models.Comment;
import edu.illinois.finalproject.Models.Post;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * Created by kyraz on 12/5/2017.
 */

public class PostDetailActivity extends AppCompatActivity {
    public static final String POST_KEY_EXTRA = "post_key";
    private final Context context = PostDetailActivity.this;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        final String postKey = getIntent().getStringExtra(POST_KEY_EXTRA);
        if (postKey == null) {
            finish();
        }
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        final DatabaseReference postRef = mDatabase.child("posts").child(postKey);
        final TextView authorTextView = (TextView) findViewById(R.id.post_author_name);
        final ImageView authorAvatar = (ImageView) findViewById(R.id.post_author_icon);
        final TextView timeTextView = (TextView) findViewById(R.id.post_timestamp);
        final TextView postTextView = (TextView) findViewById(R.id.post_text);
        final ImageView mPictureView = (ImageView) findViewById(R.id.post_picture);

        postRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final Post post = dataSnapshot.getValue(Post.class);
                if (post == null) {
                    return;
                }
                authorTextView.setText(post.getAuthor().getUserName());
                timeTextView.setText(convertTime((long) dataSnapshot.child("time").getValue()));
                postTextView.setText(dataSnapshot.child("text").getValue().toString());
                GlideUtil.loadProfileIcon(
                        dataSnapshot.child("author").child("profile_picture").getValue().toString(),
                        authorAvatar);

                if (post.getPicture() == null) {
                    mPictureView.setVisibility(View.GONE);
                } else {
                    mPictureView.setVisibility(View.VISIBLE);
                    GlideUtil.loadImage(post.getPicture(), mPictureView);
                }
                authorAvatar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent userDetailIntent = new Intent(context, UserDetailActivity.class);
                        userDetailIntent.putExtra(UserDetailActivity.USER_ID_EXTRA_NAME, post.getAuthor().getUid());
                        context.startActivity(userDetailIntent);
                    }
                });
                authorTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent userDetailIntent = new Intent(context, UserDetailActivity.class);
                        userDetailIntent.putExtra(UserDetailActivity.USER_ID_EXTRA_NAME, post.getAuthor().getUid());
                        context.startActivity(userDetailIntent);

                    }
                });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });
        // Clicking on the commentButton will pop up a dialog alert to let the user enter a comment.
        ImageView commentButton = (ImageView) findViewById(R.id.post_comment_icon);
        commentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user == null || user.isAnonymous()) {
                    Toast.makeText(context, "You must sign-in to make comments.", Toast.LENGTH_SHORT).show();
                    return;
                }
                final EditText input = new EditText(context);


                final AlertDialog.Builder builder = new AlertDialog.Builder(context)
                        .setTitle("New Comment")
                        .setView(R.layout.dialog_add);
                builder.setView(input);
                final AlertDialog alert = builder.create();
                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        String content = input.getText().toString();
                        Intent newCommentIntent = new Intent(context, NewCommentActivity.class);
                        newCommentIntent.putExtra(PostDetailActivity.POST_KEY_EXTRA, postKey);
                        newCommentIntent.putExtra("comment content", content);
                        if ( !content.isEmpty() ) {
                            startActivity(newCommentIntent);
                        }

                    }
                });
                builder.show();
            }
        });

        // Use FirebaseRecyclerAdapter to show all the comments of current post.
        DatabaseReference commentRef = mDatabase.child("comments").child(postKey);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.comment_list);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        FirebaseRecyclerAdapter<Comment, CommentViewHolder> adapter
                = new FirebaseRecyclerAdapter<Comment, CommentViewHolder>(
                Comment.class,
                R.layout.comment_item,
                CommentViewHolder.class,
                commentRef
        ) {

            @Override
            protected void populateViewHolder(CommentViewHolder commentViewHolder, Comment s, int i) {
                commentViewHolder.commentAuthor.setText(s.getAuthor().getUserName());
                GlideUtil.loadProfileIcon(s.getAuthor().getProfile_picture(), commentViewHolder.commentAuthorIcon);
                commentViewHolder.commentTime.setText(convertTime(Long.parseLong(s.getTimestamp().toString())));
                commentViewHolder.commentText.setText(s.getText());

            }
        };

        recyclerView.setAdapter(adapter);


    }

    /**
     * This method returns a formatted String to represent the time.
     * @param time long
     * @return String
     */
    public String convertTime(long time) {
        Date date = new Date(time);
        Format format = new SimpleDateFormat("yyyy MM dd HH:mm:ss");
        return format.format(date);
    }
}
