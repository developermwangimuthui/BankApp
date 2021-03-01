package com.intelligentsoftwaresdev.bankapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.intelligentsoftwaresdev.bankapp.R;
import com.intelligentsoftwaresdev.bankapp.databinding.ActivityAccountBinding;
import com.intelligentsoftwaresdev.bankapp.databinding.ActivityMainBinding;

import java.util.HashMap;
import java.util.Map;

import utils.Tools;

public class AccountActivity extends AppCompatActivity {

    private String userId;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private static final String TAG = "Account";
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    ActivityAccountBinding b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = DataBindingUtil.setContentView(this, R.layout.activity_account);
        initComponent();
        initToolbar();
        getUserFirestore();
        initButtons();
    }

    private void initButtons() {

        b.emailSignInButton.setOnClickListener(v -> {
            String email = b.email.getText().toString().trim();
            String phone = b.phone.getText().toString().trim();
            String dailyLimit = b.limit.getText().toString().trim();
            String password = b.password.getText().toString().trim();
            String cpassword = b.cpassword.getText().toString().trim();
            if (phone.isEmpty()) {
                b.phone.setError("Phone is required");
                b.phone.requestFocus();
            } else if (email.isEmpty()) {
                b.email.setError("Email is required");
                b.email.requestFocus();

            } else if (dailyLimit.isEmpty()) {
                b.limit.setError("Choose Your Daily Limit");
                b.limit.requestFocus();
            } else {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                user.updateEmail(email)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.e(TAG, "User email address updated.");
                                }
                            }
                        });
                if (!password.isEmpty() && !cpassword.isEmpty()) {
                    if (password == cpassword) {
                        b.password.setError("Passwords do not Match");
                        b.cpassword.setError("Passwords do not Match");
                        Toast.makeText(this, "Passwords do not Match", Toast.LENGTH_SHORT).show();
                    } else {
                        user.updatePassword(password)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Log.e(TAG, "Password Updated");
                                        }
                                    }
                                });
                    }
                }


                //Updating a user Data Firestore
                Map<String, Object> updatedUser = new HashMap<>();
                updatedUser.put("email", email);
                updatedUser.put("dailyLimit", dailyLimit);
                updatedUser.put("phone", phone);
                db.collection("users").document(mAuth.getUid())
                        .set(updatedUser, SetOptions.merge());
                updateUI(email,phone,dailyLimit);


            }
            Toast.makeText(this, "Kindly Verify To Update", Toast.LENGTH_SHORT).show();

        });
    }

    private void initComponent() {


        (findViewById(R.id.limit)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLimitDialog(v);
            }
        });

    }


    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.maintoolbar);
        toolbar.setNavigationIcon(R.drawable.ic_home);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Accounts");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Tools.setSystemBarColor(this);
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(this, LoginActivity.class));
        finish();

    }

    private void showLimitDialog(final View v) {
        final String[] array = new String[]{
                "1000", "2000", "3000"
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Daily Limit");
        builder.setSingleChoiceItems(array, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                ((EditText) v).setText(array[i]);
                dialogInterface.dismiss();
            }
        });
        builder.show();
    }


    private void getUserFirestore() {

        DocumentReference docRef = db.collection("users").document(mAuth.getUid());
        Log.e(TAG, "UserID" + mAuth.getUid());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Map<String, Object> userMap = (Map<String, Object>) document.getData();
                        String email = (String) userMap.get("email");
                        String phone = (String) userMap.get("phone");
                        String dailyLimit = (String) userMap.get("dailyLimit");
                        updateUI(email, phone, dailyLimit);
                        Log.e(TAG, "email: " + email);
                        Log.e(TAG, "phone: " + phone);
                        Log.e(TAG, "dailyLimit: " + dailyLimit);


                    } else {
                        Log.e(TAG, "No such document");
                    }
                } else {
                    Log.e(TAG, "get failed with ", task.getException());
                }
            }
        });


    }

    private void updateUI(String email, String phone, String dailLimit) {
        Log.e(TAG, "updateUI: Called");
        b.limit.setText(dailLimit);
        b.email.setText(email);
        b.phone.setText(phone);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.basic, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Toast.makeText(this, "Home ", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(AccountActivity.this, MainActivity.class));
        } else if (item.getItemId() == R.id.account) {
            Toast.makeText(this, "You are in Accounts", Toast.LENGTH_SHORT).show();

        } else if (item.getItemId() == R.id.logout) {
            logout();
            Toast.makeText(this, "Logout", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

}