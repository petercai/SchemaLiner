/***********************************************
 * Copyright (c) 2014 Peter Cai
 * All rights reserved.
 *
 * Aug 22, 2014
 *
 ***********************************************/
package cai.peter.schema.distiller;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.wsdl.Definition;
import javax.wsdl.Fault;
import javax.wsdl.Input;
import javax.wsdl.Message;
import javax.wsdl.Operation;
import javax.wsdl.Output;
import javax.wsdl.Part;
import javax.wsdl.PortType;
import javax.xml.namespace.QName;

import org.apache.axis2.AxisFault;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.WSDL11ToAllAxisServicesBuilder;
import org.apache.axis2.util.SchemaUtil;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaAll;
import org.apache.ws.commons.schema.XmlSchemaAny;
import org.apache.ws.commons.schema.XmlSchemaChoice;
import org.apache.ws.commons.schema.XmlSchemaComplexType;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaFacet;
import org.apache.ws.commons.schema.XmlSchemaGroupBase;
import org.apache.ws.commons.schema.XmlSchemaObject;
import org.apache.ws.commons.schema.XmlSchemaObjectCollection;
import org.apache.ws.commons.schema.XmlSchemaParticle;
import org.apache.ws.commons.schema.XmlSchemaSimpleType;
import org.apache.ws.commons.schema.XmlSchemaSimpleTypeContent;
import org.apache.ws.commons.schema.XmlSchemaSimpleTypeRestriction;
import org.apache.ws.commons.schema.XmlSchemaType;

import cai.peter.schema.XmlSchemaTypeEnum;
import cai.peter.schema.model.TypeInfo;
import cai.peter.schema.model.xelement;
import cai.peter.schema.model.xgroup;
import cai.peter.schema.model.xnode;


public class WsdlDistiller
{
	Map<QName, XmlSchemaElement> schemaElementLookup = new HashMap<QName, XmlSchemaElement>();
	Map<QName, XmlSchemaType> schemaTypeLookup = new HashMap<QName, XmlSchemaType>();
	
	public Map<String, Collection<xelement>> getSchemaInfo()
	{
		Map<String, Map<String, xelement>> nsElMap = new HashMap<String, Map<String, xelement>>();
		Map<String, xelement> map;
		for(QName qname : schemaElementLookup.keySet())
		{
			String ns = qname.getNamespaceURI();
			if(!nsElMap.containsKey(ns))
			{
				map = new TreeMap<String, xelement>(); 
				nsElMap.put(ns, map);
			}
			else
				map = nsElMap.get(ns);
			String name = qname.getLocalPart();
			XmlSchemaElement val = schemaElementLookup.get(qname);
			xelement e = new xelement("Element", name);
//			addAndPopulateElement(e,val);
			map.put(name, e);
		}
		Map<String, Map<String, xelement>> nsCTMap = new HashMap<String, Map<String, xelement>>();
		Map<String, Map<String, xelement>> nsSTMap = new HashMap<String, Map<String, xelement>>();
		for( QName qname : schemaTypeLookup.keySet())
		{
			String ns = qname.getNamespaceURI();
			String name = qname.getLocalPart();
			XmlSchemaType val = schemaTypeLookup.get(qname);
			xelement e;
			switch( XmlSchemaTypeEnum.valueOf(val.getClass().getSimpleName()))
			{
			case XmlSchemaComplexType:
				if( !nsCTMap.containsKey(ns))
				{
					map = new TreeMap<String, xelement>(); 
					nsCTMap.put(ns, map);
				}
				else
					map = nsCTMap.get(ns);
				e = new xelement("ComplexType", name);
//				processSchemaType(e, val);
				map.put(name, e);
				break;
			case XmlSchemaSimpleType:
				if( !nsSTMap.containsKey(ns))
				{
					map = new TreeMap<String, xelement>(); 
					nsSTMap.put(ns, map);
				}
				else
					map = nsSTMap.get(ns);
				e = new xelement("SimpleType", name);
				processSchemaType(e, val);
				map.put(name, e);
				break;
			}
			
		}
		//TODO
		Map<String, Collection<xelement>> result = new HashMap<String, Collection<xelement>>();
		for(String ns : nsElMap.keySet())
		{
			Collection<xelement> values = nsElMap.get(ns).values();
			result.put(ns, values);
		}
		for(String ns : nsCTMap.keySet())
		{
			Collection<xelement> values = nsCTMap.get(ns).values();
			result.put(ns, values);
		}
		for(String ns : nsSTMap.keySet())
		{
			Collection<xelement> values = nsSTMap.get(ns).values();
			result.put(ns, values);
		}
		return result;
	}
	
