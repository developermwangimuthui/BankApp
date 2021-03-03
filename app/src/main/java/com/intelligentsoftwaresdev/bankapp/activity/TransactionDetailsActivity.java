package com.intelligentsoftwaresdev.bankapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.intelligentsoftwaresdev.bankapp.R;
import com.intelligentsoftwaresdev.bankapp.databinding.ActivityTransactionDetailsBinding;

import java.util.Map;

public class TransactionDetailsActivity extends AppCompatActivity {
    ActivityTransactionDetailsBinding b;
    private static String TAG = "";
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    private String type;
    private String amount;
    private String bank;
    private String accountNumber;
    private String company;
    private String referenceNote;
    private String beneficiary;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = DataBindingUtil.setContentView(this, R.layout.activity_transaction_details);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String documentId = (String) extras.getSerializable("documentID");
            getDataFromFireStore(documentId);

            Log.e(TAG, "DocumentID" + documentId);
        }
        b.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void getDataFromFireStore(String documentId) {
        DocumentReference docRef = db.collection("allTransaction").document(mAuth.getUid()).collection("transactions").document(documentId);

        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Map<String, Object> transaction = (Map<String, Object>) document.getData();
                        amount = (String) transaction.get("amount");
                        bank = (String) transaction.get("bank");
                        accountNumber = (String) transaction.get("accountNumber");
                        amount = (String) transaction.get("amount");
                        company = (String) transaction.get("company");
                        referenceNote = (String) transaction.get("referenceNote");
                        beneficiary = (String) transaction.get("beneficiary");
                        Log.e(TAG, "Beneficiary"+beneficiary );
                        initUI(amount, bank, accountNumber, company, referenceNote, beneficiary);

                    } else {
                        Log.e(TAG, "No such document");
                    }
                } else {
                    Log.e(TAG, "get failed with ", task.getException());
                }
            }
        });

    }

    private void initUI(String amount, String bank, String accountNumber, String company, String referenceNote, String beneficiary) {
        if (accountNumber.isEmpty()) {
            b.LaccountNumber.setVisibility(View.GONE);
        } else {
            b.accountNumber.setText(accountNumber);
        }
        if (bank.isEmpty()) {
            b.Lbank.setVisibility(View.GONE);
        } else {
            b.bank.setText(bank);
        }
        if (company.isEmpty()) {
            b.Lcompany.setVisibility(View.GONE);
        } else {
            b.company.setText(company);
        }
        if (referenceNote.isEmpty()) {
            b.LreferenceNote.setVisibility(View.GONE);
        } else {
            b.referenceNote.setText(referenceNote);
        }
        if (beneficiary.isEmpty()) {
            b.LbeneficiaryName.setVisibility(View.GONE);
        } else {
            b.beneficiaryName.setText(beneficiary);
        }

        b.amount.setText("RM "+amount);


    }


}