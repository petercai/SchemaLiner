/***********************************************
 * Copyright (c) 2013 Peter Cai                *
 * All rights reserved.                        *
 ***********************************************/

package cai.peter.schema;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import javax.wsdl.WSDLException;
import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.exolab.castor.xml.schema.Schema;
import org.junit.Before;
import org.junit.Test;

import cai.peter.schema.distiller.XsdDistiller;
import cai.peter.schema.model.xelement;
import cai.peter.schema.model.xnode;

public class XsdDistillerTest
{
	/**
	 * Logger for this class
	 */
	private static final Logger	logger	= Logger.getLogger(XsdDistillerTest.class);
//	String xsdfile = "Services/External/CustomerAccountingUnitServiceIntf.xsd";
//	String xsdfile = "at_wsdl/wsdl/Schemas/Services/Internal/ResourceManagement.xsd";
//	String xsdfile = "at_wsdl/wsdl/Schemas/Services/Internal/AccountFacility.xsd";
	String xsdfile = "at_wsdl/wsdl/Schemas/Services/External/ProvideAccountTransfer.xsd";
	private Schema	schema;
	private XsdDistiller	xsddistiller;
	private File	schemaFile;
	@Before
	public void init() throws IOException, URISyntaxException, WSDLException
	{
		URL resource = this.getClass().getClassLoader().getResource(xsdfile);
		URI uri = resource.toURI();
		schemaFile = new File(uri);
		schema = CastorUtil.getSchema(schemaFile);
		xsddistiller = new XsdDistiller();
	}



	@Test
	public void testXsdDistiller() throws Exception
	{
		logElement(
		xsddistiller.processElements(schema)
				);
	}

	void logElement(List<xnode> nodes)
	{
		if( nodes == null ) return;
		for( xnode node : nodes)
		{
			if( node.toString()!=null)
				logger.info(node);
			logElement(node.getItems());
		}
	}
	@Test
	public void testXmlSchemaCollection() throws FileNotFoundException
	{
		InputStream is = new FileInputStream(schemaFile);
		XmlSchemaCollection schemaCol = new XmlSchemaCollection();
		XmlSchema schema = schemaCol.read(new StreamSource(is));
		Map<QName, XmlSchemaElement> elements = schema.getElements();
	}


}
