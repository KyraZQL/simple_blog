package edu.illinois.finalproject;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class PostViewHolder extends RecyclerView.ViewHolder {

    private PostClickListener mListener;


    public enum LikeStatus { LIKED, NOT_LIKED }
    private ImageView mLikeIcon;
    private static final int POST_TEXT_MAX_LINES = 6;
    public ImageView mIconView;
    public TextView mAuthorView;
    public TextView mPostTextView;
    public TextView mTimestampView;
    public TextView mNumLikesView;
    public ImageView mPictureView;
    public String mPostKey;
    public ValueEventListener mLikeListener;

    public PostViewHolder(View itemView) {
        super(itemView);


        mIconView = (ImageView) itemView.findViewById(R.id.post_author_icon);
        mAuthorView = (TextView) itemView.findViewById(R.id.post_author_name);
        mPostTextView = (TextView) itemView.findViewById(R.id.post_text);
        mTimestampView = (TextView) itemView.findViewById(R.id.post_timestamp);
        mNumLikesView = (TextView) itemView.findViewById(R.id.post_num_likes);
        mPictureView = (ImageView) itemView.findViewById(R.id.post_picture);

        itemView.findViewById(R.id.post_comment_icon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.showComments();
            }
        });
        itemView.findViewById(R.id.post_detail).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.postDetail();

                if (mPostKey != null) {
                    Context context = v.getContext();
                    Intent postDetailIntent = new Intent(context, PostDetailActivity.class);
                    postDetailIntent.putExtra(PostDetailActivity.POST_KEY_EXTRA,
                            mPostKey);
                    context.startActivity(postDetailIntent);
                }
            }
        });
        mLikeIcon = (ImageView) itemView.findViewById(R.id.post_like_icon);
        mLikeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.toggleLike();
            }
        });
    }



    public void setIcon(String url, final String authorId) {
        GlideUtil.loadProfileIcon(url, mIconView);
        mIconView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showUserDetail(authorId);
            }
        });
    }

    public void setPicture(String url) {
        if (url == null) {
            mPictureView.setVisibility(View.GONE);
        } else {
            mPictureView.setVisibility(View.VISIBLE);
            GlideUtil.loadImage(url, mPictureView);
        }
    }

    public void setAuthor(String author, final String authorId) {
        if (author == null || author.isEmpty()) {
            author = itemView.getResources().getString(R.string.user_info_no_name);
        }
        mAuthorView.setText(author);
        mAuthorView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showUserDetail(authorId);
            }
        });
    }

    /**
     * This method will open the current user's detail page.
     * @param authorId String
     */
    private void showUserDetail(String authorId) {
        Context context = itemView.getContext();
        Intent userDetailIntent = new Intent(context, UserDetailActivity.class);
        userDetailIntent.putExtra(UserDetailActivity.USER_ID_EXTRA_NAME, authorId);
        context.startActivity(userDetailIntent);
    }


    public void setText(final String text) {
        if (text == null || text.isEmpty()) {
            mPostTextView.setVisibility(View.GONE);
            return;
        } else {
            mPostTextView.setVisibility(View.VISIBLE);
            mPostTextView.setText(text);
            mPostTextView.setMaxLines(POST_TEXT_MAX_LINES);
            mPostTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mPostTextView.getMaxLines() == POST_TEXT_MAX_LINES) {
                        mPostTextView.setMaxLines(Integer.MAX_VALUE);
                    } else {
                        mPostTextView.setMaxLines(POST_TEXT_MAX_LINES);
                    }
                }
            });
        }
    }

    public void setNumLikes(long numLikes) {
        String suffix = numLikes == 1 ? " like" : " likes";
        mNumLikesView.setText(numLikes + suffix);
    }

    public void setPostClickListener(PostClickListener listener) {
        mListener = listener;
    }

    public void setLikeStatus(LikeStatus status, Context context) {

        mLikeIcon.setImageResource(status == LikeStatus.LIKED ? R.drawable.heart_full : R.drawable.heart_empty);
    }

    public interface PostClickListener {
        void showComments();
        void toggleLike();
        void postDetail();
    }
}