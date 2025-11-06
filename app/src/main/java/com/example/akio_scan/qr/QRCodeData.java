package com.example.akio_scan.qr;

public class QRCodeData {
    private String bankBIN;
    private String accountNumber;
    private String bankName; // Optional: map BIN to name
    private String amount;
    private String currency;
    private String purpose;

    // Getters and Setters
    public String getBankBIN() { return bankBIN; }
    public void setBankBIN(String bankBIN) { this.bankBIN = bankBIN; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }

    public String getAmount() { return amount; }
    public void setAmount(String amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }

    @Override
    public String toString() {
        return "QRCodeData{" +
                "bankBIN='" + bankBIN + '\'' +
                ", accountNumber='" + accountNumber + '\'' +
                ", bankName='" + bankName + '\'' +
                ", amount='" + amount + '\'' +
                ", currency='" + currency + '\'' +
                ", purpose='" + purpose + '\'' +
                '}';
    }
}