	void populateSchemType( xelement parent, XmlSchemaType schemaType)
	{
		//TODO
	}
	
	public void processSchemas(Definition defs) throws AxisFault
	{
		WSDL11ToAllAxisServicesBuilder builder = new WSDL11ToAllAxisServicesBuilder(defs);
        List<AxisService> allServices = builder.populateAllServices();
        for( AxisService as : allServices)
        {
        	ArrayList<XmlSchema> schemas = as.getSchema();
        	for( XmlSchema schema : schemas )
        	{
				int count = schema.getIncludes().getCount();
				List<XmlSchema> schemaList = Arrays.<XmlSchema>asList(SchemaUtil.getAllSchemas(schema));
				for( XmlSchema s : schemaList )
				{
					Iterator<XmlSchemaObject> it = s.getItems().getIterator();
					while(it.hasNext())
					{
						XmlSchemaObject object = it.next();
						switch( XmlSchemaTypeEnum.valueOf(object.getClass().getSimpleName()))
						{
						case XmlSchemaElement:
							XmlSchemaElement e = (XmlSchemaElement)object;
							QName name = e.getQName();
							schemaElementLookup.put(name, e);
							break;
						case XmlSchemaSimpleType:
						case XmlSchemaComplexType:
							XmlSchemaType t = (XmlSchemaType)object;
							QName tn = t.getQName();
							schemaTypeLookup.put(tn, t);
							break;
						case XmlSchemaImport:
						case XmlSchemaInclude:
							break;
						default:
							break;
						}
					}
				}

        	}
        }

		String targetNSUri = defs.getTargetNamespace();
		Map<QName, Message> messages = (Map<QName, Message>)defs.getMessages();
		for (Map.Entry<QName, Message> msgEntry: messages.entrySet())
		{
			QName messageName = msgEntry.getKey();
			Message msg = msgEntry.getValue();
			for ( Part part: (Collection<Part>) msg.getParts().values())
			{
				// TODO: multi-part message
				QName partQName = part.getElementName();
				schemaElementLookup.put(messageName, schemaElementLookup.get(partQName));
			}
		}

	}

	
	XmlSchemaElement lookupElement(QName qname)
	{
		return schemaElementLookup.get(qname);
	}
	
	XmlSchemaType lookupType(QName qname)
	{
		return schemaTypeLookup.get(qname);
	}

