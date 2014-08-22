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
import org.apache.log4j.Logger;
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
import org.apache.ws.commons.schema.utils.XmlSchemaRef;

import cai.peter.schema.model.xelement;
import cai.peter.schema.model.xgroup;
import cai.peter.schema.model.xnode;


public class WsdlDistiller
{
	/**
	 * Logger for this class
	 */
	private static final Logger	logger	= Logger.getLogger(WsdlDistiller.class);

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
			xnode msgNode = new xnode("message", localPart);
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
					crawlElement(msgNode, xelements.get(partElement));

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
		if( content instanceof XmlSchemaSimpleTypeRestriction)
		{
			QName baseTypeName = ((XmlSchemaSimpleTypeRestriction)content).getBaseTypeName();
			return baseTypeName.getLocalPart();
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


	xnode crawlElement(xnode parent, XmlSchemaElement element)
	{
		xelement child = null;
		XmlSchemaRef<XmlSchemaElement> schemaRef = element.getRef();
		if( schemaRef.getTarget() != null )
		{
			QName targetQName = schemaRef.getTargetQName();
			xnode refNode = new xnode(targetQName.getLocalPart());
			refNode.setPath(parent.getPath());
			parent.addChild(refNode);
			XmlSchemaElement xmlSchemaElement = xelements.get(targetQName);
			return crawlElement(refNode, xmlSchemaElement);
		}

		String type=null;
		XmlSchemaType schemaType = element.getSchemaType();
		if( schemaType != null )
		{
			if( schemaType instanceof XmlSchemaComplexType )
				processComplexType(parent, (XmlSchemaComplexType)schemaType);
			else
				type = getPrimitiveTypeName((XmlSchemaSimpleType)schemaType);
		}

		String name = element.getName();
		if( name != null )
		{
			child = new xelement(name);
			child.setPath(parent.getPath());
			child.setCardinality(element.getMinOccurs(), element.getMaxOccurs());
			child.setType(type);
			parent.addChild(child);

			return child;
		}

		throw new RuntimeException("crawlElement() must return a non-null node!");
	}

	void processComplexType( xnode node, XmlSchemaComplexType complexType)
	{
		QName baseTypeName = complexType.getBaseSchemaTypeName();
		if( baseTypeName!=null)
		{
			XmlSchemaElement se = xelements.get(baseTypeName);
			XmlSchemaType schemaType = se.getSchemaType();
			if( schemaType!=null &&  schemaType instanceof XmlSchemaComplexType )
				processComplexType(node, (XmlSchemaComplexType)schemaType);
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
			xgroup group = processGroup(node, (XmlSchemaGroupParticle)particle);
			node.addGroup(group);
		}
		else
		{
			throw new RuntimeException("Unsupported particle: "+particle.getClass().getName()+"!");

		}
	}

	void processGroupParticle(XmlSchemaObject item, xgroup parentGroup, xnode parent)
	{
		if( item instanceof XmlSchemaGroupParticle)
		{
			xgroup childGroup = processGroup(parent, (XmlSchemaGroupParticle)item);
			parentGroup.addGroup(childGroup);
		}
		else if (item instanceof XmlSchemaElement)
		{
			xnode element = crawlElement(parent, (XmlSchemaElement)item);
			parentGroup.addNode(element);
		}
		else
			throw new RuntimeException("Unsupported particle: "+item.getClass().getName()+"!");

	}

	xgroup processGroup( xnode parent, XmlSchemaGroupParticle group)
	{
		xgroup result = new xgroup("sequence");
		parent.addGroup(result);
		if(group instanceof XmlSchemaSequence)
		{
			List<XmlSchemaSequenceMember> items = ((XmlSchemaSequence)group).getItems();
			for(XmlSchemaSequenceMember it : items)
			{
				XmlSchemaObject item = (XmlSchemaObject)it;
				processGroupParticle(item, result, parent);
//					if( item instanceof XmlSchemaGroupParticle)
//						result.addGroup(processGroup(parent, (XmlSchemaGroupParticle)item));
//					else if (item instanceof XmlSchemaElement)
//					{
//						xnode element = crawlElement(parent, (XmlSchemaElement)item);
//						result.addNode(element);
//					}
//					else
//						throw new RuntimeException("Unsupported particle: "+item.getClass().getName()+"!");
			}
		}
		else if (group instanceof XmlSchemaChoice)
		{
			result.setOrder("choice");
			List<XmlSchemaObject> items = ((XmlSchemaChoice)group).getItems();
			for( XmlSchemaObject item : items )
			{
				processGroupParticle(item, result, parent);
//				if( item instanceof XmlSchemaGroupParticle)
//					result.addGroup(processGroup(parent, (XmlSchemaGroupParticle)item));
//				else if (item instanceof XmlSchemaElement)
//				{
//					xnode element = crawlElement(parent, (XmlSchemaElement)item);
//					result.addNode(element);
//				}
//				else
//					throw new RuntimeException("Unsupported particle: "+item.getClass().getName()+"!");
//
			}

		}
		else if (group instanceof XmlSchemaAll)
		{
			result.setOrder("all");
			List<XmlSchemaElement> items = ((XmlSchemaAll)group).getItems();
			for(XmlSchemaElement item : items)
			{
				xnode element = crawlElement(parent, item);
				result.addNode(element);

			}

		}
		return result;
	}

	/*
	 * TODO
	 */
	void processType()
	{}
}
