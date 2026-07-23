// src/main/java/com/shopzen/ecommerce_api/config/UUIDConverter.java
package com.shopzen.ecommerce_api.config;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.UUID;

@Converter(autoApply = true)
public class UUIDConverter implements AttributeConverter<UUID, String> {

    @Override
    public String convertToDatabaseColumn(UUID attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.toString();
    }

    @Override
    public UUID convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return null;
        }
        try {
            return UUID.fromString(dbData);
        } catch (IllegalArgumentException e) {
            // Handle invalid UUID format
            return null;
        }
    }
}