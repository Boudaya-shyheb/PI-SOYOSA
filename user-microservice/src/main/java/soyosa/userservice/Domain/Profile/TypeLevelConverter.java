package soyosa.userservice.Domain.Profile;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class TypeLevelConverter implements AttributeConverter<TypeLevel, String> {

    @Override
    public String convertToDatabaseColumn(TypeLevel attribute) {
        if (attribute == null) {
            return TypeLevel.NONE.name();
        }
        return attribute.name();
    }

    @Override
    public TypeLevel convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            return TypeLevel.NONE;
        }
        try {
            return TypeLevel.valueOf(dbData.trim());
        } catch (IllegalArgumentException e) {
            // Log fallback or handle unknown values silently
            System.err.println("Unknown TypeLevel in database: [" + dbData + "]. Mapping to NONE.");
            return TypeLevel.NONE;
        }
    }
}
