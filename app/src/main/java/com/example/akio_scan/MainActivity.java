package com.example.akio_scan;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.example.akio_scan.qr.QRCodeData;
import com.example.akio_scan.qr.QRCodeParser;
import com.example.akio_scan.scanner.ImageQrDecoder;
import com.example.akio_scan.ui.ResultDisplayHelper;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CAMERA = 100;
    private static final int REQUEST_GALLERY = 101;
    private View resultCardContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnScanCamera = findViewById(R.id.btnScanCamera);
        Button btnPickImage = findViewById(R.id.btnPickImage);
        resultCardContent = findViewById(R.id.resultCardContent);

        btnScanCamera.setOnClickListener(v -> scanFromCamera());
        btnPickImage.setOnClickListener(v -> pickImageFromGallery());
        
        // Show empty state initially
        ResultDisplayHelper.showEmptyState(resultCardContent);
    }

    private void scanFromCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA);
        } else {
            new IntentIntegrator(this)
                    .setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
                    .setPrompt("Scan banking QR code")
                    .setCameraId(0)
                    .setBeepEnabled(false)
                    .initiateScan();
        }
    }

    private void pickImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_GALLERY);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            scanFromCamera();
        } else {
            Toast.makeText(this, "Camera permission required", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_GALLERY && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            try {
                InputStream is = getContentResolver().openInputStream(uri);
                Bitmap bitmap = BitmapFactory.decodeStream(is);
                String qrContent = ImageQrDecoder.decodeQRFromBitmap(bitmap);
                processQRContent(qrContent);
            } catch (Exception e) {
                Toast.makeText(this, "Failed to read image", Toast.LENGTH_SHORT).show();
            }
        } else {
            IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            if (result != null && result.getContents() != null) {
                processQRContent(result.getContents());
            }
        }
    }

    private void processQRContent(String qrContent) {
        if (qrContent == null || qrContent.trim().isEmpty()) {
            Toast.makeText(this, "No QR code found", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            QRCodeData parsed = QRCodeParser.parse(qrContent);
            ResultDisplayHelper.display(resultCardContent, parsed);
        } catch (Exception e) {
            Toast.makeText(this, "Invalid QR format: " + e.getMessage(), Toast.LENGTH_LONG).show();
            ResultDisplayHelper.showEmptyState(resultCardContent);
        }
    }
}