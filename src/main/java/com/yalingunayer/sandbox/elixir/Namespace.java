package com.yalingunayer.sandbox.elixir;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class Namespace {
    private final String name;
    private final List<Module> modules;

    public List<EnumModule> getEnums() {
        return this.modules
                .stream()
                .filter(module -> module instanceof EnumModule)
                .map(EnumModule.class::cast)
                .toList();
    }

    public List<ClassModule> getClasses() {
        return this.modules
                .stream()
                .filter(module -> module instanceof ClassModule)
                .map(ClassModule.class::cast)
                .toList();
    }
}
