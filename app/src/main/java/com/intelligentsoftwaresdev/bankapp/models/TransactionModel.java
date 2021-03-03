package com.intelligentsoftwaresdev.bankapp.models;

public class TransactionModel {
    String type;
    String amount;
    String bank;
    String accountNumber;
    String company;
    String referenceNote;
    String beneficiary;

//    public TransactionModel(String type, String amount, String bank, String accountNumber, String company, String receiverAccountNumber, String referenceNote) {
//    }
    public TransactionModel(){}

    public TransactionModel(String type, String amount, String bank, String accountNumber, String company, String referenceNote ,String beneficiary) {
        this.type = type;
        this.amount = amount;
        this.bank = bank;
        this.accountNumber = accountNumber;
        this.company = company;
        this.referenceNote = referenceNote;
        this.beneficiary = beneficiary;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getBeneficiary() {
        return beneficiary;
    }

    public void setBeneficiary(String beneficiary) {
        this.beneficiary = beneficiary;
    }

       public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getBank() {
        return bank;
    }

    public void setBank(String bank) {
        this.bank = bank;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }



    public String getReferenceNote() {
        return referenceNote;
    }

    public void setReferenceNote(String referenceNote) {
        this.referenceNote = referenceNote;
    }
}
