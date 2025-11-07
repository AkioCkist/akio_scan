package com.example.akio_scan.bank;

import androidx.annotation.NonNull;

public class BankApp {
    public final String appId;
    public final String appName;
    public final String bankName;
    public final String appLogo;
    public final int monthlyInstall;
    public final String deeplinkTemplate; // e.g., "https://dl.vietqr.io/pay?app=ocb"
    public final boolean supportsAutofill;

    public BankApp(String appId, String appName, String bankName, String appLogo,
                   int monthlyInstall, String deeplinkTemplate, int autofill) {
        this.appId = appId;
        this.appName = appName;
        this.bankName = bankName;
        this.appLogo = appLogo.trim();
        this.monthlyInstall = monthlyInstall;
        this.deeplinkTemplate = deeplinkTemplate;
        this.supportsAutofill = (autofill == 1); // ✅ int == 1 → boolean
    }

    @NonNull
    public String buildDeepLink(String qrData) {
        if (qrData == null) qrData = "";
        String encoded = android.net.Uri.encode(qrData);
        // Thêm &data=... vào cuối URL
        if (deeplinkTemplate.contains("?")) {
            return deeplinkTemplate + "&data=" + encoded;
        } else {
            return deeplinkTemplate + "?data=" + encoded;
        }
    }
}
