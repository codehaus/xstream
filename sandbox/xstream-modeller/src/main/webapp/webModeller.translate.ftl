<#macro tipify name>${name?cap_first}</#macro>

<html>

Custom xstream instance:<br/>

<@html>
public class CustomXStream extends XStream {

	public CustomXStream() {
	
		<#list graph.elements as node>
			<#assign typ = node.type>
			<#assign name = typ.name>
			<#if !typ.implicitCollection>
		alias("${name}", <@tipify name/>.class);
				<#list typ.children as child>
					<#assign childType = child.type>
					<#-- TODO nasty... should use some type of enum? factory? anything else -->
					<#if childType.class.name == "org.codehaus.xstream.modeller.dom.Element">
						<#if childType.implicitCollection>
		addImplicitCollection("${childType.name}", "${childType.name}", <@tipify name/>.class);
						</#if>
					<#elseif childType.class.name == "org.codehaus.xstream.modeller.dom.Attribute">
		useAttributeFor("${childType.name}", "${childType.name}", <@tipify name/>.class);
					<#else>
						<#-- no config for this type -->
					</#if>
				</#list>
			</#if>
		</#list>
		
	}

}
</@html>

	<hr/>

	Java model:

	<hr/>

	<#include "form.ftl"/>
	
	
</html>
