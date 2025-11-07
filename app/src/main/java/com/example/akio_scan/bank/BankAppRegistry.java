package com.example.akio_scan.bank;

import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BankAppRegistry {
    private static final String TAG = "BankAppRegistry";
    private static final Map<String, BankApp> BANK_BY_APP_ID = new HashMap<>();
    private static final Map<String, String> BIN_TO_APP_ID = new HashMap<>();

    // Gọi 1 lần khi app khởi động (ví dụ: trong Application class)
    public static void initializeFromJson(String jsonString) {
        BANK_BY_APP_ID.clear();
        BIN_TO_APP_ID.clear();

        try {
            JSONObject root = new JSONObject(jsonString);
            JSONArray apps = root.getJSONArray("apps");
            
            Log.d(TAG, "initializeFromJson: Found " + apps.length() + " bank apps");

            for (int i = 0; i < apps.length(); i++) {
                JSONObject appJson = apps.getJSONObject(i);
                BankApp bank = new BankApp(
                        appJson.getString("appId"),
                        appJson.getString("appName"),
                        appJson.getString("bankName"),
                        appJson.getString("appLogo"),
                        appJson.optInt("monthlyInstall", 0),
                        appJson.getString("deeplink"),
                        appJson.optInt("autofill", 0)
                );
                BANK_BY_APP_ID.put(bank.appId, bank);
                Log.d(TAG, "initializeFromJson: Added bank - " + bank.appName + 
                           " (autofill: " + bank.supportsAutofill + ")");
            }

            // Map BIN → appId (theo chuẩn NAPAS)
            BIN_TO_APP_ID.put("970443", "ocb");      // OCB
            BIN_TO_APP_ID.put("970437", "acb");      // ACB
            BIN_TO_APP_ID.put("970415", "icb");      // VietinBank
            BIN_TO_APP_ID.put("970422", "mb");       // MB Bank (BIN phụ)
            BIN_TO_APP_ID.put("970436", "mb");       // MB Bank (BIN chính)
            BIN_TO_APP_ID.put("970418", "bidv");     // BIDV
            BIN_TO_APP_ID.put("970416", "vcb");      // Vietcombank
            BIN_TO_APP_ID.put("970423", "tcb");      // Techcombank
            BIN_TO_APP_ID.put("970420", "vpb");      // VPBank
            BIN_TO_APP_ID.put("970438", "vib-2");    // MyVIB 2.0
            BIN_TO_APP_ID.put("970442", "shb");      // SHB
            BIN_TO_APP_ID.put("970449", "lpb");      // LienVietPostBank
            BIN_TO_APP_ID.put("970450", "seab");     // SEABANK
            BIN_TO_APP_ID.put("970425", "scb");      // SCB
            BIN_TO_APP_ID.put("970454", "vietbank"); // Vietbank
            BIN_TO_APP_ID.put("970452", "cake");     // CAKE
            BIN_TO_APP_ID.put("970429", "hdb");      // HDBank
            BIN_TO_APP_ID.put("970405", "vba");      // Agribank
            BIN_TO_APP_ID.put("970426", "tpb");      // TPBank
            BIN_TO_APP_ID.put("970456", "timo");     // Timo
            BIN_TO_APP_ID.put("970439", "vib");      // MyVIB cũ
            BIN_TO_APP_ID.put("970446", "shbvn");    // Shinhan
            BIN_TO_APP_ID.put("970428", "nab");      // Nam A Bank
            BIN_TO_APP_ID.put("970424", "abb");      // ABB
            BIN_TO_APP_ID.put("970430", "eib");      // Eximbank
            BIN_TO_APP_ID.put("970458", "coopbank"); // Co-opBank
            BIN_TO_APP_ID.put("970460", "pvcb");     // PVcomBank
            BIN_TO_APP_ID.put("970433", "wvn");      // Woori
            BIN_TO_APP_ID.put("970451", "klb");      // KienlongBank
            BIN_TO_APP_ID.put("970459", "bvb");      // Bao Viet Bank
            BIN_TO_APP_ID.put("970453", "vab");      // VietABank
            BIN_TO_APP_ID.put("970461", "ncb");      // NCB
            BIN_TO_APP_ID.put("970462", "oceanbank"); // Oceanbank
            BIN_TO_APP_ID.put("970464", "pbvn");     // Public Bank
            BIN_TO_APP_ID.put("970465", "sgicb");    // SaigonBank
            BIN_TO_APP_ID.put("970447", "cimb");     // CIMB
            
            Log.d(TAG, "initializeFromJson: Mapped " + BIN_TO_APP_ID.size() + " BINs to app IDs");
            Log.d(TAG, "initializeFromJson: Total banks in registry: " + BANK_BY_APP_ID.size());

        } catch (JSONException e) {
            Log.e(TAG, "Failed to parse bank JSON", e);
        }
    }

    public static BankApp getBankByAppId(String appId) {
        BankApp result = BANK_BY_APP_ID.get(appId);
        Log.d(TAG, "getBankByAppId: " + appId + " -> " + (result != null ? result.appName : "null"));
        return result;
    }

    public static BankApp getBankByBIN(String bin) {
        Log.d(TAG, "getBankByBIN: Looking up BIN = " + bin);
        String appId = BIN_TO_APP_ID.get(bin);
        Log.d(TAG, "getBankByBIN: BIN " + bin + " mapped to appId = " + appId);
        
        BankApp result = appId != null ? BANK_BY_APP_ID.get(appId) : null;
        Log.d(TAG, "getBankByBIN: Result = " + (result != null ? result.appName : "null"));
        return result;
    }

    public static List<BankApp> getAllBanks() {
        return new ArrayList<>(BANK_BY_APP_ID.values());
    }
}
