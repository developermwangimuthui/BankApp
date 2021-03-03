package com.intelligentsoftwaresdev.bankapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.intelligentsoftwaresdev.bankapp.R;
import com.intelligentsoftwaresdev.bankapp.databinding.ActivitySignUPBinding;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import utils.Tools;


public class SignUpActivity extends AppCompatActivity {
    ActivitySignUPBinding b;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    ;
    private String TAG = "";
    private String userId = "";
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        b = DataBindingUtil.setContentView(this, R.layout.activity_sign_u_p);
        initButtons();

    }

    private void initButtons() {
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
                startActivity(new Intent(SignUpActivity.this, LoginActivity.class));

            }
        });
        b.forgotPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignUpActivity.this, ForgotPasswordActivity.class));
            }
        });
    }


    private void signupUser() {
        long number = (long) Math.floor(Math.random() * 9_000_000_000L) + 1_000_000_000L;
        String balance = "10000.00";
        String accountNumber = String.valueOf(number);
        String username = b.signInUsername.getText().toString().trim();
        String phone ="+60"+b.signInPhone.getText().toString().trim();
//        String accountNumber = b.signInAccountNumber.getText().toString().trim();
        String nric = b.signInNric.getText().toString().trim();
        String nickname = b.signInNickname.getText().toString().trim();
        String footBallTeam = b.footballTeam.getText().toString().trim();
        String city = b.city.getText().toString().trim();
        String email = b.signInEmail.getText().toString().trim();
        String password = b.signInPassword.getText().toString().trim();
        String cpassword = b.cpassword.getText().toString().trim();
        if (TextUtils.isEmpty(username)) {
            b.signInUsername.setError("username is required");
            b.signInUsername.requestFocus();
        } else if (TextUtils.isEmpty(email)) {
            b.signInEmail.setError("Email is required");
            b.signInEmail.requestFocus();
        } else if (TextUtils.isEmpty(phone)) {
            b.signInPhone.setError("Phone is Required");
            b.signInPhone.requestFocus();
        } else if (TextUtils.isEmpty(nric)) {
            b.signInNric.setError("NRIC is Required");
            b.signInNric.requestFocus();
        } else if (TextUtils.isEmpty(nickname)) {
            b.signInNickname.setError("Nickname is Required");
            b.signInNickname.requestFocus();
        } else if (TextUtils.isEmpty(footBallTeam)) {
            b.footballTeam.setError("Football Team is Required");
            b.footballTeam.requestFocus();
        } else if (TextUtils.isEmpty(city)) {
            b.city.setError("City is Required");
            b.city.requestFocus();
        } else if (TextUtils.isEmpty(password)) {
            b.signInPassword.setError("Password is Required");
            b.signInPassword.requestFocus();
        } else if (TextUtils.isEmpty(cpassword)) {
            b.cpassword.setError("Confirm Password is Required");
            b.cpassword.requestFocus();
        } else {

            if (!password.isEmpty() && !cpassword.isEmpty()) {
                if (password.equals(cpassword)) {

                    float passStrength = getRating(password);
                    Log.e(TAG, "password Strength " + passStrength);
                    if (passStrength < 4.0) {

                        b.signInPassword.setError("Kindly Use a Password with Alphanumerics and Special Characters");

                        Toast.makeText(this, "Kindly Use a Password with Alphanumerics and Special Characters", Toast.LENGTH_LONG).show();
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
                                            user.put("username", username);
                                            user.put("phone", phone);
                                            user.put("dailyLimit", "1000");
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
                                                    Log.e(TAG, "onSuccess: User created succesfully");
                                                    Toast.makeText(SignUpActivity.this, "Account  Created Succesfully", Toast.LENGTH_SHORT).show();

                                                }
                                            });


                                            Log.e(TAG, "createUserWithEmail:success");
                                            FirebaseUser thisuser = mAuth.getCurrentUser();
                                            updateUI(thisuser);
                                            startActivity(new Intent(SignUpActivity.this, VerificationActivity.class));
                                        } else {
                                            // If sign in fails, display a message to the user.
                                            Log.e(TAG, "createUserWithEmail:failure", task.getException());
                                            Toast.makeText(SignUpActivity.this, "Email Already In Use", Toast.LENGTH_SHORT).show();
                                            updateUI(null);
                                        }

                                        // ...
                                    }
                                });
                    }
                } else {

                    Toast.makeText(this, "Passwords Do not Match", Toast.LENGTH_LONG).show();
                    return;

                }
            }


        }
    }


    private float getRating(String password) throws IllegalArgumentException {
        if (password == null) {
            throw new IllegalArgumentException();
        }
        int passwordStrength = 0;
        if (password.length() > 5) {
            passwordStrength++;
        } // minimal pw length of 6
        if (password.toLowerCase() != password) {
            passwordStrength++;
        } // lower and upper case
        if (password.length() > 8) {
            passwordStrength++;
        } // good pw length of 9+
        int numDigits = Tools.getNumberDigits(password);
        if (numDigits > 0 && numDigits != password.length()) {
            passwordStrength++;
        } // contains digits and non-digits
        return (float) passwordStrength;
    }

    //Change UI according to user data.
    public void updateUI(FirebaseUser user) {

        if (user != null) {
            Toast.makeText(this, "Signed In successfully", Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, VerificationActivity.class));

        } else {
            Toast.makeText(this, "Email Already In Use", Toast.LENGTH_LONG).show();
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