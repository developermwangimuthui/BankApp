package com.IRAKYAT.bankapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.IRAKYAT.bankapp.R;
import com.IRAKYAT.bankapp.adapter.TransactionAdapter;
import com.IRAKYAT.bankapp.databinding.ActivityMainBinding;
import com.IRAKYAT.bankapp.models.TransactionModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import utils.Tools;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding b;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private String TAG = "";
    private String balance;
    private String accountNumber;

    private RecyclerView courseRV;
    List<TransactionModel> transactionModelList = new ArrayList<>();
    private CollectionReference transactionsRef = db.collection("allTransaction").document(mAuth.getUid()).collection("transactions");
    private TransactionAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = DataBindingUtil.setContentView(this, R.layout.activity_main);
        initToolbar();
        initButtonActions();
        getDataFirestore();
        initRecyclerView();

        adapter.setOnItemClickListener(new TransactionAdapter.OnitemClickListener() {
            @Override
            public void onIemClick(DocumentSnapshot documentSnapshot, int position) {
                TransactionModel transactions = documentSnapshot.toObject(TransactionModel.class);
                String id = documentSnapshot.getId();
                String path = documentSnapshot.getReference().getPath();
                Intent intent = new Intent(MainActivity.this, TransactionDetailsActivity.class);
                intent.putExtra("documentID", id);
                startActivity(intent);
            }


        });
    }


    private void initRecyclerView() {
        Log.e(TAG, "initRecyclerView: Called");
        Query query = transactionsRef.orderBy("amount", Query.Direction.DESCENDING);
        FirestoreRecyclerOptions<TransactionModel> options = new FirestoreRecyclerOptions.Builder<TransactionModel>()
                .setQuery(query, TransactionModel.class)
                .build();
        Log.e(TAG, "Options: "+options );

        adapter = new TransactionAdapter(options);
        RecyclerView recyclerView = findViewById(R.id.transactionsRecycler);
//        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }



    private void initButtonActions() {
        b.sendMoney.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(MainActivity.this, SendMoneyActivity.class));
            }
        });
        b.payBills.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(MainActivity.this, BillingActivity.class));
            }
        });

    }


    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.maintoolbar);
        toolbar.setNavigationIcon(R.drawable.ic_home);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Home");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Tools.setSystemBarColor(this);
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(this, LoginActivity.class));
        finish();

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
            Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show();
        } else if (item.getItemId() == R.id.account) {
            Toast.makeText(this, "Account", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(MainActivity.this, AccountActivity.class));

        } else if (item.getItemId() == R.id.logout) {
            logout();
            Toast.makeText(this, "Logout", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

    //    Set Items in the User Interface;
    private void initUI(String balance,String accountNumber) {
        Log.e(TAG, "initUI: Called");
        b.balance.setText("RM "+balance);
        b.accountNumber.setText(accountNumber);

    }


    private void getDataFirestore() {

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
                        accountNumber = (String) userMap.get("accountNumber");
                        Log.e(TAG, "Balance: " + balance);
                        initUI(balance,accountNumber);

                    } else {
                        Log.e(TAG, "No such document");
                    }
                } else {
                    Log.e(TAG, "get failed with ", task.getException());
                }
            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
        getDataFirestore();
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
        Log.e(TAG, "onStart: AdapterListening");
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
        Log.e(TAG, "onStop: AdapterNotListening");
    }
}



