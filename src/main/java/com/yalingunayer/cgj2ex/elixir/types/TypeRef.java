package com.yalingunayer.cgj2ex.elixir.types;

import com.yalingunayer.cgj2ex.Utils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class TypeRef implements Type {
    private final String packageName;
    private final String typeName;

    @Override
    public String getTypeSpec() {
        return getNamespace() + "." + typeName + ".t()";
    }

    @Override
    public String getDeserializationSpec() {
        return "\"" + getNamespace() + "." + typeName +  "\"";
    }

    public String getNamespace() {
        return Utils.packageNameToNamespace(this.packageName);
    }

    public AliasedTypeRef aliased() {
        return new AliasedTypeRef(this.packageName, this.typeName);
    }

    @Override
    public boolean requiresNestedDeserialization() {
        return true;
    }
}
