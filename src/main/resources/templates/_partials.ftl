<#assign Utils=statics['com.yalingunayer.cgj2ex.Utils']>

<#macro show_enum enum indent=1 useFullName=false>
<#local spc>${""?left_pad(indent * 2)}</#local>
${spc}defmodule <#if useFullName>${enum.namespace}.</#if>${enum.name} do
  ${spc}use Craftgate.Enum, [
    <#list enum.values as value>
    ${spc}:${value}<#sep>,
  </#list>

  ${spc}]
${spc}end
</#macro>

<#macro show_class class indent=1 useFullName=false>
<#local spc>${""?left_pad(indent * 2)}</#local>
defmodule <#if useFullName>${class.namespace}.</#if>${class.name} do
  use Craftgate.Serializable, [
  <#list class.fields as field>
  ${spc}${field.elixirName}: ${field.type.deserializationSpec}<#sep>,
  </#list>

  ]
end
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
