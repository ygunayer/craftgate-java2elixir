package com.yalingunayer.cgj2ex.elixir;

import com.yalingunayer.cgj2ex.Utils;

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
