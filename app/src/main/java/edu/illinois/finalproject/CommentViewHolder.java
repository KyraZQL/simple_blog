package edu.illinois.finalproject;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;


/**
 * Created by kyraz on 12/5/2017.
 */

public class CommentViewHolder extends RecyclerView.ViewHolder {
    public final ImageView commentAuthorIcon;
    public final TextView commentText;
    public final TextView commentAuthor;
    public final TextView commentTime;


    public CommentViewHolder(View itemView) {
        super(itemView);
        commentAuthorIcon = (ImageView) itemView.findViewById(R.id.comment_author_icon);
        commentText = (TextView) itemView.findViewById(R.id.comment_text);
        commentAuthor = (TextView) itemView.findViewById(R.id.comment_name);
        commentTime = (TextView) itemView.findViewById(R.id.comment_time);
    }
}