	TypeInfo getPrimitiveTypeName(XmlSchemaSimpleType simpleType)
	{
		TypeInfo typeInfo=null;
		String ns=null;
		QName qName = simpleType.getQName();
		if( qName !=null )
		{
			ns = qName.getNamespaceURI();
			String name = qName.getLocalPart();
			typeInfo = new TypeInfo(name);
		}
		XmlSchemaSimpleTypeContent content = simpleType.getContent();
		if( content instanceof XmlSchemaSimpleTypeRestriction)
		{
			XmlSchemaSimpleTypeRestriction typeRestriction = (XmlSchemaSimpleTypeRestriction)content;
			if( !"http://www.w3.org/2001/XMLSchema".equals(ns) )
			{
				XmlSchemaSimpleType baseType = typeRestriction.getBaseType();
				if( baseType!=null )
					return getPrimitiveTypeName(baseType);
				else
				{
					String baseTypeName = typeRestriction.getBaseTypeName().getLocalPart();
					if (baseTypeName != null)
					{
						if( typeInfo==null)
							typeInfo = new TypeInfo(baseTypeName);
						else
							typeInfo.setName(baseTypeName);
					}
				}
			}
			XmlSchemaObjectCollection facets = typeRestriction.getFacets();
			Iterator<XmlSchemaFacet> iterator = facets.getIterator();
			while(iterator.hasNext())
			{
				XmlSchemaFacet facet = iterator.next();
				XmlSchemaTypeEnum xtype = XmlSchemaTypeEnum.valueOf(facet.getClass().getSimpleName());
				String value = String.valueOf(facet.getValue());
				switch(xtype)
				{
				case XmlSchemaMaxLengthFacet:
					typeInfo.setMax(value);
					break;
				case XmlSchemaFractionDigitsFacet:
					typeInfo.setFraction(value);
					break;
				case XmlSchemaMinLengthFacet:
					typeInfo.setMin(value);
					break;
				case XmlSchemaTotalDigitsFacet:
					typeInfo.setTotal(value);
					break;
				case XmlSchemaEnumerationFacet:
					typeInfo.addEnumeration(value);
					break;
				default:
					break;
					
				}
			}
		}
		else if( typeInfo == null )
			throw new RuntimeException("Unsupported XmlSchemaSimpleTypeContent: "+content.getClass().getName()+"!");
		return typeInfo;
	}
	
	public List<xelement> processDefinitions(Definition defs) throws AxisFault
	{
		ArrayList<xelement> result = new ArrayList<xelement>();
	
		processSchemas(defs);
		
		/*
		 * PortTypes
		 */
		Collection<PortType> pts = defs.getPortTypes().<PortType>values();
		for (PortType pt : pts) {
			String portTypeName = pt.getQName().getLocalPart();
			xelement ptNode = new xelement("PortType", portTypeName);
			result.add(ptNode);
			
			List<Operation> operations = pt.<Operation>getOperations();
			for (Operation op : operations) {
				String operationName = op.getName();
				xelement operationNode = new xelement("Operation", operationName);
				/*
				 * add operation to root
				 */
				result.add(operationNode);
				
				Input input = op.getInput();
				if( input!=null )
				{
					QName inputQName = input.getMessage().getQName();
					String inputName = inputQName.getLocalPart();
					xelement inputNode = new xelement("Input",inputName);
					if( inputNode != null )
					{
						operationNode.addItem(inputNode);
						XmlSchemaElement inputSchemaElement = lookupElement(inputQName);
						if( inputSchemaElement != null )
							addAndPopulateElement(inputNode, inputSchemaElement);
					}
				}
				
				Output output = op.getOutput();
				if( output !=null )
				{
					QName outputQName = output.getMessage().getQName();
					String ouputName = outputQName.getLocalPart();
					xelement outputNode = new xelement("Output", ouputName);
					if( outputNode != null )
					{
						operationNode.addItem(outputNode);
						XmlSchemaElement ouputSchemaElement = lookupElement(outputQName);
						if( ouputSchemaElement != null )
							addAndPopulateElement(outputNode, ouputSchemaElement);
					}
				}
				Map faults = op.getFaults();
				for (Fault fault : (Collection<Fault>) faults.<Fault>values()) {
					QName faultQName = fault.getMessage().getQName();
					String faultName = fault.getName();
					xelement faultNode = new xelement("Fault", faultName);
					operationNode.addItem(faultNode);
					XmlSchemaElement faultSchemaElement = lookupElement(faultQName);
					if( faultSchemaElement!=null )
						addAndPopulateElement(faultNode, faultSchemaElement);
				}
	
			}
		}
		
		/*
		 * messages
		 */
		Set<QName> messageSet = defs.getMessages().keySet();
		for (QName msgQName : messageSet)
		{
			String messageName = msgQName.getLocalPart();
			xelement msgNode = new xelement("Message", messageName);
			result.add(msgNode);

			Message msg = defs.getMessage(msgQName);
			for ( Part part: (Collection<Part>) msg.getParts().values())
			{
				QName partElement = part.getElementName();
				if( partElement!= null)
				{
					addAndPopulateElement(msgNode, lookupElement(partElement));
				}
			}
		}
		return result;
	}
	void addAndPopulateAny( xnode parent, XmlSchemaAny schemaAny)
	{
		xelement currentEl = new xelement("unsupportedAny");
		currentEl.setPath(parent.getPath());
		currentEl.setCardinality(schemaAny.getMinOccurs(), schemaAny.getMaxOccurs());
		parent.addItem(currentEl);
		
	}
	void addAndPopulateElement( xnode parent, XmlSchemaElement schemaElement)
	{
		String name = schemaElement.getName();
		QName refName = schemaElement.getRefName();
		if( refName != null )
		{
			xelement refNode = new xelement(refName.getLocalPart());
			refNode.setPath(parent.getPath());
			parent.addItem(refNode);
			XmlSchemaElement xmlSchemaElement = lookupElement(refName);
			populateElement(refNode, xmlSchemaElement);
		}
		else 
		if( name != null )
		{
			xelement currentEl = new xelement(name);
			currentEl.setPath(parent.getPath());
			currentEl.setCardinality(schemaElement.getMinOccurs(), schemaElement.getMaxOccurs());
			parent.addItem(currentEl);
			populateElement(currentEl, schemaElement);
		}
		else
			throw new RuntimeException("Unknown XmlSchemaElement: "+schemaElement);
	}
	
