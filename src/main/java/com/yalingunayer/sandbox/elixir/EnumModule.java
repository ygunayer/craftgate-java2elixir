package com.yalingunayer.sandbox.elixir;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.List;
import java.util.Set;

@Getter
@Builder
@RequiredArgsConstructor
@ToString
public class EnumModule implements Module {
    private final ModuleType type = ModuleType.ENUM;

    private final String packageName;
    private final String name;
    private final Set<String> values;

    public List<String> getValueAtoms() {
        return values.stream().map(value -> ":" + value).toList();
    }
}
