package edu.illinois.finalproject;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.content.ContextCompat;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

public class GlideUtil {
    // code below derived from:
    // https://github.com/firebase/friendlypix-android/blob/master/app/src/
    // main/java/com/google/firebase/samples/apps/friendlypix/GlideUti.java
    public static void loadImage(String url, ImageView imageView) {
        Context context = imageView.getContext();
        ColorDrawable cd = new ColorDrawable(ContextCompat.getColor(context, R.color.blue_grey_500));
        Glide.with(context)
                .load(url)
                .placeholder(cd)
                .crossFade()
                .centerCrop()
                .into(imageView);
    }

    public static void loadProfileIcon(String url, ImageView imageView) {
        Context context = imageView.getContext();
        Glide.with(context)
                .load(url)
                .placeholder(R.drawable.ic_person_outline_black_24dp)
                .dontAnimate()
                .fitCenter()
                .into(imageView);
    }
}