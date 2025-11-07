package com.example.akio_scan;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.material.button.MaterialButton;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.example.akio_scan.bank.BankApp;
import com.example.akio_scan.bank.BankAppRegistry;
import com.example.akio_scan.qr.QRCodeData;
import com.example.akio_scan.qr.QRCodeParser;
import com.example.akio_scan.scanner.ImageQrDecoder;
import com.example.akio_scan.ui.ResultDisplayHelper;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int REQUEST_CAMERA = 100;
    private static final int REQUEST_GALLERY = 101;
    
    private View resultCardContent;
    private MaterialButton btnOpenInBank;
    private QRCodeData currentQRData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "=== MainActivity onCreate ===");

        // Initialize views
        MaterialButton btnScanCamera = findViewById(R.id.btnScanCamera);
        MaterialButton btnPickImage = findViewById(R.id.btnPickImage);
        resultCardContent = findViewById(R.id.resultCardContent);
        btnOpenInBank = findViewById(R.id.btnOpenInBank);

        // Check if button exists
        if (btnOpenInBank == null) {
            Log.e(TAG, "ERROR: btnOpenInBank not found in layout!");
        } else {
            Log.d(TAG, "✓ btnOpenInBank found in layout");
            btnOpenInBank.setVisibility(View.GONE);
        }

        // Set click listeners
        btnScanCamera.setOnClickListener(v -> startCameraScan());
        btnPickImage.setOnClickListener(v -> pickFromGallery());

        // Show empty state
        ResultDisplayHelper.showEmptyState(resultCardContent);
        
        Log.d(TAG, "=== MainActivity initialized ===");
    }

    private void startCameraScan() {
        Log.d(TAG, ">>> Starting camera scan");
        if (btnOpenInBank != null) btnOpenInBank.setVisibility(View.GONE);
        
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, 
                new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA);
        } else {
            new IntentIntegrator(this)
                    .setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
                    .setPrompt("Scan banking QR code")
                    .setCameraId(0)
                    .setBeepEnabled(false)
                    .initiateScan();
        }
    }

    private void pickFromGallery() {
        Log.d(TAG, ">>> Opening gallery");
        if (btnOpenInBank != null) btnOpenInBank.setVisibility(View.GONE);
        
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_GALLERY);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, 
                                          @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA && grantResults.length > 0 
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startCameraScan();
        } else {
            Toast.makeText(this, "Camera permission required", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        Log.d(TAG, "=== onActivityResult: requestCode=" + requestCode + 
                   ", resultCode=" + resultCode + " ===");

        // GALLERY PICK
        if (requestCode == REQUEST_GALLERY) {
            if (resultCode == RESULT_OK && data != null) {
                Log.d(TAG, "Gallery: Image selected");
                handleGalleryImage(data.getData());
            } else {
                Log.d(TAG, "Gallery: Cancelled");
            }
            return;
        }

        // CAMERA SCAN
        IntentResult scanResult = IntentIntegrator.parseActivityResult(
            requestCode, resultCode, data);
        if (scanResult != null) {
            if (scanResult.getContents() != null) {
                Log.d(TAG, "Camera: QR scanned");
                handleQRCode(scanResult.getContents());
            } else {
                Log.d(TAG, "Camera: Cancelled");
            }
        }
    }

    private void handleGalleryImage(Uri imageUri) {
        try {
            Log.d(TAG, ">>> Decoding QR from gallery image");
            InputStream is = getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            
            String qrContent = ImageQrDecoder.decodeQRFromBitmap(bitmap);
            
            if (qrContent != null && !qrContent.isEmpty()) {
                Log.d(TAG, "✓ QR found in image");
                handleQRCode(qrContent);
            } else {
                Log.w(TAG, "✗ No QR found in image");
                Toast.makeText(this, "No QR code found in image", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "✗ Error decoding image", e);
            Toast.makeText(this, "Failed to read image", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleQRCode(String qrContent) {
        Log.d(TAG, "==========================================");
        Log.d(TAG, "=== HANDLE QR CODE ===");
        Log.d(TAG, "==========================================");
        Log.d(TAG, "QR Length: " + qrContent.length() + " characters");
        Log.d(TAG, "QR Preview: " + qrContent.substring(0, Math.min(100, qrContent.length())) + "...");
        
        try {
            // Parse QR code
            Log.d(TAG, "Parsing QR code...");
            QRCodeData qrData = QRCodeParser.parse(qrContent);
            currentQRData = qrData;
            
            Log.d(TAG, "✓✓✓ QR PARSED SUCCESSFULLY ✓✓✓");
            Log.d(TAG, "  BIN: " + qrData.getBankBIN());
            Log.d(TAG, "  Bank Name: " + qrData.getBankName());
            Log.d(TAG, "  Account: " + qrData.getAccountNumber());
            Log.d(TAG, "  Amount: " + qrData.getAmount());
            Log.d(TAG, "  Currency: " + qrData.getCurrency());
            Log.d(TAG, "  Purpose: " + qrData.getPurpose());
            
            // Display result
            Log.d(TAG, "Displaying result in UI...");
            ResultDisplayHelper.display(this, resultCardContent, qrData);
            Log.d(TAG, "✓ Result displayed");
            
            // Setup button
            Log.d(TAG, "Setting up bank button...");
            setupBankButton(qrData, qrContent);
            
        } catch (Exception e) {
            Log.e(TAG, "✗✗✗ FAILED TO PARSE QR CODE ✗✗✗", e);
            Log.e(TAG, "Error message: " + e.getMessage());
            Toast.makeText(this, "Invalid QR code format", Toast.LENGTH_SHORT).show();
            ResultDisplayHelper.showEmptyState(resultCardContent);
            if (btnOpenInBank != null) btnOpenInBank.setVisibility(View.GONE);
        }
        Log.d(TAG, "==========================================");
    }

    private void setupBankButton(QRCodeData qrData, String qrContent) {
        Log.d(TAG, "==========================================");
        Log.d(TAG, "=== SETUP BANK BUTTON DEBUG ===");
        Log.d(TAG, "==========================================");
        
        if (btnOpenInBank == null) {
            Log.e(TAG, "✗✗✗ CRITICAL ERROR: btnOpenInBank is NULL!");
            Log.e(TAG, "     Check if findViewById(R.id.btnOpenInBank) is finding the button");
            return;
        } else {
            Log.d(TAG, "✓ Step 1: btnOpenInBank exists");
            Log.d(TAG, "    Current visibility: " + 
                (btnOpenInBank.getVisibility() == View.VISIBLE ? "VISIBLE" : 
                 btnOpenInBank.getVisibility() == View.GONE ? "GONE" : "INVISIBLE"));
        }
        
        // Hide by default
        btnOpenInBank.setVisibility(View.GONE);
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
        
        // Show button
        String buttonText = "Open in " + bank.appName;
        btnOpenInBank.setText(buttonText);
        btnOpenInBank.setVisibility(View.VISIBLE);
        
        Log.d(TAG, "✓✓✓ Step 7: BUTTON IS NOW VISIBLE!");
        Log.d(TAG, "    Button text: " + buttonText);
        Log.d(TAG, "    Button visibility: VISIBLE");
        Log.d(TAG, "==========================================");
        
        // Set click handler
        btnOpenInBank.setOnClickListener(v -> {
            Log.d(TAG, ">>> BUTTON CLICKED! Opening " + bank.appName);
            openBankApp(bank, qrContent);
        });
    }

    private void openBankApp(BankApp bank, String qrContent) {
        try {
            String deepLink = bank.buildDeepLink(qrContent);
            Log.d(TAG, "Opening deep link: " + deepLink);
            
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(deepLink));
            startActivity(intent);
            
            Toast.makeText(this, "Opening " + bank.appName, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Failed to open bank app", e);
            Toast.makeText(this, "Cannot open " + bank.appName, Toast.LENGTH_SHORT).show();
        }
    }
}