	void populateElement( xelement element, XmlSchemaElement schemaElement)
	{
		XmlSchemaType schemaType = schemaElement.getSchemaType();
		QName typeQName = schemaElement.getSchemaTypeName();
		if( schemaType!=null)
		{
			processSchemaType(element, schemaType);
		}
		else if( typeQName != null )
		{
			schemaType = lookupType(typeQName);
			if( schemaType != null )
				processSchemaType(element, schemaType);
			else
				throw new RuntimeException("unknow schema element: "
								+ element.toString() + "@"+schemaElement);
		}
		else
			throw new RuntimeException("unknow schema element: "
					+ element.toString() + "@"+schemaElement);

	}


	void processSchemaType(xelement element, XmlSchemaType schemaType)
			throws RuntimeException
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

	void processComplexType( xelement element, XmlSchemaComplexType complexType)
	{
		QName baseTypeName = complexType.getBaseSchemaTypeName();
		if( baseTypeName!=null)
		{
			XmlSchemaType schemaType = lookupType(baseTypeName);
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
		else if(particle instanceof XmlSchemaGroupBase)
		{
			XmlSchemaGroupBase schemaGroup = (XmlSchemaGroupBase)particle;
			addAndPopulateGroup(element, schemaGroup);
		}
		else
		{
			throw new RuntimeException("Unsupported particle: "+particle.getClass().getName()+"!");

		}
	}

	void addAndPopulateGroup(xnode parent, XmlSchemaGroupBase schemaGroup)
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
	
	void populateGroup(xgroup resultGroup, XmlSchemaGroupBase schemaGroup)
	{
		{
			XmlSchemaObjectCollection items = (/*(XmlSchemaSequence)*/schemaGroup).getItems();
			Iterator iterator = items.getIterator();
			while(iterator.hasNext())
			{
				Object item = iterator.next();
				if( item instanceof XmlSchemaGroupBase)
				{
					XmlSchemaGroupBase itSchemaGroup = (XmlSchemaGroupBase)item;
					addAndPopulateGroup(resultGroup, itSchemaGroup);
				}
				else if (item instanceof XmlSchemaElement)
				{
					addAndPopulateElement(resultGroup, (XmlSchemaElement)item);
				}
				else if( item instanceof XmlSchemaAny )
				{
					addAndPopulateAny(resultGroup, (XmlSchemaAny)item);
				}
				else
					throw new RuntimeException("Unsupported particle: "+item.getClass().getName()+"!");
			}
		}
	}
}
