package com.yalingunayer.cgj2ex.elixir;

import com.yalingunayer.cgj2ex.Utils;
import com.yalingunayer.cgj2ex.elixir.types.Type;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class Field {
    private final String name;

    private final Type type;

    public String getElixirName() {
        return Utils.toSnakeCase(this.name);
    }
}
