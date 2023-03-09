package com.yalingunayer.sandbox.elixir.types;

public class AliasedTypeRef extends TypeRef {
    public AliasedTypeRef(String packageName, String typeName) {
        super(packageName, typeName);
    }

    public String getAliasName() {
        return this.getNamespace() + "." + this.getTypeName();
    }

    @Override
    public String getTypeSpec() {
        return this.getTypeName() + ".t()";
    }
}
