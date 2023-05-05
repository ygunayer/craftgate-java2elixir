package com.yalingunayer.cgj2ex.elixir.types;

public interface Type {
    String getTypeSpec();

    default String getDeserializationSpec() {
        return getTypeSpec();
    }

    default boolean requiresAlias() {
        return false;
    }

    default boolean requiresNestedDeserialization() {
        return false;
    }

    default String getDefaultValue() {
        return "nil";
    }
}
