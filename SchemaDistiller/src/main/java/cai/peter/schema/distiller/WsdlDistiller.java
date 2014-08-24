/***********************************************
 * Copyright (c) 2014 Peter Cai
 * All rights reserved.
 *
 * Aug 22, 2014
 *
 ***********************************************/
package cai.peter.schema.distiller;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.wsdl.Definition;
import javax.wsdl.Message;
import javax.wsdl.Part;
import javax.xml.namespace.QName;

import org.apache.cxf.BusFactory;
import org.apache.cxf.service.model.SchemaInfo;
import org.apache.cxf.service.model.ServiceInfo;
import org.apache.cxf.wsdl11.WSDLServiceBuilder;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaAll;
import org.apache.ws.commons.schema.XmlSchemaChoice;
import org.apache.ws.commons.schema.XmlSchemaComplexType;
import org.apache.ws.commons.schema.XmlSchemaDerivationMethod;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaFacet;
import org.apache.ws.commons.schema.XmlSchemaGroupParticle;
import org.apache.ws.commons.schema.XmlSchemaMaxLengthFacet;
import org.apache.ws.commons.schema.XmlSchemaObject;
import org.apache.ws.commons.schema.XmlSchemaParticle;
import org.apache.ws.commons.schema.XmlSchemaSequence;
import org.apache.ws.commons.schema.XmlSchemaSequenceMember;
import org.apache.ws.commons.schema.XmlSchemaSimpleType;
import org.apache.ws.commons.schema.XmlSchemaSimpleTypeContent;
import org.apache.ws.commons.schema.XmlSchemaSimpleTypeRestriction;
import org.apache.ws.commons.schema.XmlSchemaType;

import cai.peter.schema.model.TypeInfo;
import cai.peter.schema.model.xelement;
import cai.peter.schema.model.xgroup;
import cai.peter.schema.model.xnode;


public class WsdlDistiller
{
	Map<QName, XmlSchemaElement> xelements = new HashMap<QName, XmlSchemaElement>();

	enum CLAZZ{
		XmlSchemaFractionDigitsFacet,
		XmlSchemaMaxLengthFacet,
		XmlSchemaMinLengthFacet,
		XmlSchemaEnumerationFacet,
		XmlSchemaMaxExclusiveFacet,
		XmlSchemaMaxInclusiveFacet,
		XmlSchemaMinExclusiveFacet,
		XmlSchemaMinInclusiveFacet,
		XmlSchemaPatternFacet,
		XmlSchemaLengthFacet,
		XmlSchemaWhiteSpaceFacet,
		XmlSchemaTotalDigitsFacet;
	}
	
	void processSchemas(Definition defs)
	{
		WSDLServiceBuilder wsdlServiceBuilder = new WSDLServiceBuilder(BusFactory.getDefaultBus());
		List<ServiceInfo> serviceInfos = wsdlServiceBuilder.buildServices(defs);
		for( ServiceInfo serviceInfo : serviceInfos)
		{
			List<SchemaInfo> schemas = serviceInfo.getSchemas();
			for( SchemaInfo schemaInfo : schemas )
			{
				XmlSchema schema = schemaInfo.getSchema();
				Map<QName, XmlSchemaElement> elements = schema.getElements();
				xelements.putAll(elements);
			}

		}

	}

	public List<xnode> processDefinitions(Definition defs)
	{
		ArrayList<xnode> result = new ArrayList<xnode>();

		processSchemas(defs);

		Set<QName> messageSet = defs.getMessages().<QName>keySet();
		for (QName msgQName : messageSet)
		{
			String localPart = msgQName.getLocalPart();
			xelement msgNode = new xelement("message", localPart);
			result.add(msgNode);

			Message msg = defs.getMessage(msgQName);
			for ( Part part: (Collection<Part>) msg.getParts().<Part>values())
			{
				/*
				 * Part already is a schema element
				 */
				QName partElement = part.getElementName();
				if( partElement!= null)
				{
					addAndPopulateElement(msgNode, xelements.get(partElement));

				}
				QName partType = part.getTypeName();
				if( partType != null )
				{
				}
//				System.out.println("    Part Type: " + ((partType != null) ? partType : "not available!" ));
			}
		}

		return result;
	}

