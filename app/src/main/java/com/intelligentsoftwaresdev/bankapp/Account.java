package com.intelligentsoftwaresdev.bankapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.intelligentsoftwaresdev.bankapp.databinding.ActivityAccountBinding;
import com.intelligentsoftwaresdev.bankapp.databinding.ActivityMainBinding;

import java.util.HashMap;
import java.util.Map;

public class Account extends AppCompatActivity {

    private String userId;
    private FirebaseAuth mAuth;
    private static final String TAG = "Account";
    FirebaseFirestore db;
    ActivityAccountBinding b;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b= DataBindingUtil.setContentView(this,R.layout.activity_account);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        b.emailSignInButton.setOnClickListener(v->{
            if (b.emailEd.getText().toString().trim().isEmpty()){
                b.emailEd.setError("fill this");
                b.emailEd.requestFocus();
            }else if (b.phone.getText().toString().trim().isEmpty()){
                b.phone.setError("fill this");
                b.phone.requestFocus();
            }else {
                // Create a new user with a first, middle, and last name

                DocumentReference documentReference = db.collection("users").document(mAuth.getUid());
                Map<String, Object> user = new HashMap<>();
                user.put("email", b.emailEd.getText().toString().trim());

                user.put("phone", b.phone.getText().toString().trim());
                user.put("password", b.password.getText().toString().trim());
// Add a new document with a generated ID
                documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "onSuccess: User updated succesfully");
                        Toast.makeText(Account.this, "User Created", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(Account.this,MainActivity.class));
                    }
                });
            }
        });
    }
}