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

import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.intelligentsoftwaresdev.bankapp.databinding.ActivityAddMoneyBinding;
import com.intelligentsoftwaresdev.bankapp.databinding.ActivityPayBillBinding;

import java.util.HashMap;
import java.util.Map;

public class AddMoney extends AppCompatActivity {
    ActivityAddMoneyBinding b;


    private String amount = "";
    private  Integer balance;
    private FirebaseAuth mAuth;
    private String TAG = "";
    private String userId = "";
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        b = DataBindingUtil.setContentView(this, R.layout.activity_add_money);
        b.btSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addMoney();
            }
        });
    }

    private void addMoney() {

         amount = b.amount.getText().toString().trim();
        if (TextUtils.isEmpty(amount)) {
            b.amount.setError("Account Number is required");
        } else {
            DocumentReference documentReference = db.collection("transactions").document(mAuth.getUid());
            Map<String, Object> transaction = new HashMap<>();
            transaction.put("type", "addMoney");
            transaction.put("amount", amount);
            documentReference.set(transaction).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {

                    Toast.makeText(AddMoney.this, "Bill Paid Succesfully", Toast.LENGTH_SHORT).show();
                    updateBalance();
                }
            });

        }


    }



    private void updateBalance() {
        getDataFirestore();
        Integer newbalance = balance + Integer.parseInt(amount);
//        register user details into firestore
        DocumentReference documentReference = db.collection("users").document(userId);
        Map<String, Object> user = new HashMap<>();
        user.put("balance", newbalance);
        documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "onSuccess: User created succesfully");
                Toast.makeText(AddMoney.this, "Bill Paid", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(AddMoney.this,MainActivity.class));
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
                                Log.e(TAG, "onComplete: "+myMap.toString() );
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
                        Toast.makeText(AddMoney.this, "Firestore"+e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                })
                .addOnCanceledListener(new OnCanceledListener() {
                    @Override
                    public void onCanceled() {
                        Toast.makeText(AddMoney.this, "cancelled", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}