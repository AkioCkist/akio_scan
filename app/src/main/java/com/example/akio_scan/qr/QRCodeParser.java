package com.example.akio_scan.qr;

import java.util.HashMap;
import java.util.Map;

public class QRCodeParser {

    public static QRCodeData parse(String qrData) {
        if (!CRC16.isValid(qrData)) {
            throw new IllegalArgumentException("Invalid CRC checksum");
        }

        QRCodeData result = new QRCodeData();

        // Remove CRC part for parsing
        String payload = qrData.substring(0, qrData.lastIndexOf("6304") + 4);

        Map<String, String> topLevel = parseTLV(payload);

        // Consumer Account Info (ID = "38")
        String consumerInfo = topLevel.get("38");
        if (consumerInfo != null) {
            Map<String, String> consumerTLV = parseTLV(consumerInfo);
            String paymentNetwork = consumerTLV.get("00"); // GUID (e.g., A000000727)
            String bankAndAccount = consumerTLV.get("01");

            if (bankAndAccount != null) {
                Map<String, String> bankAccountTLV = parseTLV(bankAndAccount);
                String bin = bankAccountTLV.get("00"); // e.g., "970423"
                String account = bankAccountTLV.get("01"); // e.g., "mynamebvh"

                result.setBankBIN(bin);
                result.setAccountNumber(account);

                // Optional: Map BIN to bank name
                result.setBankName(resolveBankName(bin));
            }
        }

        // Amount (ID = "54")
        result.setAmount(topLevel.get("54"));

        // Currency (ID = "53")
        result.setCurrency(topLevel.get("53"));

        // Purpose (inside ID = "62")
        String additionalData = topLevel.get("62");
        if (additionalData != null) {
            Map<String, String> additionalTLV = parseTLV(additionalData);
            result.setPurpose(additionalTLV.get("08")); // ID 08 = purpose
        }

        // Amount may be missing (static QR)
        String amount = topLevel.get("54");
        result.setAmount(amount); // can be null

        return result;
    }

    private static Map<String, String> parseTLV(String data) {
        Map<String, String> map = new HashMap<>();
        int index = 0;
        while (index < data.length()) {
            if (index + 4 > data.length()) break;

            String tag = data.substring(index, index + 2);
            String lenStr = data.substring(index + 2, index + 4);
            int len = Integer.parseInt(lenStr);
            index += 4;

            if (index + len > data.length()) break;

            String value = data.substring(index, index + len);
            map.put(tag, value);
            index += len;
        }
        return map;
    }