	public TypeInfo getPrimitiveTypeName(XmlSchemaSimpleType simpleType)
	{
		TypeInfo typeInfo=null;
		String name = simpleType.getName();
		if( name !=null )
			typeInfo = new TypeInfo(name);
//		XmlSchemaDerivationMethod deriveBy = simpleType.getDeriveBy();
		XmlSchemaSimpleTypeContent content = simpleType.getContent();
		if( content instanceof XmlSchemaSimpleTypeRestriction)
		{
			XmlSchemaSimpleTypeRestriction typeRestriction = (XmlSchemaSimpleTypeRestriction)content;
			XmlSchemaSimpleType baseType = typeRestriction.getBaseType();
			if( typeInfo == null )
				typeInfo = new TypeInfo(typeRestriction.getBaseTypeName().getLocalPart());
//			if( baseType != null)
//				return getPrimitiveTypeName(baseType);
//			else
			{
				for( XmlSchemaFacet facet : typeRestriction.getFacets() )
				{
					CLAZZ clazz = CLAZZ.valueOf(facet.getClass().getSimpleName());
//					if( facet instanceof XmlSchemaMaxLengthFacet)
//					{}
					String value = String.valueOf(facet.getValue());
					switch(clazz)
					{
					case XmlSchemaMaxLengthFacet:
					case XmlSchemaFractionDigitsFacet:
						typeInfo.setMax(value);
						break;
					case XmlSchemaMinLengthFacet:
					case XmlSchemaTotalDigitsFacet:
						typeInfo.setMin(value);
						break;
					default:
						break;
						
					}
				}
			}
		}
		else
			throw new RuntimeException("Unsupported XmlSchemaSimpleTypeContent: "+content.getClass().getName()+"!");
//		String derivationMethod = xmlType.getDerivationMethod();
//		XMLType baseType = xmlType.getBaseType();
//		if( derivationMethod !=null && baseType != null )
//			return getPrimitiveTypeName(baseType);
//		else
//			return xmlType.getName();
		return typeInfo;
	}
	
	void addAndPopulateElement( xnode parent, XmlSchemaElement schemaElement)
	{
		String name = schemaElement.getName();
		XmlSchemaElement refElement = schemaElement.getRef().getTarget();
		if( name != null )
		{
			xelement currentEl = new xelement(name);
			currentEl.setPath(parent.getPath());
			currentEl.setCardinality(schemaElement.getMinOccurs(), schemaElement.getMaxOccurs());
			parent.addItem(currentEl);
			populateElement(currentEl, schemaElement);
		}
		else if( refElement != null )
		{
			QName targetQName = schemaElement.getRef().getTargetQName();
			xelement refNode = new xelement(targetQName.getLocalPart());
			refNode.setPath(parent.getPath());
			parent.addItem(refNode);
			XmlSchemaElement xmlSchemaElement = xelements.get(targetQName);
			populateElement(refNode, xmlSchemaElement);
		}
		else
			throw new RuntimeException("Unknown XmlSchemaElement: "+schemaElement);
	}
	
