package com.intelligentsoftwaresdev.bankapp.models;

public class Transaction {
    String amount;
    String type;

    public Transaction(String amount, String type) {
        this.amount = amount;
        this.type = type;
    }
}
