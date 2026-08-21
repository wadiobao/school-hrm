package com.kltn.school_hrm.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.kltn.school_hrm.utils.AesEncryptor;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.RequiredArgsConstructor;

@Converter
@Component
@RequiredArgsConstructor
public class CccdEncryptionConverter implements AttributeConverter<String, String> {

    private final AesEncryptor aesEncryptor;

    @Override
    public String convertToDatabaseColumn(String attribute) {
        return aesEncryptor.encrypt(attribute); // Tự mã hóa khi lưu DB
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        return aesEncryptor.decrypt(dbData); // Tự giải mã khi đọc từ DB
    }
}
