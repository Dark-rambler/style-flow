package com.styloflow.shared.infrastructure.persistence;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Map;
import java.util.stream.Collectors;

@Converter
public abstract class DbEnumConverter<E extends Enum<E>> implements AttributeConverter<E, String> {

    private final Map<E, String> toDb;
    private final Map<String, E> fromDb;

    protected DbEnumConverter(Map<E, String> mapping) {
        this.toDb = Map.copyOf(mapping);
        this.fromDb = mapping.entrySet().stream().collect(Collectors.toUnmodifiableMap(Map.Entry::getValue, Map.Entry::getKey));
    }

    @Override
    public String convertToDatabaseColumn(E value) {
        return value == null ? null : toDb.get(value);
    }

    @Override
    public E convertToEntityAttribute(String value) {
        if (value == null) {
            return null;
        }
        E result = fromDb.get(value);
        if (result == null) {
            throw new IllegalArgumentException("Unknown database value: " + value);
        }
        return result;
    }
}
