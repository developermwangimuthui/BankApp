package com.intelligentsoftwaresdev.bankapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.intelligentsoftwaresdev.bankapp.databinding.ActivitySendMoneyBinding;

import java.util.HashMap;
import java.util.Map;

public class Billing extends AppCompatActivity {

    ActivitySendMoneyBinding b;
    private FirebaseAuth mAuth;
    private String TAG = "";
    private String userId = "";
    private String amount = "";
    private Integer balance;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        b = DataBindingUtil.setContentView(this, R.layout.activity_send_money);
        initComponent();

        b.btSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMoney();
            }
        });
    }

    private void initComponent() {


        (findViewById(R.id.company)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCompanyDialog(v);
            }
        });

    }

    private void showStateDialog(final View v) {
        final String[] array = new String[]{
                "CIMB", "Affin Bank", "RHB", "Hong Leong Bank", "AmBank", "Standard Chartered Bank"
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

    private void showCompanyDialog(final View v) {
        final String[] array = new String[]{
                "TNB Malaysia", "TeleKom Malasia", "RHB", "Hong Leong Bank", "AmBank", "Standard Chartered Bank"
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
        String recieverAccountNumber = b.accountNumber.getText().toString().trim();
        String company = b.company.getText().toString().trim();
        String referenceNote = b.referenceNote.getText().toString().trim();
        amount = b.amount.getText().toString().trim();
        if (TextUtils.isEmpty(recieverAccountNumber)) {
            b.accountNumber.setError("Account Number is required");
        } else if (TextUtils.isEmpty(amount)) {
            b.amount.setError("Amount is required");
        } else if (TextUtils.isEmpty(referenceNote)) {
            b.referenceNote.setError("ReferenceNote is Required");
        } else if (TextUtils.isEmpty(company)) {
            b.company.setError("Company is Required");
        } else {
            DocumentReference documentReference = db.collection("transactions").document(mAuth.getUid());
            Map<String, Object> transaction = new HashMap<>();
            transaction.put("type", "billing");
            transaction.put("amount", amount);
            transaction.put("company", company);
            transaction.put("recieverAccountNumber", recieverAccountNumber);
            transaction.put("referenceNote", referenceNote);
            documentReference.set(transaction).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {

                    Toast.makeText(Billing.this, "Money Send Succesfully", Toast.LENGTH_SHORT).show();
                    updateBalance();
                }
            });

        }


    }

    private void updateBalance() {
        getDataFirestore();
        Integer newbalance = balance - Integer.parseInt(amount);
//        register user details into firestore
        DocumentReference documentReference = db.collection("users").document(userId);
        Map<String, Object> user = new HashMap<>();
        user.put("balance", newbalance);
        documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "onSuccess: User created succesfully");
                Toast.makeText(Billing.this, "Money Sent", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(Billing.this, MainActivity.class));
            }
        });


    }

    private void getDataFirestore() {
        db.collection("users")
                .document(mAuth.getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        Log.e(TAG, "onComplete: ");
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
//                                Toast.makeText(MainActivity.this, "Firestore"+document.getData(), Toast.LENGTH_SHORT).show();
                                Log.e(TAG, "DocumentSnapshot data: " + document.getData());//see log below
                                Map<String, Object> myMap = (Map<String, Object>) document.getData();
                                Log.e(TAG, "onComplete: " + myMap.toString());
                                String sbalance = String.valueOf((double) myMap.get("balance"));
                                balance = Integer.parseInt(sbalance);
                                Log.e(TAG, "onComplete: " + balance);
                            } else {
                                Log.e(TAG, "No such document");
                            }
                        } else {
                            Log.e(TAG, "get failed with ", task.getException());
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Billing.this, "Firestore" + e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                })
                .addOnCanceledListener(new OnCanceledListener() {
                    @Override
                    public void onCanceled() {
                        Toast.makeText(Billing.this, "cancelled", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}