	void populateElement( xelement element, XmlSchemaElement schemaElement)
	{
		XmlSchemaType schemaType = schemaElement.getSchemaType();
		if( schemaType!=null)
		{
			if (schemaType instanceof XmlSchemaSimpleType)
			{
				TypeInfo typeInfo = getPrimitiveTypeName((XmlSchemaSimpleType)schemaType);
				element.setTypeInfo(typeInfo);
			}
			else if(schemaType instanceof XmlSchemaComplexType )
			{
				processComplexType(element, (XmlSchemaComplexType)schemaType);
			}
			else
				throw new RuntimeException("Unknown schemaType: "+schemaType.getClass().getName());
		}

	}

//	xelement processElementZzzz(xelement parent, XmlSchemaElement schemaElement)
//	{
//		String name = schemaElement.getName();
//		XmlSchemaElement refElement = schemaElement.getRef().getTarget();
//		if( name != null )
//		{
//			xelement currentEl = parent;
//			if(!name.equals(parent.getName()))
//			{
//				/*
//				 * if element.name is not same as parent.name, processGroup() calls 
//				 * this method to create a new xpath node
//				 */
//				currentEl = new xelement(name);
//				currentEl.setPath(parent.getPath());
//				currentEl.setCardinality(schemaElement.getMinOccurs(), schemaElement.getMaxOccurs());
//			}
//			XmlSchemaType schemaType = schemaElement.getSchemaType();
//			if( schemaType!=null)
//			{
//				if (schemaType instanceof XmlSchemaSimpleType)
//				{
//					TypeInfo typeInfo = getPrimitiveTypeName((XmlSchemaSimpleType)schemaType);
//					currentEl.setTypeInfo(typeInfo);
//					return currentEl; 
//					
//				}
//				else if(schemaType instanceof XmlSchemaComplexType )
//				{
//					processComplexType(parent, (XmlSchemaComplexType)schemaType);
//				}
//				else
//					throw new RuntimeException("Unknown schemaType: "+schemaType.getClass().getName());
//			}
//		}
//		else if( refElement != null )
//		{
//			QName targetQName = schemaElement.getRef().getTargetQName();
//			xelement refNode = new xelement(targetQName.getLocalPart());
//			refNode.setPath(parent.getPath());
//			parent.addItem(refNode);
//			XmlSchemaElement xmlSchemaElement = xelements.get(targetQName);
//			return processElement(refNode, xmlSchemaElement);
////			XmlSchemaElement xmlSchemaElement = xelements.get(targetQName);
////			return processElement(parent, xmlSchemaElement);
//		}
//		return null;
//	}

	void processComplexType( xelement element, XmlSchemaComplexType complexType)
	{
		QName baseTypeName = complexType.getBaseSchemaTypeName();
		if( baseTypeName!=null)
		{
			XmlSchemaElement se = xelements.get(baseTypeName);
			XmlSchemaType schemaType = se.getSchemaType();
			if( schemaType!=null &&  schemaType instanceof XmlSchemaComplexType )
				processComplexType(element, (XmlSchemaComplexType)schemaType);
			else
				throw new RuntimeException(baseTypeName+" is not handled yet!");
		}

		/*
		 * TODO: attributes
		 */


		XmlSchemaParticle particle = complexType.getParticle();
		if( particle == null )
			return;
		else if(particle instanceof XmlSchemaGroupParticle)
		{
			XmlSchemaGroupParticle schemaGroup = (XmlSchemaGroupParticle)particle;
			addAndPopulateGroup(element, schemaGroup);
		}
		else
		{
			throw new RuntimeException("Unsupported particle: "+particle.getClass().getName()+"!");

		}
	}

//	void processGroupParticle(XmlSchemaObject item, xgroup parentGroup, xnode parent)
//	{
//		if( item instanceof XmlSchemaGroupParticle)
//		{
//			xgroup childGroup = processGroup(parent, (XmlSchemaGroupParticle)item);
//			parentGroup.addGroup(childGroup);
//		}
//		else if (item instanceof XmlSchemaElement)
//		{
//			xnode element= processElement(parent, (XmlSchemaElement)item);
//			parentGroup.addItem(element);
//		}
//		else
//			throw new RuntimeException("Unsupported particle: "+item.getClass().getName()+"!");
//
//	}

	void addAndPopulateGroup(xnode parent, XmlSchemaGroupParticle schemaGroup)
	{
		xgroup resultGroup = new xgroup("sequence");
		parent.addItem(resultGroup);
		if (schemaGroup instanceof XmlSchemaChoice)
		{
			resultGroup.setOrder("choice");
		}
		else if (schemaGroup instanceof XmlSchemaAll)
		{
			resultGroup.setOrder("all");
		}
		
		populateGroup(resultGroup, schemaGroup);
	}
	
