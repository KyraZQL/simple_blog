package edu.illinois.finalproject;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.HeaderViewListAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import edu.illinois.finalproject.Models.Post;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import edu.illinois.finalproject.Models.User;

import static android.support.v7.widget.LinearLayoutManager.VERTICAL;
import static edu.illinois.finalproject.UserDetailActivity.USER_ID_EXTRA_NAME;

public class UserDetailActivity extends AppCompatActivity {
    private final String TAG = "edu.illinois.finalproject.UserDetailActivity";
    public static final String USER_ID_EXTRA_NAME = "user_name";
    private final Context context = UserDetailActivity.this;
    private RecyclerView recyclerView;
    private PostAdapter mGridAdapter;

    private ValueEventListener mUserInfoListener;
    private String mUserId;
    private DatabaseReference mUserRef;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_detail);

        Intent intent = getIntent();
        mUserId = intent.getStringExtra(USER_ID_EXTRA_NAME);
        if (mUserId == null) {
            mUserId = FirebaseUtil.getCurrentUserId();
        }
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);

        recyclerView = (RecyclerView) findViewById(R.id.user_posts_grid);

        mGridAdapter = new PostAdapter();
        this.recyclerView.setAdapter(mGridAdapter);
        this.recyclerView.setLayoutManager(new LinearLayoutManager(this, VERTICAL, false));

        mUserRef = FirebaseUtil.getPeopleRef().child(mUserId);
        mUserInfoListener = mUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);

                Map<String, Object> userValue = (Map) dataSnapshot.getValue();

                Log.w(TAG, "mUserRef:" + mUserRef.getKey());
                CircleImageView userPhoto = (CircleImageView) findViewById(R.id.user_detail_photo);
                if (!isActivityDestroyed(UserDetailActivity.this)) {
                    GlideUtil.loadProfileIcon(userValue.get("photoUrl").toString(), userPhoto);
                }
                String name = userValue.get("displayName").toString();
                if (name == null) {
                    name = getString(R.string.user_info_no_name);
                }
                collapsingToolbar.setTitle(name);

                if (user.getPosts() != null) {


                    List<String> paths = new ArrayList<String>(user.getPosts().keySet());
                    for (int i = 0; i < paths.size(); i++) {
                        Log.i(TAG, "onDataChange: " + paths.get(i));
                    }
                    mGridAdapter.addPaths(paths);

                }

            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {

            }
        });
    }

    @Override
    protected void onDestroy() {

        mUserRef.child(mUserId).removeEventListener(mUserInfoListener);
        super.onDestroy();

    }

    private static boolean isActivityDestroyed(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1
                && activity != null && activity.isDestroyed()) {
            return true;
        }
        return false;
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }

}


class PostAdapter extends RecyclerView.Adapter<SimplePostViewHolder> {
    private static final String TAG = "";
    private List<String> mPostPaths;
    private String mUserId;

    public PostAdapter() {
        mPostPaths = new ArrayList<String>();
    }

    @Override
    public int getItemViewType(int position) {

        return R.layout.user_post_item;
    }

    @Override
    public SimplePostViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        LayoutInflater inflater = (LayoutInflater) parent.getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View postItem = inflater.inflate(viewType, parent, false);

        return new SimplePostViewHolder(postItem);

    }

    @Override
    public void onBindViewHolder(final SimplePostViewHolder holder, int position) {


        mUserId = FirebaseUtil.getCurrentUserId();

        Log.i(TAG, "onBindViewHolder: " + mUserId);
        DatabaseReference ref = FirebaseUtil.getPostsRef().child(mPostPaths.get(position));
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                final String postKey = dataSnapshot.getKey();

                final Post post = dataSnapshot.getValue(Post.class);
                if (post == null) {
                    // Current user doesn't have any posts.
                    Log.i(TAG, "onDataChange: null post");
                } else {
                    GlideUtil.loadProfileIcon(post.getAuthor().getProfile_picture(), holder.mIconView);
                    holder.mAuthorView.setText(post.getAuthor().getUserName());
                    holder.mPostTextView.setText(post.getText());
                    Log.i(TAG, "onDataChange: " + post.getAuthor().getUid());

                    if (post.getAuthor().getUid().equals(mUserId)) {
                        // This is the current user's own detail page.
                        holder.mDeletePostView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                final Context context = v.getContext();

                                final AlertDialog.Builder builder = new AlertDialog.Builder(context)
                                        .setTitle("Do you really want delete this post?")
                                        .setView(R.layout.delete_check)
                                        .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int id) {
                                                dataSnapshot.getRef().removeValue();
                                                FirebaseUtil.getPeopleRef().child(mUserId)
                                                        .child("posts").child(postKey).getRef().removeValue();
                                                FirebaseUtil.getCommentsRef().child(postKey).getRef().removeValue();
                                                Toast.makeText(context, "Post deleted.", Toast.LENGTH_SHORT).show();
                                            }
                                        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int id) {
                                                dialog.dismiss();
                                            }
                                        });

                                final AlertDialog alert = builder.create();
                                builder.show();
                            }

                        });
                    } else {
                        // The delete post button won't come up.
                        holder.mDeletePostView.setVisibility(View.GONE);
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    public void addPaths(List<String> paths) {
        int startIndex = mPostPaths.size();
        mPostPaths.addAll(paths);
        notifyItemRangeInserted(startIndex, mPostPaths.size());
    }

    @Override
    public int getItemCount() {
        return mPostPaths.size();
    }
}


