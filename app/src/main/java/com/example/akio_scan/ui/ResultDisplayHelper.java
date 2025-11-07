package com.example.akio_scan.ui;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.example.akio_scan.R;
import com.example.akio_scan.qr.QRCodeData;

public class ResultDisplayHelper {

    /**
     * Hiển thị dữ liệu QR và thiết lập chức năng "chạm để copy"
     *
     * @param context     Context để truy cập Clipboard và Toast
     * @param rootView    ViewGroup chứa các section (bank, account, v.v.)
     * @param data        Dữ liệu QR đã parse
     */
    public static void display(Context context, View rootView, QRCodeData data) {
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

        // === Bank ===
        String bankName = data.getBankName();
        tvBankName.setText(bankName != null ? bankName : "--");
        setupCopyOnClick(bankSection, bankName, "Bank name copied", context);

        // === Account Number ===
        String accountNumber = data.getAccountNumber();
        String displayAccount = "--";
        if (accountNumber != null && !accountNumber.isEmpty()) {
            if (accountNumber.length() > 4) {
                StringBuilder formatted = new StringBuilder();
                for (int i = 0; i < accountNumber.length(); i++) {
                    if (i > 0 && i % 4 == 0) {
                        formatted.append(" ");
                    }
                    formatted.append(accountNumber.charAt(i));
                }
                displayAccount = formatted.toString();
            } else {
                displayAccount = accountNumber;
            }
        }
        tvAccountNumber.setText(displayAccount);
        setupCopyOnClick(accountSection, accountNumber, "Account number copied", context);

        // === Amount ===
        if (data.getAmount() != null && !data.getAmount().isEmpty()) {
            amountSection.setVisibility(View.VISIBLE);
            tvAmount.setText(formatAmount(data.getAmount()) + " VND");
            setupCopyOnClick(amountSection, data.getAmount(), "Amount copied", context);
        } else {
            amountSection.setVisibility(View.GONE);
        }

        // === Purpose ===
        if (data.getPurpose() != null && !data.getPurpose().isEmpty()) {
            purposeSection.setVisibility(View.VISIBLE);
            tvPurpose.setText(data.getPurpose());
            setupCopyOnClick(purposeSection, data.getPurpose(), "Message copied", context);
        } else {
            purposeSection.setVisibility(View.GONE);
        }

        // === Currency ===
        if (data.getCurrency() != null && !data.getCurrency().isEmpty()) {
            currencySection.setVisibility(View.VISIBLE);
            tvCurrency.setText(data.getCurrency());
            setupCopyOnClick(currencySection, data.getCurrency(), "Currency copied", context);
        } else {
            currencySection.setVisibility(View.GONE);
        }
    }

    private static void setupCopyOnClick(View view, String textToCopy, String successMessage, Context context) {
        if (view == null || textToCopy == null || textToCopy.isEmpty() || "--".equals(textToCopy)) {
            if (view != null) {
                view.setClickable(false);
                view.setFocusable(false);
            }
            return;
        }

        view.setClickable(true);
        view.setFocusable(true);

        view.setOnClickListener(v -> {
            // Optional: animation
            v.animate()
                    .scaleX(0.95f)
                    .scaleY(0.95f)
                    .setDuration(100)
                    .withEndAction(() -> {
                        v.animate()
                                .scaleX(1.0f)
                                .scaleY(1.0f)
                                .setDuration(100)
                                .start();
                    })
                    .start();

            // Copy to clipboard
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("QR Data", textToCopy);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(context, successMessage, Toast.LENGTH_SHORT).show();
        });
    }

    public static void showEmptyState(View rootView) {
        rootView.findViewById(R.id.bankSection).setVisibility(View.GONE);
        rootView.findViewById(R.id.accountSection).setVisibility(View.GONE);
        rootView.findViewById(R.id.amountSection).setVisibility(View.GONE);
        rootView.findViewById(R.id.messageSection).setVisibility(View.GONE);
        rootView.findViewById(R.id.currencySection).setVisibility(View.GONE);
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