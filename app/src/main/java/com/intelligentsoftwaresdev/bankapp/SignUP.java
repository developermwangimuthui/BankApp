package com.intelligentsoftwaresdev.bankapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.intelligentsoftwaresdev.bankapp.databinding.ActivitySignUPBinding;

import java.util.HashMap;
import java.util.Map;


public class SignUP extends AppCompatActivity {
    ActivitySignUPBinding b;
    private FirebaseAuth mAuth;
    private String TAG = "";
    private String userId = "";
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        b = DataBindingUtil.setContentView(this, R.layout.activity_sign_u_p);

        b.signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signupUser();
//                startActivity( new Intent(SignUP.this,Verification.class));
            }
        });
        b.clickToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignUP.this, Login.class));
            }
        });
    }


    private void signupUser() {
        Double balance = 3000.00;
        String username = b.signInUsername.getText().toString().trim();
        String phone = b.signInPhone.getText().toString().trim();
        String accountNumber = b.signInAccountNumber.getText().toString().trim();
        String nric = b.signInNric.getText().toString().trim();
        String nickname = b.signInNickname.getText().toString().trim();
        String footBallTeam = b.footballTeam.getText().toString().trim();
        String city = b.city.getText().toString().trim();
        String email = b.signInEmail.getText().toString().trim();
        String password = b.signInPassword.getText().toString().trim();
        if (TextUtils.isEmpty(username)) {
            b.signInUsername.setError("username is required");
        } else if (TextUtils.isEmpty(email)) {
            b.signInEmail.setError("Email is required");
        } else if (TextUtils.isEmpty(phone)) {
            b.signInPassword.setError("Phone is Required");
        } else if (TextUtils.isEmpty(accountNumber)) {
            b.signInPassword.setError("Account Number is Required");
        } else if (TextUtils.isEmpty(nric)) {
            b.signInPassword.setError("NRIC is Required");
        } else if (TextUtils.isEmpty(nickname)) {
            b.signInPassword.setError("Nickname is Required");
        } else if (TextUtils.isEmpty(footBallTeam)) {
            b.signInPassword.setError("Football Team is Required");
        } else if (TextUtils.isEmpty(city)) {
            b.signInPassword.setError("City is Required");
        } else if (TextUtils.isEmpty(password)) {
            b.signInPassword.setError("Password is Required");
        } else {

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
//                                get the user Id of the current User
                                userId = mAuth.getCurrentUser().getUid();
//                                register user details into firestore
                                DocumentReference documentReference = db.collection("users").document(userId);
                                Map<String, Object> user = new HashMap<>();
                                user.put("username", "Ada");
                                user.put("phone", phone);
                                user.put("accountNumber", accountNumber);
                                user.put("nric", nric);
                                user.put("nickname", nickname);
                                user.put("footBallTeam", footBallTeam);
                                user.put("city", city);
                                user.put("balance", balance);
                                user.put("email", email);
                                user.put("password", password);
                                documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d(TAG, "onSuccess: User created succesfully");
                                        Toast.makeText(SignUP.this, "User Created", Toast.LENGTH_SHORT).show();
                                    }
                                });


                                Log.d(TAG, "createUserWithEmail:success");
                                FirebaseUser thisuser = mAuth.getCurrentUser();
                                updateUI(thisuser);
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                Toast.makeText(SignUP.this, "Sign Up Failed", Toast.LENGTH_SHORT).show();
                                updateUI(null);
                            }

                            // ...
                        }
                    });


        }
    }

    //Change UI according to user data.
    public void updateUI(FirebaseUser user) {

        if (user != null) {
            Toast.makeText(this, "U Signed In successfully", Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, Verification.class));

        } else {
            Toast.makeText(this, "U Didnt signed in", Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

}