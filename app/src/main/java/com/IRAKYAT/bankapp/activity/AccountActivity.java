package com.IRAKYAT.bankapp.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.IRAKYAT.bankapp.R;
import com.IRAKYAT.bankapp.databinding.ActivityAccountBinding;

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
            String phone = "+60"+b.phone.getText().toString().trim();
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
                Intent intent = new Intent(AccountActivity.this, Verification2Activity.class);
                intent.putExtra("password", password);
                intent.putExtra("cpassword", cpassword);
                intent.putExtra("phone", phone);
                intent.putExtra("dailyLimit", dailyLimit);
                intent.putExtra("email", email);
                startActivityForResult(intent, 1);

                    Toast.makeText(this, "Kindly Verify To Update", Toast.LENGTH_SHORT).show();

            }


        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e(TAG, "onActivityResult: Called");
        if (resultCode == RESULT_OK) {
            Log.e(TAG, "onActivityResult: Result Okay");

            if (data != null) {
                Log.e(TAG, "onActivityResult: data " + data);
                String password = data.getStringExtra("password");
                String cpassword = data.getStringExtra("cpassword");
                String phone = data.getStringExtra("phone");
                String dailyLimit = data.getStringExtra("dailyLimit");
                String email = data.getStringExtra("email");
                waitingForResult(password, cpassword, phone, dailyLimit, email);
            }
        }
        if (resultCode == RESULT_CANCELED) {
            Log.e(TAG, "onActivityResult: Result Cancelled");
            Toast.makeText(this, "Authentication Failed", Toast.LENGTH_SHORT).show();
        }

    }


    private void waitingForResult(String password, String cpassword, String phone, String dailyLimit, String email) {

        Log.e(TAG, "waitingForResult: Called" + email);

        if (!password.isEmpty() && !cpassword.isEmpty()) {
            if (password.equals(cpassword)) {
                float passStrength = getRating(password);
                Log.e(TAG, "password Strength " + passStrength);
                if (passStrength < 4.0) {

                    b.password.setError("Kindly Use a Password with Alphanumerics and Special Characters");

                    Toast.makeText(this, "Kindly Use a Password with Alphanumerics and Special Characters", Toast.LENGTH_LONG).show();
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
                    user.updatePassword(password)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Log.e(TAG, "Password Updated");
                                    }
                                }
                            });
                    //Updating a user Data Firestore
                    Map<String, Object> updatedUser = new HashMap<>();
                    updatedUser.put("email", email);
                    updatedUser.put("dailyLimit", dailyLimit);
                    updatedUser.put("phone", phone);
                    db.collection("users").document(mAuth.getUid())
                            .set(updatedUser, SetOptions.merge());
                    updateUI(email, phone, dailyLimit);

//                    startActivity(new Intent(AccountActivity.this, MainActivity.class));
                    finish();
                }

            } else {

                Toast.makeText(this, "Passwords Do not Match", Toast.LENGTH_LONG).show();
                return;
            }

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

            //Updating a user Data Firestore
            Map<String, Object> updatedUser = new HashMap<>();
            updatedUser.put("email", email);
            updatedUser.put("dailyLimit", dailyLimit);
            updatedUser.put("phone", phone);
            db.collection("users").document(mAuth.getUid())
                    .set(updatedUser, SetOptions.merge());
            updateUI(email, phone, dailyLimit);

//            startActivity(new Intent(AccountActivity.this, MainActivity.class));
            finish();

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

    private void initComponent() {


        (findViewById(R.id.limit)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLimitDialog(v);
            }
        });
        (findViewById(R.id.wlimit)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showWLimitDialog(v);
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

    private void showWLimitDialog(final View v) {
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
        b.phone.setText(phone.substring(3));

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
            finish();
        } else if (item.getItemId() == R.id.account) {
            Toast.makeText(this, "You are in Accounts", Toast.LENGTH_SHORT).show();

        } else if (item.getItemId() == R.id.logout) {
            logout();
            Toast.makeText(this, "Logout", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

}