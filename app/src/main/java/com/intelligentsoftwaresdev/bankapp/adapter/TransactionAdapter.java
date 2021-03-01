package com.intelligentsoftwaresdev.bankapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.intelligentsoftwaresdev.bankapp.R;
import com.intelligentsoftwaresdev.bankapp.models.Transaction;

import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransHolder> {
    Context context;
    List<Transaction> transactions;

    public TransactionAdapter(Context context, List<Transaction> transactions) {
        this.context = context;
        this.transactions = transactions;
    }

    @NonNull
    @Override
    public TransHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new TransHolder(LayoutInflater.from(context).inflate(R.layout.item_transaction,parent));
    }

    @Override
    public void onBindViewHolder(@NonNull TransHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    class TransHolder extends RecyclerView.ViewHolder {
TextView amount,type;
        public TransHolder(@NonNull View itemView) {
            super(itemView);
            amount=itemView.findViewById(R.id.amount);
            type=itemView.findViewById(R.id.type);
        }
    }
}
