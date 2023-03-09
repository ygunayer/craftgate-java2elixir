package com.yalingunayer.sandbox.elixir.types;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BasicType implements Type {
    STRING("String.t()"),
    INTEGER("integer()"),
    FLOAT("float()"),
    BOOLEAN("boolean()"),
    LIST("list()"),
    MAP("map()"),
    TERM("term()");

    private final String typeSpec;
}
