# Akio Scan - Vietnamese Banking QR Code Scanner

**Scan how you like, we'll spend it right!** 💳

A modern Android application for scanning and processing Vietnamese banking QR codes (VietQR) with automatic bank app integration.

## ✨ Features

- 📷 **Camera Scanning** - Real-time QR code scanning with camera
- 🖼️ **Gallery Import** - Select QR code images from your gallery
- 🔗 **Share Support** - Share QR images directly to the app from other apps
- 🏦 **Banking Integration** - Automatically open supported bank apps with pre-filled payment data
- 💰 **Smart Parsing** - Extract account number, amount, purpose, and bank details
- 📋 **Copy to Clipboard** - Tap any field to copy instantly
- 🎨 **Modern UI** - Clean Material Design 3 interface

## 🏦 Supported Banks

The following banks support automatic app opening with pre-filled data:

| Bank | App Name | BIN Code(s) | Status |
|------|----------|-------------|--------|
| OCB | OCB OMNI - Digital Bank | 970443 | ✅ Enabled |
| ACB | ACB One | 970437 | ✅ Enabled |
| MB Bank | MB Bank | 970422, 970436 | ✅ Enabled |
| BIDV | BIDV SmartBanking | 970418 | ✅ Enabled |
| Vietcombank | Vietcombank | 970416 | ⚪ View only |
| Techcombank | Techcombank Mobile | 970423 | ⚪ View only |
| VPBank | VPBank NEO | 970420 | ⚪ View only |
| VietinBank | VietinBank iPay | 970415 | ⚪ View only |

*And 20+ more banks for viewing QR data*

## 📱 Screenshots

- **Home Screen**: Scan with camera or pick from gallery
- **Result Display**: View parsed QR code information
- **Bank Integration**: One-tap to open bank app with pre-filled data
- **Share Support**: Process QR codes shared from other apps

## 🛠️ Tech Stack

- **Language**: Java
- **Min SDK**: Android 8.0 (API 26)
- **Target SDK**: Android 14 (API 34)
- **Architecture**: Activity-based with helper classes
- **QR Scanner**: ZXing (Zebra Crossing)
- **UI**: Material Design 3 Components

## 📋 Prerequisites

Before building the app, ensure you have:

- **Android Studio** Hedgehog (2023.1.1) or newer
- **JDK** 17 or higher
- **Android SDK** with API 34 installed
- **Gradle** 8.2 or higher (included with Android Studio)

## 🚀 Installation & Build

### 1. Clone the Repository

```bash
git clone https://github.com/AkioCkist/akio_scan.git
cd akio_scan
```

### 2. Open in Android Studio

1. Launch Android Studio
2. Click **File** → **Open**
3. Navigate to the cloned `akio_scan` folder
4. Click **OK**
5. Wait for Gradle sync to complete

### 3. Configure SDK

1. Open **File** → **Project Structure**
2. Under **Project**, ensure:
   - **SDK Location** points to your Android SDK
   - **Gradle Version** is 8.2+
3. Under **Modules** → **app**, verify:
   - **Compile SDK Version**: 34
   - **Min SDK Version**: 26
   - **Target SDK Version**: 34

### 4. Sync Dependencies

```bash
# In Android Studio terminal or PowerShell
./gradlew clean build
```

Or click **File** → **Sync Project with Gradle Files**

### 5. Build the App

#### Option A: Using Android Studio
1. Click **Build** → **Build Bundle(s) / APK(s)** → **Build APK(s)**
2. Wait for build to complete
3. Find APK in `app/build/outputs/apk/debug/app-debug.apk`

#### Option B: Using Command Line (PowerShell)

```powershell
# Build debug APK
./gradlew assembleDebug

# Build release APK (requires signing configuration)
./gradlew assembleRelease
```

### 6. Install on Device

#### Option A: Using Android Studio
1. Connect your Android device via USB (enable USB debugging)
2. Click the **Run** button (▶️) or press `Shift + F10`
3. Select your device from the list

#### Option B: Using ADB

```powershell
# Install debug APK
adb install app/build/outputs/apk/debug/app-debug.apk

# Or use gradlew
./gradlew installDebug
```

## 🔧 Configuration

### Enable More Banks for Auto-Open

To enable the "Open in Bank" button for additional banks:

1. Open `app/src/main/assets/bank.json`
2. Find the bank you want to enable
3. Change `"autofill": 0` to `"autofill": 1`
4. Rebuild the app

**Example:**
```json
{
  "appId": "vcb",
  "appName": "Vietcombank",
  "bankName": "Ngân hàng TMCP Ngoại Thương Việt Nam",
  "deeplink": "https://dl.vietqr.io/pay?app=vcb",
  "autofill": 1  // Changed from 0 to 1
}
```

