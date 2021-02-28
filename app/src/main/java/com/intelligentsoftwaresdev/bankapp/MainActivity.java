package com.intelligentsoftwaresdev.bankapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.intelligentsoftwaresdev.bankapp.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding b;
    FirebaseFirestore db;
    private String TAG = "";
    private String userId;
    private FirebaseAuth mAuth;
    List<Transaction> list=new ArrayList<>();
    TransactionAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "onCreate: Mainactivity ");
        Toast.makeText(this, "I am home ", Toast.LENGTH_SHORT).show();
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        initRecycler();
        b = DataBindingUtil.setContentView(this, R.layout.activity_main);
        b.addMoney.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, AddMoney.class));
            }
        });
        b.sendMoney.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SendMoney.class));
            }
        });
        b.payBills.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, Billing.class));
            }
        });
        b.accIB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, Account.class));
            }
        });

        getDataFirestore();
    }

    private void initRecycler() {
        adapter= new TransactionAdapter(this,list);
        b.recycler.setLayoutManager(new LinearLayoutManager(this));
        b.recycler.setAdapter(adapter);


//        list.add( new Transaction("1000","sent"));
//        adapter.notifyDataSetChanged();
    }

    private void getDataFirestore() {
        Toast.makeText(this, "Firestore", Toast.LENGTH_SHORT).show();
        Log.e(TAG, "getDataFirestore: " + mAuth.getUid());
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
                                String balance = String.valueOf((long) myMap.get("balance"));
                                b.balance.setText(balance);
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
                        Toast.makeText(MainActivity.this, "Firestore" + e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                })
                .addOnCanceledListener(new OnCanceledListener() {
                    @Override
                    public void onCanceled() {
                        Toast.makeText(MainActivity.this, "cancelled", Toast.LENGTH_SHORT).show();
                    }
                });
    }


}