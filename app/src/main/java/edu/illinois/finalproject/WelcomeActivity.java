package edu.illinois.finalproject;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class WelcomeActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "edu.illinois.finalproject.WelcomeActivity";
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        findViewById(R.id.sign_in_button).setOnClickListener(this);
        findViewById(R.id.explore_button).setOnClickListener(this);

        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.explore_button:
                mAuth.signInAnonymously().addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        Intent feedsIntent = new Intent(WelcomeActivity.this, FeedsActivity.class);
                        startActivity(feedsIntent);
                    }
                }).addOnFailureListener( new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(WelcomeActivity.this, "Unable to sign in anonymously.",
                                Toast.LENGTH_SHORT).show();
                            Log.e(TAG, e.getMessage());
                    }
                });
                break;
            case R.id.sign_in_button:
                Intent signInIntent = new Intent(this, ProfileActivity.class);
                startActivity(signInIntent);
                break;
        }
    }


}