### Add New Bank BIN Codes

If you encounter a QR code with an unrecognized BIN:

1. Open `app/src/main/java/com/example/akio_scan/bank/BankAppRegistry.java`
2. Add the mapping in the `initializeFromJson()` method:

```java
BIN_TO_APP_ID.put("970xxx", "bank_id");  // Your bank's BIN
```

## 📂 Project Structure

```
akio_scan/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/akio_scan/
│   │   │   │   ├── MainActivity.java              # Main screen
│   │   │   │   ├── MyApplication.java             # App initialization
│   │   │   │   ├── bank/
│   │   │   │   │   ├── BankApp.java               # Bank model
│   │   │   │   │   └── BankAppRegistry.java       # Bank registry & BIN mapping
│   │   │   │   ├── qr/
│   │   │   │   │   ├── QRCodeData.java            # QR data model
│   │   │   │   │   ├── QRCodeParser.java          # VietQR parser
│   │   │   │   │   └── CRC16.java                 # CRC validation
│   │   │   │   ├── scanner/
│   │   │   │   │   └── ImageQrDecoder.java        # Image QR decoder
│   │   │   │   ├── share/
│   │   │   │   │   └── ShareReceiverActivity.java # Handle shared images
│   │   │   │   └── ui/
│   │   │   │       └── ResultDisplayHelper.java   # UI helper
│   │   │   ├── res/
│   │   │   │   ├── layout/
│   │   │   │   │   ├── activity_main.xml          # Main layout
│   │   │   │   │   ├── activity_share_receiver.xml
│   │   │   │   │   └── result_card_content.xml
│   │   │   │   ├── values/                         # Colors, strings, themes
│   │   │   │   └── drawable/                       # Icons & backgrounds
│   │   │   ├── assets/
│   │   │   │   └── bank.json                       # Bank database
│   │   │   └── AndroidManifest.xml
│   │   └── androidTest/                            # Android tests
│   └── build.gradle.kts                            # App dependencies
├── gradle/                                          # Gradle wrapper
├── build.gradle.kts                                # Project Gradle
└── settings.gradle.kts
```

## 🔍 How It Works

### QR Code Processing Flow

1. **Scan/Import**: User scans QR with camera or picks image from gallery
2. **Decode**: ZXing library decodes the QR code into raw string
3. **Parse**: `QRCodeParser` extracts fields following VietQR EMVCo standard:
   - BIN (Bank Identification Number)
   - Account number
   - Amount (if present)
   - Purpose/message (if present)
4. **Validate**: CRC16 checksum validation
5. **Display**: Show parsed information in UI
6. **Bank Lookup**: Check if bank supports auto-open feature
7. **Deep Link**: Generate deep link with QR data if supported
8. **Launch**: Open bank app with pre-filled payment data

### Bank Integration

The app uses VietQR deep links to communicate with bank apps:

```
https://dl.vietqr.io/pay?app={bank_id}&data={encoded_qr_content}
```

Banks that support this standard will automatically receive and process the payment data.

## 🐛 Debugging

### Enable Verbose Logging

The app includes comprehensive logging. Filter by tags in Logcat:

```powershell
# View all app logs
adb logcat | Select-String "MainActivity|BankAppRegistry|MyApplication"

# View specific component
adb logcat -s MainActivity:D
```

### Common Issues

**Button doesn't appear after scanning:**
- Check if bank has `"autofill": 1` in `bank.json`
- Verify BIN is mapped in `BankAppRegistry.java`
- Check Logcat for detailed step-by-step logs

**QR code not recognized:**
- Ensure image quality is good
- Verify QR follows VietQR/EMVCo standard
- Check CRC validation in logs

**Build fails:**
- Run `./gradlew clean`
- Invalidate caches: **File** → **Invalidate Caches** → **Invalidate and Restart**
- Update Android Gradle Plugin

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 👨‍💻 Author

**AkioCkist**
- GitHub: [@AkioCkist](https://github.com/AkioCkist)

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

### Development Guidelines

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📞 Support

If you encounter any issues or have questions:
- Open an issue on GitHub
- Check existing issues for solutions
- Review the debug logs using the logging commands above

## 🎯 Roadmap

- [ ] Add support for more banks
- [ ] Transaction history
- [ ] QR code generation
- [ ] Multiple language support
- [ ] Dark mode improvements
- [ ] Widget support

## 🙏 Acknowledgments

- **ZXing** - QR code scanning library
- **Material Design 3** - UI components
- **VietQR** - Vietnamese QR payment standard
- All the bank apps that support deep linking

---

Made with ❤️ in Vietnam 🇻🇳
