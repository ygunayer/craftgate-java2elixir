<#assign Utils=statics['com.yalingunayer.cgj2ex.Utils']>

<#macro show_enum enum indent=1>
<#local spc>${""?left_pad(indent * 2)}</#local>
${spc}defmodule ${enum.name} do
  <#list enum.values as value>
  ${spc}def ${Utils.toSnakeCase(value)}(), do: :${value}
  </#list>

  ${spc}@type t :: <#list enum.values as value>:${value}<#sep> | </#list>
${spc}end
</#macro>

<#macro show_class class indent=1>
<#local spc>${""?left_pad(indent * 2)}</#local>
${spc}defmodule ${class.name} do
  ${spc}use Jason.Structs.Struct

  ${spc}jason_struct do
  <#list class.exposedFields as name, field>
    ${spc}field :${Utils.toSnakeCase(name)}, ${field.typeSpec}
  </#list>
  ${spc}end
${spc}end
</#macro>

<#macro show_namespace namespace>
defmodule ${namespace.name} do
  <#list namespace.enums as enum>
    <@show_enum enum=enum />
    <#nt>
  </#list>
  <#list namespace.classes as class>
    <@show_class class=class />
    <#nt>
  </#list>
end
</#macro>
