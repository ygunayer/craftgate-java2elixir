package com.yalingunayer.cgj2ex.elixir;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.List;

@Getter
@Builder
@RequiredArgsConstructor
@ToString
public class ClassModule implements Module {
    private final ModuleType type = ModuleType.CLASS;

    private final String packageName;
    private final String name;
    private final List<Field> fields;

    public boolean requiresNestedDeserialization() {
        return fields.stream().anyMatch(field -> field.getType().requiresNestedDeserialization());
    }
}
