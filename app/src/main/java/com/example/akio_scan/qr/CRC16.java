package com.example.akio_scan.qr;

public class CRC16 {
    public static String calculateCRC16(String data) {
        int crc = 0xFFFF;
        for (int i = 0; i < data.length(); i++) {
            char c = data.charAt(i);
            crc ^= ((int) c) << 8;
            for (int j = 0; j < 8; j++) {
                if ((crc & 0x8000) != 0) {
                    crc = (crc << 1) ^ 0x1021;
                } else {
                    crc <<= 1;
                }
            }
            crc &= 0xFFFF;
        }
        return String.format("%04X", crc);
    }

    public static boolean isValid(String qrContent) {
        if (qrContent == null || qrContent.length() < 8) return false;

        int crcIndex = qrContent.lastIndexOf("63");
        if (crcIndex == -1 || crcIndex + 8 > qrContent.length()) return false;

        String lenStr = qrContent.substring(crcIndex + 2, crcIndex + 4);
        try {
            int len = Integer.parseInt(lenStr);
            if (len != 4) return false;

            String payload = qrContent.substring(0, crcIndex + 4);
            String expected = qrContent.substring(crcIndex + 4, crcIndex + 8);
            String computed = calculateCRC16(payload);

            return computed.equalsIgnoreCase(expected);
        } catch (Exception e) {
            return false;
        }
    }
}