package com.example.akio_scan.share;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.example.akio_scan.MainActivity;
import com.example.akio_scan.R;
import com.example.akio_scan.bank.BankApp;
import com.example.akio_scan.bank.BankAppRegistry;
import com.example.akio_scan.qr.QRCodeData;
import com.example.akio_scan.qr.QRCodeParser;
import com.example.akio_scan.scanner.ImageQrDecoder;
import java.io.InputStream;

public class ShareReceiverActivity extends AppCompatActivity {

    private static final String TAG = "ShareReceiverActivity";
    private TextView tvBank, tvAccount, tvAmount, tvMessage;
    private MaterialButton btnOpenApp;
    private QRCodeData currentQRData;
    private String rawQRContent;

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
        btnOpenApp = findViewById(R.id.btnOpenApp);

        // Setup click-to-copy for each field with the new button IDs
        setupCopyOnClick(findViewById(R.id.bankCopyButton), tvBank, "bank name");
        setupCopyOnClick(findViewById(R.id.accountCopyButton), tvAccount, "account number");
        setupCopyOnClick(findViewById(R.id.tvAmount), tvAmount, "amount");
        setupCopyOnClick(findViewById(R.id.messageCopyButton), tvMessage, "message");

        btnClose.setOnClickListener(v -> finish());
        
        // Initially hide the button - will show if bank supports autofill
        btnOpenApp.setVisibility(View.GONE);
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
        Log.d(TAG, "==========================================");
        Log.d(TAG, "=== PROCESSING SHARED IMAGE ===");
        Log.d(TAG, "==========================================");
        
        try {
            Uri uri = getIntent().getParcelableExtra(Intent.EXTRA_STREAM);
            if (uri == null) {
                Log.e(TAG, "✗ No URI received in intent");
                showErrorAndFinish("No image received");
                return;
            }
            Log.d(TAG, "✓ URI received: " + uri.toString());

            Log.d(TAG, "Opening input stream...");
            InputStream is = getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            Log.d(TAG, "✓ Bitmap decoded: " + bitmap.getWidth() + "x" + bitmap.getHeight());
            
            Log.d(TAG, "Decoding QR code from bitmap...");
            String qrContent = ImageQrDecoder.decodeQRFromBitmap(bitmap);

            if (qrContent == null || qrContent.trim().isEmpty()) {
                Log.w(TAG, "✗ No QR code found in image");
                showErrorAndFinish("No QR code found in the image");
                return;
            }
            
            Log.d(TAG, "✓✓✓ QR CODE FOUND ✓✓✓");
            Log.d(TAG, "QR Length: " + qrContent.length() + " characters");
            Log.d(TAG, "QR Preview: " + qrContent.substring(0, Math.min(100, qrContent.length())) + "...");

            // Store raw QR content for deep linking
            rawQRContent = qrContent;
            
            Log.d(TAG, "Parsing QR code...");
            QRCodeData data = QRCodeParser.parse(qrContent);
            currentQRData = data;
            
            Log.d(TAG, "✓✓✓ QR PARSED SUCCESSFULLY ✓✓✓");
            Log.d(TAG, "  BIN: " + data.getBankBIN());
            Log.d(TAG, "  Bank Name: " + data.getBankName());
            Log.d(TAG, "  Account: " + data.getAccountNumber());
            Log.d(TAG, "  Amount: " + data.getAmount());
            Log.d(TAG, "  Purpose: " + data.getPurpose());
            
            Log.d(TAG, "Displaying data in UI...");
            displayData(data);
            Log.d(TAG, "✓ Data displayed");
            
            Log.d(TAG, "Setting up bank button...");
            setupBankButton(data);

        } catch (Exception e) {
            Log.e(TAG, "✗✗✗ FAILED TO PROCESS IMAGE ✗✗✗", e);
            Log.e(TAG, "Error message: " + e.getMessage());
            e.printStackTrace();
            showErrorAndFinish("Failed to read image: " + e.getMessage());
        }
        Log.d(TAG, "==========================================");
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

