package com.intelligentsoftwaresdev.bankapp.adapter;


import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.intelligentsoftwaresdev.bankapp.R;
import com.intelligentsoftwaresdev.bankapp.models.TransactionModel;


public class TransactionAdapter extends FirestoreRecyclerAdapter<TransactionModel, TransactionAdapter.TransactionHolder> {

    private String TAG = "";
    private OnitemClickListener listener;

    public TransactionAdapter(@NonNull FirestoreRecyclerOptions<TransactionModel> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull TransactionHolder holder, int position, @NonNull TransactionModel model) {
        Log.e(TAG, "onBindViewHolder: " + model.getType());
        holder.tvType.setText(model.getType());
        holder.tvAmount.setText( "RM " + model.getAmount());
    }

    @NonNull
    @Override
    public TransactionHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction, parent, false);
        return new TransactionHolder(v);
    }

    class TransactionHolder extends RecyclerView.ViewHolder {
        TextView tvType;
        TextView tvAmount;
        TextView tvTransactionDate;

        public TransactionHolder(@NonNull View itemView) {
            super(itemView);
            tvType = itemView.findViewById(R.id.rvType);
            tvAmount = itemView.findViewById(R.id.rvAmount);
            tvTransactionDate = itemView.findViewById(R.id.transactionDate);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null){
                        listener.onIemClick(getSnapshots().getSnapshot(position),position );
                    }
                }
            });
        }
    }

    public interface OnitemClickListener{
        void  onIemClick(DocumentSnapshot documentSnapshot,int position);
    }
    public void  setOnItemClickListener(OnitemClickListener listener){

        this.listener = listener;
    }
}