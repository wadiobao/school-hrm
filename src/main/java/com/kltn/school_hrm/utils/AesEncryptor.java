package com.kltn.school_hrm.utils;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.codec.Hex;
import org.springframework.stereotype.Component;

@Component
public class AesEncryptor {
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int TAG_LENGTH_BIT = 128;
    private static final int IV_LENGTH_BYTE = 12;

    private final SecretKeySpec secretKey;

    // Inject secretKey từ application.properties
    public AesEncryptor(@Value("${app.security.aes.secret-key}") String secretKeyConfig) {
        if (secretKeyConfig == null || secretKeyConfig.length() != 64) {
            throw new IllegalArgumentException("Secret key mã hóa AES dạng Hex phải có độ dài đúng 64 ký tự!");
        }

        try {
            // Chuyển Hex sang byte[]
            byte[] keyBytes = Hex.decode(secretKeyConfig);
            this.secretKey = new SecretKeySpec(keyBytes, "AES");
        } catch (Exception e) {
            throw new IllegalArgumentException("Chuỗi cấu hình không phải định dạng mã Hex hợp lệ!", e);
        }
    }

    // Hàm Mã hóa
    public String encrypt(String plainText) {
        if (plainText == null || plainText.isEmpty())
            return null;
        try {
            byte[] iv = new byte[IV_LENGTH_BYTE];
            new SecureRandom().nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec spec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec);

            byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            // Nối IV và CipherText lại với nhau
            byte[] cipherTextWithIv = new byte[iv.length + cipherText.length];
            System.arraycopy(iv, 0, cipherTextWithIv, 0, iv.length);
            System.arraycopy(cipherText, 0, cipherTextWithIv, iv.length, cipherText.length);

            return Base64.getEncoder().encodeToString(cipherTextWithIv);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi mã hóa dữ liệu AES", e);
        }
    }

    // Hàm Giải mã
    public String decrypt(String cipherTextBase64) {
        if (cipherTextBase64 == null || cipherTextBase64.isEmpty())
            return null;
        try {
            byte[] cipherTextWithIv = Base64.getDecoder().decode(cipherTextBase64);

            byte[] iv = new byte[IV_LENGTH_BYTE];
            System.arraycopy(cipherTextWithIv, 0, iv, 0, iv.length);

            byte[] cipherText = new byte[cipherTextWithIv.length - IV_LENGTH_BYTE];
            System.arraycopy(cipherTextWithIv, IV_LENGTH_BYTE, cipherText, 0, cipherText.length);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec spec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);

            byte[] plainTextBytes = cipher.doFinal(cipherText);
            return new String(plainTextBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi giải mã dữ liệu AES", e);
        }
    }
}
