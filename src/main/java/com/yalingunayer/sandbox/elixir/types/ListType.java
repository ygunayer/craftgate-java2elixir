package com.yalingunayer.sandbox.elixir.types;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@RequiredArgsConstructor
public class ListType implements Type {
    @Setter
    private List<Type> inner = new ArrayList<>();

    @Override
    public String getTypeSpec() {
        return "[" + this.inner.stream().map(Type::getTypeSpec).collect(Collectors.joining(", ")) + "]";
    }
}
