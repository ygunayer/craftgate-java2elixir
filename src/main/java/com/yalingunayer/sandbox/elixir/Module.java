package com.yalingunayer.sandbox.elixir;

import com.yalingunayer.sandbox.Utils;

import java.util.Collections;
import java.util.stream.Collectors;

public interface Module {
    String getPackageName();

    String getName();

    ModuleType getType();

    default String getFullName() {
        return getPackageName() + "." + getName();
    }

    default String getNamespace() {
        return Utils.packageNameToNamespace(getPackageName());
    }

    default String getModuleName() {
        return Utils.toTitleCase(this.getName());
    }

}