    private void setupBankButton(QRCodeData qrData) {
        Log.d(TAG, "==========================================");
        Log.d(TAG, "=== SHARE RECEIVER: SETUP BANK BUTTON ===");
        Log.d(TAG, "==========================================");
        
        if (btnOpenApp == null) {
            Log.e(TAG, "✗✗✗ CRITICAL ERROR: btnOpenApp is NULL!");
            Log.e(TAG, "     Check if findViewById(R.id.btnOpenApp) is finding the button");
            return;
        } else {
            Log.d(TAG, "✓ Step 1: btnOpenApp exists");
            Log.d(TAG, "    Current visibility: " + 
                (btnOpenApp.getVisibility() == View.VISIBLE ? "VISIBLE" : 
                 btnOpenApp.getVisibility() == View.GONE ? "GONE" : "INVISIBLE"));
        }
        
        // Hide by default
        btnOpenApp.setVisibility(View.GONE);
        Log.d(TAG, "✓ Step 2: Button set to GONE (default)");
        
        String bin = qrData.getBankBIN();
        Log.d(TAG, "✓ Step 3: Extracted BIN from QR data");
        Log.d(TAG, "    BIN value: " + (bin != null ? "[" + bin + "]" : "NULL"));
        
        if (bin == null || bin.isEmpty()) {
            Log.w(TAG, "✗ Step 4: BIN is null or empty - STOPPING");
            Log.w(TAG, "    QR Data: " + qrData.toString());
            return;
        }
        
        Log.d(TAG, "✓ Step 4: BIN is valid, looking up bank...");
        BankApp bank = BankAppRegistry.getBankByBIN(bin);
        
        if (bank == null) {
            Log.w(TAG, "✗ Step 5: getBankByBIN returned NULL");
            Log.w(TAG, "    This BIN [" + bin + "] is not in the registry");
            Log.w(TAG, "    Check BIN_TO_APP_ID map in BankAppRegistry");
            return;
        }
        
        Log.d(TAG, "✓ Step 5: Bank found!");
        Log.d(TAG, "    Bank Name: " + bank.appName);
        Log.d(TAG, "    Bank ID: " + bank.appId);
        Log.d(TAG, "    Supports Autofill: " + bank.supportsAutofill);
        Log.d(TAG, "    Deep Link Template: " + bank.deeplinkTemplate);
        
        if (!bank.supportsAutofill) {
            Log.w(TAG, "✗ Step 6: Bank exists but autofill is FALSE");
            Log.w(TAG, "    To enable: Change 'autofill: 0' to 'autofill: 1' in bank.json");
            return;
        }
        
        Log.d(TAG, "✓ Step 6: Bank supports autofill!");
        
        // Show button with bank name
        String buttonText = "Open in " + bank.appName;
        btnOpenApp.setText(buttonText);
        btnOpenApp.setVisibility(View.VISIBLE);
        
        Log.d(TAG, "✓✓✓ Step 7: BUTTON IS NOW VISIBLE!");
        Log.d(TAG, "    Button text: " + buttonText);
        Log.d(TAG, "    Button visibility: VISIBLE");
        Log.d(TAG, "==========================================");
        
        // Set click handler to open bank app
        btnOpenApp.setOnClickListener(v -> {
            Log.d(TAG, ">>> BUTTON CLICKED! Opening " + bank.appName);
            openBankApp(bank);
        });
    }

    private void openBankApp(BankApp bank) {
        try {
            String deepLink = bank.buildDeepLink(rawQRContent);
            Log.d(TAG, "Opening deep link: " + deepLink);
            
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(deepLink));
            startActivity(intent);
            
            Toast.makeText(this, "Opening " + bank.appName, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Failed to open bank app", e);
            Toast.makeText(this, "Cannot open " + bank.appName, Toast.LENGTH_SHORT).show();
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