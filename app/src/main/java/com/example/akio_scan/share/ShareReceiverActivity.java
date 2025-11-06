package com.example.akio_scan.share;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.akio_scan.MainActivity;
import com.example.akio_scan.R;
import com.example.akio_scan.qr.QRCodeData;
import com.example.akio_scan.qr.QRCodeParser;
import com.example.akio_scan.scanner.ImageQrDecoder;
import java.io.InputStream;

public class ShareReceiverActivity extends AppCompatActivity {

    private TextView tvBank, tvAccount, tvAmount, tvMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_receiver); // 👈 Gán layout UI

        initViews();
        processSharedImage();
    }

    private void initViews() {
        tvBank = findViewById(R.id.tvBank);
        tvAccount = findViewById(R.id.tvAccount);
        tvAmount = findViewById(R.id.tvAmount);
        tvMessage = findViewById(R.id.tvMessage);
        Button btnClose = findViewById(R.id.btnClose);
        Button btnOpenApp = findViewById(R.id.btnOpenApp); // optional

        // Setup click-to-copy for each field
        setupCopyOnClick(findViewById(R.id.tvBank), tvBank, "bank name");
        setupCopyOnClick(findViewById(R.id.tvAccount), tvAccount, "account number");
        setupCopyOnClick(findViewById(R.id.tvAmount), tvAmount, "amount");
        setupCopyOnClick(findViewById(R.id.tvMessage), tvMessage, "purpose");

        btnClose.setOnClickListener(v -> finish());
        btnOpenApp.setOnClickListener(v -> openInMainApp());
    }

    private void setupCopyOnClick(View card, TextView textView, String label) {
        card.setOnClickListener(v -> {
            String text = textView.getText().toString().trim();
            // Skip if placeholder text
            if (text.isEmpty() ||
                    text.equals("(Enter manually)") ||
                    text.equals("(None)")) {
                Toast.makeText(this, "No data to copy", Toast.LENGTH_SHORT).show();
                return;
            }

            copyToClipboard(text);
            Toast.makeText(this, "Copied " + label + "!", Toast.LENGTH_SHORT).show();
        });
    }

    private void copyToClipboard(String text) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("QR Data", text);
        clipboard.setPrimaryClip(clip);
    }

    private void processSharedImage() {
        try {
            Uri uri = getIntent().getParcelableExtra(Intent.EXTRA_STREAM);
            if (uri == null) {
                showErrorAndFinish("No image received");
                return;
            }

            InputStream is = getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            String qrContent = ImageQrDecoder.decodeQRFromBitmap(bitmap);

            if (qrContent == null || qrContent.trim().isEmpty()) {
                showErrorAndFinish("No QR code found in the image");
                return;
            }

            QRCodeData data = QRCodeParser.parse(qrContent);
            displayData(data);

        } catch (Exception e) {
            e.printStackTrace();
            showErrorAndFinish("Failed to read image: " + e.getMessage());
        }
    }

    private void displayData(QRCodeData data) {
        // Bank
        tvBank.setText(data.getBankName() != null ? data.getBankName() : "Unknown");

        // Account
        tvAccount.setText(data.getAccountNumber() != null ? data.getAccountNumber() : "(None)");

        // Amount
        if (data.getAmount() != null && !data.getAmount().isEmpty()) {
            tvAmount.setText(formatVND(data.getAmount()));
        } else {
            tvAmount.setText("(Enter manually)");
        }

        // Purpose
        tvMessage.setText(
                (data.getPurpose() != null && !data.getPurpose().isEmpty())
                        ? data.getPurpose()
                        : "(None)"
        );
    }

    private String formatVND(String amountStr) {
        try {
            double amount = Double.parseDouble(amountStr);
            return String.format("%,.0f", amount) + " VND";
        } catch (Exception e) {
            return amountStr + " VND";
        }
    }

    private void openInMainApp() {
        QRCodeData temp = new QRCodeData(); // We don't have original data, so reconstruct from UI
        // Alternatively, re-parse QR again — but for simplicity, just open MainActivity
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void showErrorAndFinish(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        finish();
    }
}