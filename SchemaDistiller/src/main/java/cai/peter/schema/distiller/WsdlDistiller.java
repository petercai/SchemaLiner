/***********************************************
 * Copyright (c) 2014 Peter Cai
 * All rights reserved.
 *
 * Aug 22, 2014
 *
 ***********************************************/
package cai.peter.schema.distiller;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.wsdl.Definition;
import javax.wsdl.Fault;
import javax.wsdl.Message;
import javax.wsdl.Operation;
import javax.wsdl.Part;
import javax.wsdl.PortType;
import javax.xml.namespace.QName;

import org.apache.cxf.BusFactory;
import org.apache.cxf.service.model.SchemaInfo;
import org.apache.cxf.service.model.ServiceInfo;
import org.apache.cxf.wsdl11.WSDLServiceBuilder;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaAll;
import org.apache.ws.commons.schema.XmlSchemaChoice;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.apache.ws.commons.schema.XmlSchemaComplexType;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaExternal;
import org.apache.ws.commons.schema.XmlSchemaFacet;
import org.apache.ws.commons.schema.XmlSchemaGroupParticle;
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
	Map<QName, XmlSchemaElement> qNameLookup = new HashMap<QName, XmlSchemaElement>();
	Map<String, XmlSchemaElement> nameLookup = new HashMap<String, XmlSchemaElement>();

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
	
	Map<String, XmlSchemaElement> reindexSchema(Map<QName, XmlSchemaElement> source)
	{
		Map<String, XmlSchemaElement> nameLookup = new HashMap<String, XmlSchemaElement>();
		for(Map.Entry<QName, XmlSchemaElement> entry: source.entrySet())
		{
			QName key = entry.getKey();
			XmlSchemaElement value = entry.getValue();
			nameLookup.put(key.getLocalPart(), value);
		}
		return nameLookup;
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
//				populateSchema(qNameLookup, schema);
				qNameLookup.putAll(schema.getElements());
			}
		}
		nameLookup = reindexSchema(qNameLookup);
	}
	
	void populateSchema(Map<QName, XmlSchemaElement> lookup, XmlSchema schema)
	{
		if( schema == null ) return;
		lookup.putAll(schema.getElements());
		List<XmlSchemaExternal> externals = schema.getExternals();
		if( externals == null || externals.size()==0) return;
		try
		{
			String sourceURI = schema.getSourceURI();
			String path = new URI(sourceURI).getPath();
			File sourceFile = new File(path);
			for(XmlSchemaExternal ext : externals)
			{
				String schemaLocation = ext.getSchemaLocation();
				File schemaFile = new File(sourceFile.getParent(), schemaLocation);
				try
				{
					if( schemaFile.exists())
					{
						InputStream is = new FileInputStream(schemaFile);
						XmlSchemaCollection schemaCol = new XmlSchemaCollection();
						InputStreamReader inputStreamReader = new InputStreamReader(is);
						XmlSchema child = schemaCol.read(inputStreamReader);
						lookup.putAll(child.getElements());
						populateSchema(lookup, child);
					}
				}
				catch (FileNotFoundException e)
				{
					e.printStackTrace();
				}
			}
		}
		catch (URISyntaxException e)
		{
			e.printStackTrace();
		}
	}

	public List<xnode> processDefinitions(Definition defs)
	{
		ArrayList<xnode> result = new ArrayList<xnode>();

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
				
				QName inputQName = op.getInput().getMessage().getQName();
				String inputName = inputQName.getLocalPart();
				xelement inputNode = new xelement("Input",inputName);
				operationNode.addItem(inputNode);
				XmlSchemaElement inputSchemaElement = qNameLookup.get(inputQName);
				if( inputSchemaElement == null ) 
					inputSchemaElement = nameLookup.get(inputName);
				addAndPopulateElement(inputNode, inputSchemaElement);
				
				QName outputQName = op.getOutput().getMessage().getQName();
				String ouputName = outputQName.getLocalPart();
				xelement outputNode = new xelement("Output", ouputName);
				operationNode.addItem(outputNode);
				XmlSchemaElement ouputSchemaElement = qNameLookup.get(outputQName);
				if( ouputSchemaElement == null )
					ouputSchemaElement = nameLookup.get(ouputName);
				addAndPopulateElement(outputNode, ouputSchemaElement);
				
				for (Fault fault : (Collection<Fault>) op.getFaults().<Fault>values()) {
					QName faultQName = fault.getMessage().getQName();
					String faultName = fault.getName();
					xelement faultNode = new xelement("Fault", faultName);
					operationNode.addItem(faultNode);
					XmlSchemaElement faultSchemaElement = qNameLookup.get(faultQName);
					if( faultSchemaElement == null )
						faultSchemaElement = nameLookup.get(faultQName.getLocalPart());
					addAndPopulateElement(faultNode, faultSchemaElement);
				}

			}
			System.out.println("");
		}
		/*
		 * messages
		 */
		Set<QName> messageSet = defs.getMessages().<QName>keySet();
		for (QName msgQName : messageSet)
		{
			String messageName = msgQName.getLocalPart();
			xelement msgNode = new xelement("message", messageName);
			result.add(msgNode);

			Message msg = defs.getMessage(msgQName);
			for ( Part part: (Collection<Part>) msg.getParts().<Part>values())
			{
				QName partElement = part.getElementName();
				if( partElement!= null)
				{
					addAndPopulateElement(msgNode, qNameLookup.get(partElement));
				}
			}
		}

		return result;
	}

	public TypeInfo getPrimitiveTypeName(XmlSchemaSimpleType simpleType)
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
			for( XmlSchemaFacet facet : typeRestriction.getFacets() )
			{
				CLAZZ clazz = CLAZZ.valueOf(facet.getClass().getSimpleName());
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
				case XmlSchemaEnumerationFacet:
					typeInfo.addEnumeration(value);
					break;
				default:
					break;
					
				}
			}
		}
		else
			throw new RuntimeException("Unsupported XmlSchemaSimpleTypeContent: "+content.getClass().getName()+"!");
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
			XmlSchemaElement xmlSchemaElement = qNameLookup.get(targetQName);
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

	void processComplexType( xelement element, XmlSchemaComplexType complexType)
	{
		QName baseTypeName = complexType.getBaseSchemaTypeName();
		if( baseTypeName!=null)
		{
			XmlSchemaElement se = qNameLookup.get(baseTypeName);
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
}
