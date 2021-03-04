package com.IRAKYAT.bankapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.IRAKYAT.bankapp.R;
import com.IRAKYAT.bankapp.databinding.ActivityForgotPasswordBinding;

public class ForgotPasswordActivity extends AppCompatActivity {
    ActivityForgotPasswordBinding b;
   private static String  TAG="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = DataBindingUtil.setContentView(this, R.layout.activity_forgot_password);
        initButton();
    }

    private void initButton() {
        b.signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = b.email.getText().toString().trim();
                if (email.isEmpty()) {
                    b.email.setError("Email is required");
                    b.email.requestFocus();

                }else {
                    FirebaseAuth.getInstance().sendPasswordResetEmail(b.email.getText().toString().trim())
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Log.e(TAG, "Email sent.");
                                        Toast.makeText(ForgotPasswordActivity.this, "Password Reset Link send to your Email", Toast.LENGTH_SHORT).show();
                                    }else{
                                        Log.e(TAG, "Failed no Email ");
                                        Toast.makeText(ForgotPasswordActivity.this, "Email not Found ", Toast.LENGTH_SHORT).show();
                                    }
                                }

                            });
                }
            }
        });
    }

}