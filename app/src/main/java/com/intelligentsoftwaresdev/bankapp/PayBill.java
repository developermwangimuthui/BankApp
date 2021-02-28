package com.intelligentsoftwaresdev.bankapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.intelligentsoftwaresdev.bankapp.databinding.ActivityPayBillBinding;

import java.util.HashMap;
import java.util.Map;

public class PayBill extends AppCompatActivity {
    ActivityPayBillBinding b;

    private FirebaseAuth mAuth;
    private String TAG = "";
    private String userId = "";
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        b = DataBindingUtil.setContentView(this,R.layout.activity_pay_bill);
        b.btSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                paybill();
            }
        });
    }

    private void paybill() {
        String amount = b.amount.getText().toString().trim();
        String bill = b.biil.getText().toString().trim();
        if (TextUtils.isEmpty(amount)) {
            b.amount.setError("Account Number is required");
        } else if (TextUtils.isEmpty(bill)) {
            b.biil.setError("Amount is required");
        } else {
            DocumentReference documentReference = db.collection("transactions").document(mAuth.getUid());
            Map<String, Object> transaction = new HashMap<>();
            transaction.put("type", "paybill");
            transaction.put("amount", amount);
            transaction.put("bill", bill);
            documentReference.set(transaction).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {

                    Toast.makeText(PayBill.this, "Bill Paid Succesfully", Toast.LENGTH_SHORT).show();
                }
            });

        }


    }
}