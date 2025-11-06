package com.example.akio_scan.ui;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.example.akio_scan.R;
import com.example.akio_scan.qr.QRCodeData;

public class ResultDisplayHelper {
    public static void display(View rootView, QRCodeData data) {
        // Get all sections
        LinearLayout bankSection = rootView.findViewById(R.id.bankSection);
        LinearLayout accountSection = rootView.findViewById(R.id.accountSection);
        LinearLayout amountSection = rootView.findViewById(R.id.amountSection);
        LinearLayout purposeSection = rootView.findViewById(R.id.messageSection);
        LinearLayout currencySection = rootView.findViewById(R.id.currencySection);
        LinearLayout emptyState = rootView.findViewById(R.id.emptyState);
        
        // Get all TextViews
        TextView tvBankName = rootView.findViewById(R.id.tvBankName);
        TextView tvAccountNumber = rootView.findViewById(R.id.tvAccountNumber);
        TextView tvAmount = rootView.findViewById(R.id.tvAmount);
        TextView tvPurpose = rootView.findViewById(R.id.tvMessage);
        TextView tvCurrency = rootView.findViewById(R.id.tvCurrency);
        
        // Hide empty state and show data sections
        emptyState.setVisibility(View.GONE);
        bankSection.setVisibility(View.VISIBLE);
        accountSection.setVisibility(View.VISIBLE);
        
        // Set bank name
        tvBankName.setText(data.getBankName());
        
        // Set account number with spacing for better readability
        String accountNumber = data.getAccountNumber();
        if (accountNumber != null && accountNumber.length() > 4) {
            // Format account number: XXXX XXXX XXXX
            StringBuilder formattedAccount = new StringBuilder();
            for (int i = 0; i < accountNumber.length(); i++) {
                if (i > 0 && i % 4 == 0) {
                    formattedAccount.append(" ");
                }
                formattedAccount.append(accountNumber.charAt(i));
            }
            tvAccountNumber.setText(formattedAccount.toString());
        } else {
            tvAccountNumber.setText(accountNumber);
        }
        
        // Set amount
        if (data.getAmount() != null && !data.getAmount().isEmpty()) {
            amountSection.setVisibility(View.VISIBLE);
            tvAmount.setText(formatAmount(data.getAmount()) + " VND");
        } else {
            amountSection.setVisibility(View.GONE);
        }
        
        // Set purpose
        if (data.getPurpose() != null && !data.getPurpose().isEmpty()) {
            purposeSection.setVisibility(View.VISIBLE);
            tvPurpose.setText(data.getPurpose());
        } else {
            purposeSection.setVisibility(View.GONE);
        }
        
        // Set currency
        if (data.getCurrency() != null && !data.getCurrency().isEmpty()) {
            currencySection.setVisibility(View.VISIBLE);
            tvCurrency.setText(data.getCurrency());
        } else {
            currencySection.setVisibility(View.GONE);
        }
    }
    
    public static void showEmptyState(View rootView) {
        // Hide all data sections
        rootView.findViewById(R.id.bankSection).setVisibility(View.GONE);
        rootView.findViewById(R.id.accountSection).setVisibility(View.GONE);
        rootView.findViewById(R.id.amountSection).setVisibility(View.GONE);
        rootView.findViewById(R.id.messageSection).setVisibility(View.GONE);
        rootView.findViewById(R.id.currencySection).setVisibility(View.GONE);
        
        // Show empty state
        rootView.findViewById(R.id.emptyState).setVisibility(View.VISIBLE);
    }

    private static String formatAmount(String amount) {
        try {
            double value = Double.parseDouble(amount);
            return String.format("%,.0f", value);
        } catch (NumberFormatException e) {
            return amount;
        }
    }
}