package edu.illinois.finalproject;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

public class BaseActivity extends AppCompatActivity {
    private static final String TAG_DIALOG_FRAGMENT = "tagDialogFragment";
    // code below derived from:
    // https://github.com/firebase/friendlypix-android/blob/master/app/src/
    // main/java/com/google/firebase/samples/apps/friendlypix/BaseActivity.java
    protected void showProgressDialog(String message) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment prev = getExistingDialogFragment();
        if (prev == null) {
            ProgressDialogFragment fragment = ProgressDialogFragment.newInstance(message);
            fragment.show(ft, TAG_DIALOG_FRAGMENT);
        }
    }

    protected void dismissProgressDialog() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment prev = getExistingDialogFragment();
        if (prev != null) {
            ft.remove(prev).commit();
        }
    }

    private Fragment getExistingDialogFragment() {
        return getSupportFragmentManager().findFragmentByTag(TAG_DIALOG_FRAGMENT);
    }
}