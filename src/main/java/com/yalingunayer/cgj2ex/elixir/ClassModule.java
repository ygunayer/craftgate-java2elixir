package com.yalingunayer.cgj2ex.elixir;

import com.yalingunayer.cgj2ex.elixir.types.Type;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Map;

@Getter
@Builder
@RequiredArgsConstructor
@ToString
public class ClassModule implements Module {
    private final ModuleType type = ModuleType.CLASS;

    private final String packageName;
    private final String name;
    private final Map<String, Type> exposedFields;
}