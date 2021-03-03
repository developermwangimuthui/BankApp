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
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.intelligentsoftwaresdev.bankapp.R;
import com.intelligentsoftwaresdev.bankapp.databinding.ActivityMoneySendBinding;
import com.intelligentsoftwaresdev.bankapp.models.TransactionModel;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import utils.Tools;


public class SendMoneyActivity extends AppCompatActivity {
    ActivityMoneySendBinding b;

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private String TAG = "";
    private String balance;
    private String dailyLimit;

    private ProgressBar progress_bar;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = DataBindingUtil.setContentView(this, R.layout.activity_money_send);

        progress_bar = (ProgressBar) findViewById(R.id.progress_bar);
        initComponent();
        initToolbar();
        initButtonActions();
        getBalanceFirestore();

    }

    private void initButtonActions() {
        b.btSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                progress_bar.setVisibility(View.VISIBLE);
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
        String referenceNote = b.referenceNote.getText().toString().trim();
        String beneficiary = b.beneficiary.getText().toString().trim();
        String type = "Fund Transfer";
        String company = "";


        if (TextUtils.isEmpty(amount)) {
            b.amount.setError("Amount is required");
        } else if (TextUtils.isEmpty(bank)) {
            b.bank.setError("Bank  is required");
        } else if (TextUtils.isEmpty(referenceNote)) {
            b.referenceNote.setError("Reference Note  is required");
        } else if (TextUtils.isEmpty(accountNumber)) {
            b.accountNumber.setError("Account Number is required");
        } else if (TextUtils.isEmpty(beneficiary)) {
            b.beneficiary.setError("Enter Beneficiary");
        } else {
            updateBalance(type, amount, bank, accountNumber, company, referenceNote,beneficiary);

        }


    }

    private void addDataToFirestore(String type, String amount, String bank, String accountNumber, String company, String
            referenceNote,String beneficiary) {
        CollectionReference collectionReference = db.collection("allTransaction").document(mAuth.getUid()).collection("transactions");
        // creating a collection reference
        // for our Firebase Firetore database.
        // adding our data to our courses object class.

        double sentAmount = Double.parseDouble(amount);
        DecimalFormat df = new DecimalFormat("#.00");
        String sentTwoDPAMount = df.format(sentAmount);
        Log.e(TAG, "Sent Amount "+sentTwoDPAMount );
        TransactionModel transactions = new TransactionModel(type, sentTwoDPAMount, bank, accountNumber, company, referenceNote,beneficiary);

        // below method is use to add data to Firebase Firestore.
        collectionReference.add(transactions).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                // after the data addition is successful
                // we are displaying a success toast message.
//                updateBalance(amount);

                Toast.makeText(SendMoneyActivity.this, "Kindly Verify to Continue", Toast.LENGTH_SHORT).show();

                progress_bar.setVisibility(View.GONE);
                startActivity(new Intent(SendMoneyActivity.this, VerificationActivity.class));
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // this method is called when the data addition process is failed.
                // displaying a toast message when data addition is failed.
                Toast.makeText(SendMoneyActivity.this, "Something Went Wrong\n" + e, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateBalance(String type,String amount,String bank,String accountNumber,String company,String referenceNote,String beneficiary) {
//convert the Strings to
        Log.e(TAG, "Balance: " + balance);
        double intBalance;
        intBalance = Double.parseDouble(balance);
        double indailyLimit = Double.parseDouble(dailyLimit);
        Log.e(TAG, "DailyLimit: " + indailyLimit);
        double inAmount = Double.parseDouble(amount);
        Log.e(TAG, "Amount: " + inAmount);
        if (inAmount >= indailyLimit) {

            Toast.makeText(this, "You cannot Transact as you have exceeded your Daily Limit", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "updateBalance: Transaction LimitExceeded");
            return;
        } else {
            if (inAmount > intBalance) {
                Log.e(TAG, "updateBalance: Insufficient Balance");
                Toast.makeText(this, "You have Insufficient Balance", Toast.LENGTH_LONG).show();
                return;
            } else {
//            computations
                double intnewbalance = intBalance - inAmount;
                DecimalFormat df = new DecimalFormat("#.00");
                String strNewBalance = df.format(intnewbalance);
                Log.e(TAG, "updateBalance: NewBalance"+strNewBalance);
//            update Balance in Firestore
                Map<String, Object> user = new HashMap<>();
                user.put("balance", strNewBalance);
                db.collection("users").document(mAuth.getUid())
                        .set(user, SetOptions.merge());
                Log.e(TAG, "New Balance: " + strNewBalance);

                addDataToFirestore(type, amount, bank, accountNumber, company, referenceNote,beneficiary);
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
            finish();

        } else if (item.getItemId() == R.id.logout) {
            logout();
            Toast.makeText(this, "Logout", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }


}