	void populateGroup(xgroup resultGroup, XmlSchemaGroupParticle schemaGroup)
	{
		if(schemaGroup instanceof XmlSchemaSequence)
		{
			List<XmlSchemaSequenceMember> items = ((XmlSchemaSequence)schemaGroup).getItems();
			for(XmlSchemaSequenceMember item : items)
			{
				if( item instanceof XmlSchemaGroupParticle)
				{
					XmlSchemaGroupParticle itSchemaGroup = (XmlSchemaGroupParticle)item;
					addAndPopulateGroup(resultGroup, itSchemaGroup);
				}
				else if (item instanceof XmlSchemaElement)
				{
					addAndPopulateElement(resultGroup, (XmlSchemaElement)item);
				}
				else
					throw new RuntimeException("Unsupported particle: "+item.getClass().getName()+"!");
			}
		}
		else if (schemaGroup instanceof XmlSchemaChoice)
		{
			List<XmlSchemaObject> items = ((XmlSchemaChoice)schemaGroup).getItems();
			for( XmlSchemaObject item : items )
			{
				if( item instanceof XmlSchemaGroupParticle)
				{
					XmlSchemaGroupParticle itSchemaGroup = (XmlSchemaGroupParticle)item;
					addAndPopulateGroup(resultGroup, itSchemaGroup);
				}
				else if (item instanceof XmlSchemaElement)
				{
					addAndPopulateElement(resultGroup, (XmlSchemaElement)item);
				}
				else
					throw new RuntimeException("Unsupported particle: "+item.getClass().getName()+"!");
			}

		}
		else if (schemaGroup instanceof XmlSchemaAll)
		{
			List<XmlSchemaElement> items = ((XmlSchemaAll)schemaGroup).getItems();
			for(XmlSchemaElement item : items)
			{
				addAndPopulateElement(resultGroup, item);
			}
		}
	}
	
//	void processGroupZzz( xelement parentNode, XmlSchemaGroupParticle schemaGroup)
//	{
//		xgroup resultGroup = new xgroup("sequence");
//		parentNode.addItem(resultGroup);
//		if(schemaGroup instanceof XmlSchemaSequence)
//		{
//			List<XmlSchemaSequenceMember> items = ((XmlSchemaSequence)schemaGroup).getItems();
//			for(XmlSchemaSequenceMember it : items)
//			{
//				XmlSchemaObject item = (XmlSchemaObject)it;
//				if( item instanceof XmlSchemaGroupParticle)
//				{
//					/*xgroup childGroup = */processGroup(parentNode, (XmlSchemaGroupParticle)item);
////					resultGroup.addGroup(childGroup);
//				}
//				else if (item instanceof XmlSchemaElement)
//				{
//					xnode element= processElement(parentNode, (XmlSchemaElement)item);
//					if( element != null )
//						resultGroup.addItem(element);
//				}
//				else
//					throw new RuntimeException("Unsupported particle: "+item.getClass().getName()+"!");
//			}
//		}
//		else if (schemaGroup instanceof XmlSchemaChoice)
//		{
//			resultGroup.setOrder("choice");
//			/*
//			 * in the scope of resultGroup
//			 */
//			List<XmlSchemaObject> items = ((XmlSchemaChoice)schemaGroup).getItems();
//			for( XmlSchemaObject item : items )
//			{
//				if( item instanceof XmlSchemaGroupParticle)
//				{
//					/*xgroup childGroup = */processGroup(parentNode, (XmlSchemaGroupParticle)item);
////					resultGroup.addGroup(childGroup);
//				}
//				else if (item instanceof XmlSchemaElement)
//				{
//					xnode element= processElement(parentNode, (XmlSchemaElement)item);
//					if( element != null )
//						resultGroup.addItem(element);
//				}
//				else
//					throw new RuntimeException("Unsupported particle: "+item.getClass().getName()+"!");
//			}
//
//		}
//		else if (schemaGroup instanceof XmlSchemaAll)
//		{
//			resultGroup.setOrder("all");
//			List<XmlSchemaElement> items = ((XmlSchemaAll)schemaGroup).getItems();
//			for(XmlSchemaElement item : items)
//			{
//				xnode element = processElement(parentNode, item);
//				if( element != null )
//					resultGroup.addItem(element);
//
//			}
//
//		}
//	}

}
