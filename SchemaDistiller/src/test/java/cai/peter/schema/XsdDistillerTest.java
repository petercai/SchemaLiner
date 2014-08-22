/***********************************************
 * Copyright (c) 2013 Peter Cai                *
 * All rights reserved.                        *
 ***********************************************/

package cai.peter.schema;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import javax.wsdl.Definition;
import javax.wsdl.WSDLException;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.wsdl.WSDLManager;
import org.apache.log4j.Logger;
import org.exolab.castor.xml.schema.Schema;
import org.junit.Before;
import org.junit.Test;

import cai.peter.schema.distiller.WsdlDistiller;
import cai.peter.schema.distiller.XsdDistiller;
import cai.peter.schema.model.xnode;

public class XsdDistillerTest
{
	/**
	 * Logger for this class
	 */
	private static final Logger	logger	= Logger.getLogger(XsdDistillerTest.class);
	String xsdfile = "Services/External/CustomerAccountingUnitServiceIntf.xsd";
	String wsdlfile = "at_wsdl/wsdl/AccountTransferHTTP.wsdl";
	private Schema	schema;
	private XsdDistiller	xsddistiller;
	private WsdlDistiller	wsdldistiller;
	private Definition	defs;

	@Before
	public void init() throws IOException, URISyntaxException, WSDLException
	{
		URL resource = this.getClass().getClassLoader().getResource(xsdfile);
		URI uri = resource.toURI();
		schema = CastorUtil.getSchema(new File(uri));
		xsddistiller = new XsdDistiller();

		URL wsdlUrl = this.getClass().getClassLoader().getResource(wsdlfile);
//		URL wsdlUrl = this.getClass().getClassLoader().getResource("ebay/PayPalSvc.wsdl");

		Bus bus = BusFactory.getDefaultBus();
		WSDLManager wsdlManager = bus.getExtension(WSDLManager.class);
		defs = wsdlManager.getDefinition(wsdlUrl);


	}



	@Test
	public void testWsdlDistiller() throws Exception
	{

	}

	@Test
	public void testXsdDistiller() throws Exception
	{
		wsdldistiller.processDefinitions(defs);
//		logElement();
	}

	void logElement(List<xnode> nodes)
	{
		if( nodes == null ) return;
		for( xnode node : nodes)
		{
			logger.info(node);
			logElement(node.getChildren());
		}
	}

}
