package com.yalingunayer.cgj2ex.elixir.types;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Map;

@Getter
@RequiredArgsConstructor
public enum BasicType implements Type {
    INTEGER(":integer"),
    FLOAT(":float"),
    BOOLEAN(":boolean"),
    STRING(":string"),
    BINARY(":binary"),
    DECIMAL(":decimal"),
    UTC_DATETIME(":utc_datetime"),
    NAIVE_DATETIME(":naive_datetime"),
    DATE(":date"),
    TIME(":time"),
    ANY(":any"),
    ARRAY(":array"),
    MAP(":map"),
    STRUCT(":struct");

    private final String typeSpec;

    public static BasicType from(Class<?> clazz) {
        if (String.class.equals(clazz)) {
            return STRING;
        }

        if (Integer.class.equals(clazz) || Long.class.equals(clazz) ||
                int.class.equals(clazz) || long.class.equals(clazz)) {
            return INTEGER;
        }

        if (BigDecimal.class.equals(clazz)) {
            return DECIMAL;
        }

        if (Double.class.equals(clazz) || Float.class.equals(clazz) ||
                double.class.equals(clazz) || float.class.equals(clazz)) {
            return FLOAT;
        }

        if (Boolean.class.equals(clazz) || boolean.class.equals(clazz)) {
            return BOOLEAN;
        }

        if (clazz.isAssignableFrom(Map.class)) {
            return MAP;
        }

        if (LocalDateTime.class.equals(clazz) || ZonedDateTime.class.equals(clazz)) {
            return NAIVE_DATETIME;
        }

        if (LocalDate.class.equals(clazz) || Date.class.equals(clazz)) {
            return DATE;
        }

        return ANY;
    }
}
