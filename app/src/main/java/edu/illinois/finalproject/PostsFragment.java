package edu.illinois.finalproject;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import edu.illinois.finalproject.Models.Author;
import edu.illinois.finalproject.Models.Post;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Shows a list of posts.
 */
public class PostsFragment extends Fragment {

    public static final String TAG = "edu.illinois.finalproject.PostsFragment";
    private static final String KEY_LAYOUT_POSITION = "layoutPosition";
    private int mRecyclerViewPosition = 0;
    private OnPostSelectedListener mListener;
    private DatabaseReference mLikeRef;


    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter<PostViewHolder> mAdapter;

    public PostsFragment() {
        // Required empty public constructor
    }

    public static PostsFragment newInstance() {
        PostsFragment fragment = new PostsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_posts, container, false);
        rootView.setTag(TAG);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.my_recycler_view);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        mRecyclerView.setLayoutManager(linearLayoutManager);

        if (savedInstanceState != null) {
            // Restore saved layout manager type.
            mRecyclerViewPosition = (int) savedInstanceState
                    .getSerializable(KEY_LAYOUT_POSITION);
            mRecyclerView.scrollToPosition(mRecyclerViewPosition);

        }

        Query allPostsQuery = FirebaseUtil.getPostsRef();
        mAdapter = getFirebaseRecyclerAdapter(allPostsQuery);
        mAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
            }
        });

        mRecyclerView.setAdapter(mAdapter);
    }

    private FirebaseRecyclerAdapter<Post, PostViewHolder> getFirebaseRecyclerAdapter(Query query) {
        return new FirebaseRecyclerAdapter<Post, PostViewHolder>(
                Post.class, R.layout.post_item_with_picture, PostViewHolder.class, query) {
            @Override
            public void populateViewHolder(final PostViewHolder postViewHolder,
                                           final Post post, final int position) {
                setupPost(postViewHolder, post, position, null);
            }

            @Override
            public void onViewRecycled(PostViewHolder holder) {
                super.onViewRecycled(holder);
            }
        };
    }

    /**
     * This method sets up the post item view and let users with authentication like or make comments
     * on it.
     *
     * @param postViewHolder PostViewHolder
     * @param post           Post
     * @param position       int
     * @param inPostKey      String
     */
    private void setupPost(final PostViewHolder postViewHolder, final Post post, final int position,
                           final String inPostKey) {
        postViewHolder.setText(post.getText());

        final String postKey;
        if (mAdapter instanceof FirebaseRecyclerAdapter) {
            postKey = ((FirebaseRecyclerAdapter) mAdapter).getRef(position).getKey();
        } else {
            postKey = inPostKey;
        }

        Author author = post.getAuthor();
        postViewHolder.setAuthor(author.getUserName(), author.getUid());
        postViewHolder.setIcon(author.getProfile_picture(), author.getUid());
        postViewHolder.setPicture(post.getPicture());


        ValueEventListener likeListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                postViewHolder.setNumLikes(dataSnapshot.getChildrenCount());
                if (dataSnapshot.hasChild(FirebaseUtil.getCurrentUserId())) {
                    postViewHolder.setLikeStatus(PostViewHolder.LikeStatus.LIKED, getActivity());
                } else {
                    postViewHolder.setLikeStatus(PostViewHolder.LikeStatus.NOT_LIKED, getActivity());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        FirebaseUtil.getLikesRef().child(postKey).addValueEventListener(likeListener);
        postViewHolder.mLikeListener = likeListener;

        postViewHolder.setPostClickListener(new PostViewHolder.PostClickListener() {
            @Override
            public void showComments() {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user == null || user.isAnonymous()) {
                    Toast.makeText(getActivity(), "You must sign-in to make comments.",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                mListener.onPostComment(postKey);
            }

            @Override
            public void toggleLike() {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user == null || user.isAnonymous()) {
                    Toast.makeText(getActivity(), "You must sign-in to like this post.",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                mListener.onPostLike(postKey);
            }

            @Override
            public void postDetail() {
                mListener.onPostItem(postKey);
            }
        });
    }

    @Override
    public void onDestroy() {

        super.onDestroy();
        if (mAdapter != null && mAdapter instanceof FirebaseRecyclerAdapter) {
            ((FirebaseRecyclerAdapter) mAdapter).cleanup();
        }
    }


    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save currently selected layout manager.
        int recyclerViewScrollPosition = getRecyclerViewScrollPosition();
        Log.d(TAG, "Recycler view scroll position: " + recyclerViewScrollPosition);
        savedInstanceState.putSerializable(KEY_LAYOUT_POSITION, recyclerViewScrollPosition);
        super.onSaveInstanceState(savedInstanceState);
    }

    private int getRecyclerViewScrollPosition() {
        int scrollPosition = 0;

        if (mRecyclerView != null && mRecyclerView.getLayoutManager() != null) {
            scrollPosition = ((LinearLayoutManager) mRecyclerView.getLayoutManager())
                    .findFirstCompletelyVisibleItemPosition();
        }
        return scrollPosition;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface OnPostSelectedListener {
        void onPostComment(String postKey);

        void onPostLike(String postKey);

        void onPostItem(String postKey);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnPostSelectedListener) {
            mListener = (OnPostSelectedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnPostSelectedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

}
