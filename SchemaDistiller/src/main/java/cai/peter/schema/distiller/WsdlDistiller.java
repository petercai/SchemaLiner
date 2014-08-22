/***********************************************
 * Copyright (c) 2014 Peter Cai
 * All rights reserved.
 *
 * Aug 22, 2014
 *
 ***********************************************/
package cai.peter.schema.distiller;

import org.apache.log4j.Logger;

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
import org.apache.ws.commons.schema.XmlSchemaComplexType;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaType;
import org.apache.ws.commons.schema.utils.XmlSchemaRef;

import cai.peter.schema.model.xelement;
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
					msgNode.addChild(processElement(xelements.get(partElement)));

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

	xnode processElement(XmlSchemaElement element)
	{
		String name = element.getName();
		xelement el = new xelement(name);
		el.setCardinality(element.getMinOccurs(), element.getMaxOccurs());
		/*
		 * TODO
		 */
		XmlSchemaRef<XmlSchemaElement> schemaRef = element.getRef();


		XmlSchemaType schemaType = element.getSchemaType();
		return el;
	}

	void processComplexType( xnode node, XmlSchemaType type)
	{
		/*
		 * TODO: extension
		 */
		if( type instanceof XmlSchemaComplexType )
		{
			QName baseSchemaTypeName = ((XmlSchemaComplexType)type).getBaseSchemaTypeName();
		}


	}

	/*
	 * TODO
	 */
	void processType()
	{}
}
