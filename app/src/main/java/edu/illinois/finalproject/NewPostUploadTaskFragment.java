package edu.illinois.finalproject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;

import edu.illinois.finalproject.Models.Author;
import edu.illinois.finalproject.Models.Post;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NewPostUploadTaskFragment extends Fragment {
    private static final String TAG = "NewPostTaskFragment";

    public interface TaskCallbacks {
        void onPostUploaded(String error);
    }

    private Context mApplicationContext;

    private Bitmap selectedBitmap;

    private TaskCallbacks mCallbacks;

    public NewPostUploadTaskFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Retain this fragment across config changes.
        setRetainInstance(true);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof TaskCallbacks) {
            mCallbacks = (TaskCallbacks) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement TaskCallbacks");
        }
        mApplicationContext = context.getApplicationContext();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }


    public void loadPost(Bitmap inSelectedBitmap, String inPostText) {

        UploadPostTask uploadTask = new UploadPostTask(inSelectedBitmap, inPostText);
        uploadTask.execute();
    }

    class UploadPostTask extends AsyncTask<Void, Void, Void> {
        private WeakReference<Bitmap> bitmapReference;
        private String postText;

        public UploadPostTask(Bitmap inSelectedBitmap, String inPostText) {

            bitmapReference = new WeakReference<Bitmap>(inSelectedBitmap);
            postText = inPostText;

        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected Void doInBackground(Void... params) {

            final Author author = FirebaseUtil.getAuthor();
            if (author == null) {
                Log.e(TAG, "Couldn't upload post: Couldn't get signed in user.");
                mCallbacks.onPostUploaded(mApplicationContext.getString(
                        R.string.error_user_not_signed_in));
                return null;
            }

            final Bitmap bitmap = bitmapReference.get();

            //If the user doesn't choose a picture, we will upload a post without picture.
            if (bitmap == null) {

                final FirebaseDatabase database = FirebaseDatabase.getInstance();
                final DatabaseReference databaseRef = database.getReference();

                Post newPost = new Post(author, postText, ServerValue.TIMESTAMP);

                final String key = databaseRef.child("posts").push().getKey();

                final Map<String, Object> postValues = newPost.toMap();

                Map<String, Object> childUpdates = new HashMap<>();
                childUpdates.put("/posts/" + key, postValues);

                databaseRef.child("people").child(author.getUid()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        DatabaseReference authorRef = snapshot.getRef();
                        authorRef.child("posts").child(key).setValue(true);
                    }

                    @Override
                    public void onCancelled(DatabaseError firebaseError) {
                        Log.e("Read failed", firebaseError.getMessage());
                    }
                });

                databaseRef.updateChildren(childUpdates, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError firebaseError, DatabaseReference databaseReference) {
                        if (firebaseError == null) {
                            mCallbacks.onPostUploaded(null);
                        } else {
                            Log.e(TAG, "Unable to create new post: " + firebaseError.getMessage());
                            FirebaseCrash.report(firebaseError.toException());
                            mCallbacks.onPostUploaded(mApplicationContext.getString(
                                    R.string.error_upload_task_create));
                        }
                    }
                });
                return null;
            } else {
                // If the user chooses a picture, we will upload this picture to firebase storage and
                // get a url at first, and then upload a post with the url.

                FirebaseStorage storageRef = FirebaseStorage.getInstance();
                StorageReference photoRef = storageRef.getReference();

                Log.i(TAG, "doInBackground: receive " + photoRef);

                final Long timestamp = System.currentTimeMillis();
                final StorageReference bitmapRef = photoRef.child(FirebaseUtil.getCurrentUserId())
                        .child(timestamp.toString()).child(UUID.randomUUID() + ".jpg");
                Log.d(TAG, bitmapRef.toString());

                ByteArrayOutputStream fullSizeStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fullSizeStream);
                byte[] bytes = fullSizeStream.toByteArray();
                bitmapRef.putBytes(bytes).
                        addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                final Uri bitmapUrl = taskSnapshot.getDownloadUrl();
                                Log.i(TAG, "onSuccess: " + '\n' + bitmapUrl.toString());
                                final FirebaseDatabase database = FirebaseDatabase.getInstance();
                                final DatabaseReference databaseRef = database.getReference();

                                Post newPost = new Post(author, postText, ServerValue.TIMESTAMP,
                                        bitmapUrl.toString());

                                final String key = databaseRef.child("posts").push().getKey();

                                final Map<String, Object> postValues = newPost.toMap();

                                Map<String, Object> childUpdatesWithPicture = new HashMap<>();

                                childUpdatesWithPicture.put("/posts/" + key, postValues);

                                databaseRef.child("people").child(author.getUid())
                                        .addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot snapshot) {
                                                DatabaseReference authorRef = snapshot.getRef();
                                                authorRef.child("posts").child(key).setValue(true);
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError firebaseError) {
                                                Log.e("Read failed", firebaseError.getMessage());
                                            }
                                        });

                                databaseRef.updateChildren(childUpdatesWithPicture
                                        , new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(DatabaseError firebaseError, DatabaseReference databaseReference) {
                                        if (firebaseError == null) {
                                            mCallbacks.onPostUploaded(null);
                                        } else {
                                            Log.e(TAG, "Unable to create new post: " + firebaseError.getMessage());
                                            FirebaseCrash.report(firebaseError.toException());
                                            mCallbacks.onPostUploaded(mApplicationContext.getString(
                                                    R.string.error_upload_task_create));
                                        }

                                    }
                                }
                                );
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                FirebaseCrash.logcat(Log.ERROR, TAG, "Failed to upload post to database.");
                                FirebaseCrash.report(e);
                                mCallbacks.onPostUploaded(mApplicationContext.getString(
                                        R.string.error_upload_task_create));
                            }
                        });
                return null;
            }
        }

    }

    public void setSelectedBitmap(Bitmap bitmap) {
        this.selectedBitmap = bitmap;
    }

    public Bitmap getSelectedBitmap() {
        return selectedBitmap;
    }

}

