package com.yalingunayer.cgj2ex.elixir;

import com.yalingunayer.cgj2ex.Utils;

public interface Module {
    String getPackageName();

    String getName();

    ModuleType getType();

    default String getFullName() {
        return getPackageName() + "." + getName();
    }

    default String getOriginalFileUrl() {
        var filePath = getFullName().replaceAll("\\.", "/") + ".java";
        return "https://github.com/craftgate/craftgate-java-client/blob/master/src/main/java/" + filePath;
    }

    default String getNamespace() {
        return Utils.packageNameToNamespace(getPackageName());
    }

    default String getModuleName() {
        return Utils.toTitleCase(this.getName());
    }

}
