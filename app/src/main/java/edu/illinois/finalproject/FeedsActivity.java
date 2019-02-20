package edu.illinois.finalproject;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FeedsActivity extends AppCompatActivity implements PostsFragment.OnPostSelectedListener {
    private static final String TAG = "edu.illinois.finalproject.FeedsActivity";
    private FloatingActionButton mFab;
    private final Context context = FeedsActivity.this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feeds);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ViewPager viewPager = (ViewPager) findViewById(R.id.feeds_view_pager);
        FeedsPagerAdapter adapter = new FeedsPagerAdapter(getSupportFragmentManager());

        adapter.addFragment(PostsFragment.newInstance(), "FEED");
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(1);


        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user == null || user.isAnonymous()) {
                    Toast.makeText(FeedsActivity.this, "You must sign-in to post.", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent newPostIntent = new Intent(FeedsActivity.this, NewPostActivity.class);
                startActivity(newPostIntent);
            }
        });
    }

    @Override
    public void onPostComment(final String postKey) {

        final EditText input = new EditText(context);

        final AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle("New Comment")
                .setView(R.layout.dialog_add);
        builder.setView(input);

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

    @Override
    public void onPostLike(final String postKey) {
        final String userKey = FirebaseUtil.getCurrentUserId();
        final DatabaseReference postLikesRef = FirebaseUtil.getLikesRef();
        if (userKey != null) {
            postLikesRef.child(postKey).child(userKey).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // User already liked this post, so we toggle like off.
                        postLikesRef.child(postKey).child(userKey).removeValue();
                    } else {
                        postLikesRef.child(postKey).child(userKey).setValue(ServerValue.TIMESTAMP);
                    }
                }

                @Override
                public void onCancelled(DatabaseError firebaseError) {

                }
            });
        }
    }
    @Override
    public void onPostItem(final String postKey) {
        Intent intent = new Intent(this, PostDetailActivity.class);
        intent.putExtra(PostDetailActivity.POST_KEY_EXTRA, postKey);

        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu. menu_feeds, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

            if (id == R.id.action_profile) {
            startActivity(new Intent(this, ProfileActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    class FeedsPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public FeedsPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

}
