package edu.illinois.finalproject;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;


public class NewPostActivity extends BaseActivity
        implements EasyPermissions.PermissionCallbacks,
        NewPostUploadTaskFragment.TaskCallbacks {
    public static final String TAG = "edu.illinois.finalproject.NewPostActivity";
    public static final String TAG_TASK_FRAGMENT = "newPostUploadTaskFragment";

    private static final int TC_PICK_IMAGE = 101;
    private static final int RC_CAMERA_PERMISSIONS = 102;

    private static final String[] cameraPerms = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };
    private Uri mFileUri;
    private ImageView mImageView;
    private EditText editText;

    private Bitmap selectedBitmap;


    private NewPostUploadTaskFragment mTaskFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar_newpost);
        myToolbar.setTitle("New Post");
        setSupportActionBar(myToolbar);
        myToolbar.setNavigationIcon(R.mipmap.ic_clear_white_24dp);
        myToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        // find the retained fragment on activity restarts
        FragmentManager fm = getSupportFragmentManager();
        mTaskFragment = (NewPostUploadTaskFragment) fm.findFragmentByTag(TAG_TASK_FRAGMENT);

        // create the fragment and data the first time
        if (mTaskFragment == null) {
            // add the fragment
            mTaskFragment = new NewPostUploadTaskFragment();
            fm.beginTransaction().add(mTaskFragment, TAG_TASK_FRAGMENT).commit();
        }
        editText = (EditText) findViewById(R.id.new_post_text);
        editText.setHint("Enter your new post.");
        mImageView = (ImageView) findViewById(R.id.new_post_picture);

        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImagePicker();
            }
        });
        if (selectedBitmap != null) {
            mImageView.setImageBitmap(selectedBitmap);
        }
        selectedBitmap = mTaskFragment.getSelectedBitmap();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_new_post, menu);
        return true;
    }

    @Override
    public void onPostUploaded(final String error) {
        NewPostActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dismissProgressDialog();
                if (error == null) {
                    Toast.makeText(NewPostActivity.this, "Post created!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(NewPostActivity.this, error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_submit) {
            final EditText descriptionText = (EditText) findViewById(R.id.new_post_text);

            String postText = descriptionText.getText().toString();
            if (TextUtils.isEmpty(postText)) {
                descriptionText.setError(getString(R.string.error_required_field));
                return false;
            }
            showProgressDialog(getString(R.string.post_upload_progress_message));

            mTaskFragment.loadPost(selectedBitmap, postText);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @AfterPermissionGranted(RC_CAMERA_PERMISSIONS)
    private void showImagePicker() {
        // Check for camera permissions
        if (!EasyPermissions.hasPermissions(this, cameraPerms)) {
            EasyPermissions.requestPermissions(this,
                    "This sample will upload a picture from your Camera",
                    RC_CAMERA_PERMISSIONS, cameraPerms);
            return;
        }

        // Choose file storage location
        File file = new File(getExternalCacheDir(), UUID.randomUUID().toString());
        mFileUri = Uri.fromFile(file);

        // Camera
        final List<Intent> cameraIntents = new ArrayList<Intent>();
        final Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        final PackageManager packageManager = getPackageManager();
        final List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
        for (ResolveInfo res : listCam) {
            final String packageName = res.activityInfo.packageName;
            final Intent intent = new Intent(captureIntent);
            intent.setComponent(new ComponentName(packageName, res.activityInfo.name));
            intent.setPackage(packageName);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, mFileUri);
            cameraIntents.add(intent);
        }

        // Image Picker
        Intent pickerIntent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        Intent chooserIntent = Intent.createChooser(pickerIntent,
                getString(R.string.picture_chooser_title));
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, cameraIntents.toArray(new
                Parcelable[cameraIntents.size()]));
        startActivityForResult(chooserIntent, TC_PICK_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == TC_PICK_IMAGE) {
            if (resultCode == Activity.RESULT_OK) {
                final boolean isCamera;
                if (data.getData() == null) {
                    isCamera = true;
                } else {
                    isCamera = MediaStore.ACTION_IMAGE_CAPTURE.equals(data.getAction());
                }
                if (!isCamera) {
                    mFileUri = data.getData();
                }
                Log.d(TAG, "Received file uri: " + mFileUri.getPath());
                try {
                    Bitmap imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), mFileUri);
                    mImageView.setImageBitmap(imageBitmap);
                    selectedBitmap = imageBitmap;
                } catch (IOException e) {
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        // store the data in the fragment
        if (selectedBitmap != null) {
            mTaskFragment.setSelectedBitmap(selectedBitmap);
            Log.i(TAG, "onDestroy: get the bitmap");
        }

        super.onDestroy();
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {

    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {

    }

}
