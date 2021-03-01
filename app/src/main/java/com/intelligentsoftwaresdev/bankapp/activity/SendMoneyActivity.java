package com.intelligentsoftwaresdev.bankapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.intelligentsoftwaresdev.bankapp.R;
import com.intelligentsoftwaresdev.bankapp.databinding.ActivityMoneySendBinding;

import java.util.HashMap;
import java.util.Map;

import utils.Tools;


public class SendMoneyActivity extends AppCompatActivity {
    ActivityMoneySendBinding b;

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private String TAG = "";
    private String balance;
    private String dailyLimit;

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = DataBindingUtil.setContentView(this, R.layout.activity_money_send);
        initComponent();
        initToolbar();
        initButtonActions();
        getBalanceFirestore();

    }

    private void initButtonActions() {
        b.btSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMoney();
            }
        });
    }


    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.maintoolbar);
        toolbar.setNavigationIcon(R.drawable.ic_home);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Send Money");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Tools.setSystemBarColor(this);
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(this, LoginActivity.class));
        finish();

    }

    private void initComponent() {


        (findViewById(R.id.bank)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BankDialog(v);
            }
        });

    }

    private void BankDialog(final View v) {
        final String[] array = new String[]{
                "Bank Rakyat", "CIMB Bank", "Public Bank", "RHB Bank", "Alliance Bank", "Bank Islam", "Maybank", "Hong Leong Bank"
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Bank");
        builder.setSingleChoiceItems(array, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                ((EditText) v).setText(array[i]);
                dialogInterface.dismiss();
            }
        });
        builder.show();
    }

    private void sendMoney() {
        String amount = b.amount.getText().toString().trim();
        String accountNumber = b.accountNumber.getText().toString().trim();
        String bank = b.bank.getText().toString().trim();
        if (TextUtils.isEmpty(amount)) {
            b.amount.setError("Amount is required");
        } else if (TextUtils.isEmpty(bank)) {
            b.bank.setError("Bank  is required");
        } else if (TextUtils.isEmpty(bank)) {
            b.accountNumber.setError("Account Number is required");
        } else {
            DocumentReference documentReference = db.collection("transactions").document(mAuth.getUid());
            Map<String, Object> transaction = new HashMap<>();
            transaction.put("type", "sendmoney");
            transaction.put("amount", amount);
            transaction.put("bank", bank);
            transaction.put("accountNumber", accountNumber);
            documentReference.set(transaction).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {

                    updateBalance(amount);
                }
            });

        }


    }


    private void updateBalance(String amount) {
//convert the Strings to

        Log.e(TAG, "Balance: " + balance);
        Integer intBalance = Integer.parseInt(balance);
        Integer inAmount = Integer.parseInt(amount);
        Integer indailyLimit = Integer.parseInt(dailyLimit);
        Log.e(TAG, "DailyLimit: " + indailyLimit);
        Log.e(TAG, "Amount: " + inAmount);
        if( inAmount>= indailyLimit){
            Toast.makeText(this, "You cannot Transact as you have exceeded your Daily Limit", Toast.LENGTH_SHORT).show();
        } else {
            if (inAmount > intBalance) {
                Toast.makeText(this, "You have Insufficient Balance", Toast.LENGTH_LONG).show();
            } else {
//            computations
                Integer intnewbalance = intBalance - inAmount;
                String strNewBalance = intnewbalance.toString();
//            update Balance in Firestore
                Map<String, Object> user = new HashMap<>();
                user.put("balance", strNewBalance);
                db.collection("users").document(mAuth.getUid())
                        .set(user, SetOptions.merge());
                Log.e(TAG, "New Balance: " + strNewBalance);

                startActivity(new Intent(SendMoneyActivity.this, VerificationActivity.class));
            }
        }


    }

    private void getBalanceFirestore() {

        DocumentReference docRef = db.collection("users").document(mAuth.getUid());
        Log.e(TAG, "UserID" + mAuth.getUid());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Map<String, Object> userMap = (Map<String, Object>) document.getData();
                        balance = (String) userMap.get("balance");
                        dailyLimit = (String) userMap.get("dailyLimit");
                        Log.e(TAG, "Balance: " + balance);


                    } else {
                        Log.e(TAG, "No such document");
                    }
                } else {
                    Log.e(TAG, "get failed with ", task.getException());
                }
            }
        });


    }


//    Menu Components

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.basic, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {

            startActivity(new Intent(SendMoneyActivity.this, MainActivity.class));
            finish();
            Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show();
        } else if (item.getItemId() == R.id.account) {
            Toast.makeText(this, "Account", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(SendMoneyActivity.this, AccountActivity.class));

        } else if (item.getItemId() == R.id.logout) {
            logout();
            Toast.makeText(this, "Logout", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }


}