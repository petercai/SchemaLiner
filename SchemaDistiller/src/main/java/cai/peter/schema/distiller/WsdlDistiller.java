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
import java.util.List;
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

import cai.peter.schema.model.xnode;


public class WsdlDistiller
{
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
				System.out.println("  TargetNamespace: \t" + schema.getTargetNamespace());
			}

		}

	}

	public List<xnode> processDefinitions(Definition defs)
	{
		ArrayList<xnode> result = new ArrayList<xnode>();



		processSchemas(defs);





		Set<QName> keySet = defs.getMessages().<QName>keySet();
		for (QName qName : keySet)
		{
//			QName qName = (QName)key;
			String localPart = qName.getLocalPart();
			xnode msgNode = new xnode("message", localPart);
			result.add(msgNode);
			Message msg = defs.getMessage(qName);
			System.out.println("");
			System.out.println("  Message Name: " + localPart);
			System.out.println("  Message Parts: ");
			Collection<Part> values = msg.getParts().<Part>values();
			for ( Part part: values)
			{
				System.out.println("    Part Name: " + part.getName());
				System.out.println("    Part Element: " + ((part.getElementName() != null) ? part.getElementName() : "not available!"));
				System.out.println("    Part Type: " + ((part.getTypeName() != null) ? part.getTypeName() : "not available!" ));
			}
		}

		return result;
	}
}
