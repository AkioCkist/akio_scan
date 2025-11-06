package com.example.akio_scan.share;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.akio_scan.MainActivity;
import com.example.akio_scan.qr.QRCodeData;
import com.example.akio_scan.qr.QRCodeParser;
import com.example.akio_scan.scanner.ImageQrDecoder;
import com.example.akio_scan.ui.ResultDisplayHelper;
import java.io.InputStream;

public class ShareReceiverActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        handleSharedImage();
    }

    private void handleSharedImage() {
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null && type.startsWith("image/")) {
            Uri imageUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
            if (imageUri != null) {
                processImageUri(imageUri);
            } else {
                showErrorAndFinish("No image received");
            }
        } else {
            showErrorAndFinish("Invalid share request");
        }
    }

    private void processImageUri(Uri uri) {
        try {
            InputStream is = getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            String qrContent = ImageQrDecoder.decodeQRFromBitmap(bitmap);

            if (qrContent == null || qrContent.trim().isEmpty()) {
                showErrorAndFinish("No QR code found in the image");
                return;
            }

            QRCodeData parsed = QRCodeParser.parse(qrContent);

            // Show result in a dialog
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle("QR Code Detected")
                    .setMessage(buildResultMessage(parsed))
                    .setPositiveButton("OK", (d, w) -> finish())
                    .setNegativeButton("Open in App", (d, w) -> {
                        // Optional: pass data to MainActivity
                        Intent mainIntent = new Intent(this, MainActivity.class);
                        mainIntent.putExtra("qr_result", buildResultMessage(parsed));
                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(mainIntent);
                        finish();
                    })
                    .create();
            dialog.show();

        } catch (Exception e) {
            e.printStackTrace();
            showErrorAndFinish("Failed to read image: " + e.getMessage());
        }
    }

    private String buildResultMessage(QRCodeData data) {
        StringBuilder sb = new StringBuilder();
        sb.append("🏦 Bank: ").append(data.getBankName()).append("\n");
        sb.append("💳 Account: ").append(data.getAccountNumber()).append("\n");
        if (data.getAmount() != null && !data.getAmount().isEmpty()) {
            sb.append("💰 Amount: ").append(formatAmount(data.getAmount())).append(" VND\n");
        } else {
            sb.append("💰 Amount: (enter manually)\n");
        }
        if (data.getPurpose() != null && !data.getPurpose().isEmpty()) {
            sb.append("📝 Purpose: ").append(data.getPurpose()).append("\n");
        }
        return sb.toString();
    }

    private String formatAmount(String amount) {
        try {
            double value = Double.parseDouble(amount);
            return String.format("%,.0f", value);
        } catch (NumberFormatException e) {
            return amount;
        }
    }

    private void showErrorAndFinish(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        finish();
    }
}