    private static String resolveBankName(String bin) {
        if (bin == null || bin.isEmpty()) return "Unknown";

        // Maps BIN (Bank Identification Number) to a short, human-readable name.
        switch (bin) {
            case "970415": return "VietinBank"; // Ngân hàng TMCP Công thương Việt Nam
            case "970436": return "Vietcombank"; // Ngân hàng TMCP Ngoại Thương Việt Nam
            case "970418": return "BIDV"; // Ngân hàng TMCP Đầu tư và Phát triển Việt Nam
            case "970405": return "Agribank"; // Ngân hàng Nông nghiệp và Phát triển Nông thôn Việt Nam
            case "970448": return "OCB"; // Ngân hàng TMCP Phương Đông
            case "970422": return "MBBank"; // Ngân hàng TMCP Quân đội
            case "970407": return "Techcombank"; // Ngân hàng TMCP Kỹ thương Việt Nam
            case "970416": return "ACB"; // Ngân hàng TMCP Á Châu
            case "970432": return "VPBank"; // Ngân hàng TMCP Việt Nam Thịnh Vượng
            case "970423": return "TPBank"; // Ngân hàng TMCP Tiên Phong
            case "970403": return "Sacombank"; // Ngân hàng TMCP Sài Gòn Thương Tín
            case "970437": return "HDBank"; // Ngân hàng TMCP Phát triển Thành phố Hồ Chí Minh
            case "970454": return "VietCapitalBank"; // Ngân hàng TMCP Bản Việt
            case "970429": return "SCB"; // Ngân hàng TMCP Sài Gòn
            case "970441": return "VIB"; // Ngân hàng TMCP Quốc tế Việt Nam
            case "970443": return "SHB"; // Ngân hàng TMCP Sài Gòn - Hà Nội
            case "970431": return "Eximbank"; // Ngân hàng TMCP Xuất Nhập khẩu Việt Nam
            case "970426": return "MSB"; // Ngân hàng TMCP Hàng Hải Việt Nam
            case "546034": return "CAKE"; // TMCP Việt Nam Thịnh Vượng - Ngân hàng số CAKE by VPBank
            case "546035": return "Ubank"; // TMCP Việt Nam Thịnh Vượng - Ngân hàng số Ubank by VPBank
            case "971005": return "ViettelMoney"; // Tổng Công ty Dịch vụ số Viettel
            case "963388": return "Timo"; // Ngân hàng số Timo by Ban Viet Bank
            case "971011": return "VNPTMoney"; // VNPT Money
            case "970400": return "SaigonBank"; // Ngân hàng TMCP Sài Gòn Công Thương
            case "970409": return "BacABank"; // Ngân hàng TMCP Bắc Á
            case "971025": return "MoMo"; // CTCP Dịch Vụ Di Động Trực Tuyến
            case "971133": return "PVcomBank Pay"; // Ngân hàng TMCP Đại Chúng Việt Nam Ngân hàng số
            case "970412": return "PVcomBank"; // Ngân hàng TMCP Đại Chúng Việt Nam
            case "970414": return "MBV"; // Ngân hàng TNHH MTV Việt Nam Hiện Đại
            case "970419": return "NCB"; // Ngân hàng TMCP Quốc Dân
            case "970424": return "ShinhanBank"; // Ngân hàng TNHH MTV Shinhan Việt Nam
            case "970425": return "ABBANK"; // Ngân hàng TMCP An Bình
            case "970427": return "VietABank"; // Ngân hàng TMCP Việt Á
            case "970428": return "NamABank"; // Ngân hàng TMCP Nam Á
            case "970430": return "PGBank"; // Ngân hàng TMCP Thịnh vượng và Phát triển
            case "970433": return "VietBank"; // Ngân hàng TMCP Việt Nam Thương Tín
            case "970438": return "BaoVietBank"; // Ngân hàng TMCP Bảo Việt
            case "970440": return "SeABank"; // Ngân hàng TMCP Đông Nam Á
            case "970446": return "COOPBANK"; // Ngân hàng Hợp tác xã Việt Nam
            case "970449": return "LPBank"; // Ngân hàng TMCP Lộc Phát Việt Nam
            case "970452": return "KienLongBank"; // Ngân hàng TMCP Kiên Long
            case "668888": return "KBank"; // Ngân hàng Đại chúng TNHH Kasikornbank
            case "977777": return "MAFC"; // Công ty Tài chính TNHH MTV Mirae Asset
            case "970442": return "HongLeong"; // Ngân hàng TNHH MTV Hong Leong Việt Nam
            case "970467": return "KEBHANAHN"; // Ngân hàng KEB Hana – Chi nhánh Hà Nội
            case "970466": return "KEBHanaHCM"; // Ngân hàng KEB Hana – Chi nhánh TP. Hồ Chí Minh
            case "533948": return "Citibank"; // Ngân hàng Citibank, N.A. - Chi nhánh Hà Nội
            case "970444": return "CBBank"; // Ngân hàng Thương mại TNHH MTV Xây dựng Việt Nam
            case "422589": return "CIMB"; // Ngân hàng TNHH MTV CIMB Việt Nam
            case "796500": return "DBSBank"; // DBS Bank Ltd - Chi nhánh TP. Hồ Chí Minh
            case "970406": return "Vikki"; // Ngân hàng TNHH MTV Số Vikki
            case "999888": return "VBSP"; // Ngân hàng Chính sách Xã hội
            case "970408": return "GPBank"; // Ngân hàng Thương mại TNHH MTV Dầu Khí Toàn Cầu
            case "970463": return "KookminHCM"; // Ngân hàng Kookmin - Chi nhánh TP. Hồ Chí Minh
            case "970462": return "KookminHN"; // Ngân hàng Kookmin - Chi nhánh Hà Nội
            case "970457": return "Woori"; // Ngân hàng TNHH MTV Woori Việt Nam
            case "970421": return "VRB"; // Ngân hàng Liên doanh Việt - Nga
            case "458761": return "HSBC"; // Ngân hàng TNHH MTV HSBC (Việt Nam)
            case "970455": return "IBKHN"; // Ngân hàng Công nghiệp Hàn Quốc - Chi nhánh Hà Nội
            case "970456": return "IBKHCM"; // Ngân hàng Công nghiệp Hàn Quốc - Chi nhánh TP. Hồ Chí Minh
            case "970434": return "IndovinaBank"; // Ngân hàng TNHH Indovina
            case "970458": return "UnitedOverseas"; // Ngân hàng United Overseas - Chi nhánh TP. Hồ Chí Minh
            case "801011": return "Nonghyup"; // Ngân hàng Nonghyup - Chi nhánh Hà Nội
            case "970410": return "StandardChartered"; // Ngân hàng TNHH MTV Standard Chartered Bank Việt Nam
            case "970439": return "PublicBank"; // Ngân hàng TNHH MTV Public Việt Nam
            default: return "Bank (" + bin + ")";
        }
    }
}
