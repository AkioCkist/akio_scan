package com.example.akio_scan;

import android.app.Application;
import android.util.Log;
import com.example.akio_scan.bank.BankAppRegistry;
import java.io.InputStream;

public class MyApplication extends Application {
    private static final String TAG = "MyApplication";
    
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: Application starting");
        
        // Khởi tạo danh sách ngân hàng từ file assets/bank.json
        try {
            Log.d(TAG, "onCreate: Loading bank.json from assets");
            InputStream is = getAssets().open("bank.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String json = new String(buffer, "UTF-8");
            
            Log.d(TAG, "onCreate: JSON loaded, size = " + json.length() + " characters");
            BankAppRegistry.initializeFromJson(json);
            Log.d(TAG, "onCreate: BankAppRegistry initialized successfully");
            
        } catch (Exception e) {
            Log.e(TAG, "onCreate: Failed to initialize BankAppRegistry", e);
            e.printStackTrace();
        }
    }
}
