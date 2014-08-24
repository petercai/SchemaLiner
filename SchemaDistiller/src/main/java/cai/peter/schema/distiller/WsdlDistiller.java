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
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaGroupParticle;
import org.apache.ws.commons.schema.XmlSchemaObject;
import org.apache.ws.commons.schema.XmlSchemaParticle;
import org.apache.ws.commons.schema.XmlSchemaSequence;
import org.apache.ws.commons.schema.XmlSchemaSequenceMember;
import org.apache.ws.commons.schema.XmlSchemaSimpleType;
import org.apache.ws.commons.schema.XmlSchemaSimpleTypeContent;
import org.apache.ws.commons.schema.XmlSchemaSimpleTypeRestriction;
import org.apache.ws.commons.schema.XmlSchemaType;

import cai.peter.schema.model.xelement;
import cai.peter.schema.model.xgroup;
import cai.peter.schema.model.xnode;


public class WsdlDistiller
{
	Map<QName, XmlSchemaElement> xelements = new HashMap<QName, XmlSchemaElement>();

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
					processElement(msgNode, xelements.get(partElement));

				}
				QName partType = part.getTypeName();
				if( partType != null )
				{
					processType();
				}
//				System.out.println("    Part Type: " + ((partType != null) ? partType : "not available!" ));
			}
		}

		return result;
	}

	public String getPrimitiveTypeName(XmlSchemaSimpleType simpleType)
	{
		XmlSchemaSimpleTypeContent content = simpleType.getContent();
		if( content == null ) return simpleType.getName();
		if( content instanceof XmlSchemaSimpleTypeRestriction)
		{
			XmlSchemaSimpleTypeRestriction typeInfo = (XmlSchemaSimpleTypeRestriction)content;
			XmlSchemaSimpleType baseType = typeInfo.getBaseType();
			if( baseType != null)
				return getPrimitiveTypeName(baseType);
			else
			{
				QName baseTypeName = typeInfo.getBaseTypeName();
				return baseTypeName.getLocalPart();
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
	}

	xelement processElement(xelement parent, XmlSchemaElement element)
	{
		xelement child = null;
		String name = element.getName();
		XmlSchemaElement refElement = element.getRef().getTarget();
		if( refElement!=null)
 ;
		if( name != null )
		{
			XmlSchemaType schemaType = element.getSchemaType();
			if( schemaType!=null)
			{
				QName schemaTypeName = schemaType.getQName();
				if (schemaType instanceof XmlSchemaSimpleType /*&& (parent instanceof xelement)*/)
				{
					/*
					 * SimpleType element defines the type info for "parent"
					 */
					String type=getPrimitiveTypeName((XmlSchemaSimpleType)schemaType);
					if( !(parent instanceof xelement) )
						throw new RuntimeException(parent.toString());
					((xelement)parent).setType(type);
					return parent; // done!
					
				}
				else if(schemaType instanceof XmlSchemaComplexType )
				{
					/*
					 * ComplexType element defines the type info for "parent
					 * it doesn't contribute to xpath
					 */
					processComplexType(parent, (XmlSchemaComplexType)schemaType);
				}
				else
					throw new RuntimeException("Unknown schemaType: "+schemaType.getClass().getName());
			}
			else
			{
				/*
				 * an element 1.without type info;2.not a ref; is an xpath node
				 */
				child = new xelement(name);
				child.setPath(parent.getPath());
				child.setCardinality(element.getMinOccurs(), element.getMaxOccurs());
				parent.addItem(child);
				parent = child;
			}
		}
		else if( refElement != null )
		{
			QName targetQName = element.getRef().getTargetQName();
			/*
			 * a ref element contribtues to xpath
			 */
			xelement refNode = new xelement(targetQName.getLocalPart());
			refNode.setPath(parent.getPath());
			parent.addItem(refNode);
			XmlSchemaElement xmlSchemaElement = xelements.get(targetQName);
			return processElement(refNode, xmlSchemaElement);
		}
//		else
//			throw new RuntimeException("No way to be here");

//		if( schemaType != null )
//		{
//			if( schemaType instanceof XmlSchemaComplexType )
//				processComplexType(parent, (XmlSchemaComplexType)schemaType);
//		}

		return parent;
	}

	void processComplexType( xelement parent, XmlSchemaComplexType complexType)
	{
		QName baseTypeName = complexType.getBaseSchemaTypeName();
		if( baseTypeName!=null)
		{
			XmlSchemaElement se = xelements.get(baseTypeName);
			XmlSchemaType schemaType = se.getSchemaType();
			if( schemaType!=null &&  schemaType instanceof XmlSchemaComplexType )
				processComplexType(parent, (XmlSchemaComplexType)schemaType);
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
			/*xgroup group = */processGroup(parent, (XmlSchemaGroupParticle)particle);
//			parent.addGroup(group);
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

	void processGroup( xelement parentNode, XmlSchemaGroupParticle schemaGroup)
	{
		xgroup resultGroup = new xgroup("sequence");
		parentNode.addItem(resultGroup);
		if(schemaGroup instanceof XmlSchemaSequence)
		{
			List<XmlSchemaSequenceMember> items = ((XmlSchemaSequence)schemaGroup).getItems();
			for(XmlSchemaSequenceMember it : items)
			{
				XmlSchemaObject item = (XmlSchemaObject)it;
				if( item instanceof XmlSchemaGroupParticle)
				{
					/*xgroup childGroup = */processGroup(parentNode, (XmlSchemaGroupParticle)item);
//					resultGroup.addGroup(childGroup);
				}
				else if (item instanceof XmlSchemaElement)
				{
					xnode element= processElement(parentNode, (XmlSchemaElement)item);
					resultGroup.addItem(element);
				}
				else
					throw new RuntimeException("Unsupported particle: "+item.getClass().getName()+"!");
			}
		}
		else if (schemaGroup instanceof XmlSchemaChoice)
		{
			resultGroup.setOrder("choice");
			/*
			 * in the scope of resultGroup
			 */
			List<XmlSchemaObject> items = ((XmlSchemaChoice)schemaGroup).getItems();
			for( XmlSchemaObject item : items )
			{
				if( item instanceof XmlSchemaGroupParticle)
				{
					/*xgroup childGroup = */processGroup(parentNode, (XmlSchemaGroupParticle)item);
//					resultGroup.addGroup(childGroup);
				}
				else if (item instanceof XmlSchemaElement)
				{
					xnode element= processElement(parentNode, (XmlSchemaElement)item);
					resultGroup.addItem(element);
				}
				else
					throw new RuntimeException("Unsupported particle: "+item.getClass().getName()+"!");
			}

		}
		else if (schemaGroup instanceof XmlSchemaAll)
		{
			resultGroup.setOrder("all");
			List<XmlSchemaElement> items = ((XmlSchemaAll)schemaGroup).getItems();
			for(XmlSchemaElement item : items)
			{
				xnode element = processElement(parentNode, item);
				resultGroup.addItem(element);

			}

		}
//		return resultGroup;
	}

	/*
	 * TODO
	 */
	void processType()
	{}
}
