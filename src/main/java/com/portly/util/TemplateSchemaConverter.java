package com.portly.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.portly.domain.entity.TemplateSchema;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Convierte TemplateSchema ↔ JSON string para almacenamiento en columna TEXT.
 * Reutiliza el mismo patrón que StringListConverter.
 */
@Converter
public class TemplateSchemaConverter implements AttributeConverter<TemplateSchema, String> {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(TemplateSchema schema) {
        if (schema == null) return "{}";
        try {
            return MAPPER.writeValueAsString(schema);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    @Override
    public TemplateSchema convertToEntityAttribute(String json) {
        if (json == null || json.isBlank()) return new TemplateSchema();
        try {
            return MAPPER.readValue(json, TemplateSchema.class);
        } catch (JsonProcessingException e) {
            return new TemplateSchema();
        }
    }
}
