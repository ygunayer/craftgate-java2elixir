package com.yalingunayer.sandbox.elixir.types;

import com.yalingunayer.sandbox.Utils;
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

    public String getNamespace() {
        return Utils.packageNameToNamespace(this.packageName);
    }

    public AliasedTypeRef aliased() {
        return new AliasedTypeRef(this.packageName, this.typeName);
    }
}
