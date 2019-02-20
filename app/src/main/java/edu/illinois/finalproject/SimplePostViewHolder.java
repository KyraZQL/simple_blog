package edu.illinois.finalproject;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by kyraz on 12/11/2017.
 */

public class SimplePostViewHolder extends RecyclerView.ViewHolder {

    public ImageView mIconView;
    public TextView mAuthorView;
    public TextView mPostTextView;
    public TextView mTimestampView;
    public ImageView mDeletePostView;


    public SimplePostViewHolder(View itemView) {
        super(itemView);


        mIconView = (ImageView) itemView.findViewById(R.id.post_author_icon);
        mAuthorView = (TextView) itemView.findViewById(R.id.post_author_name);
        mPostTextView = (TextView) itemView.findViewById(R.id.post_text);
        mTimestampView = (TextView) itemView.findViewById(R.id.post_timestamp);
        mDeletePostView = (ImageView) itemView.findViewById(R.id.delete_post);
    }

}
