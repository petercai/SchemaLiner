/***********************************************
 * Copyright (c) 2013 Peter Cai                *
 * All rights reserved.                        *
 ***********************************************/

package cai.peter.schema;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import javax.wsdl.Definition;
import javax.wsdl.WSDLException;
import javax.xml.transform.stream.StreamSource;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.wsdl.WSDLManager;
import org.apache.log4j.Logger;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.junit.Before;
import org.junit.Test;

import cai.peter.schema.distiller.WsdlDistiller;
import cai.peter.schema.model.xnode;

public class WsdlDistillerTest
{
	/**
	 * Logger for this class
	 */
	private static final Logger	logger	= Logger.getLogger(WsdlDistillerTest.class);
	String wsdlfile = "at_wsdl/wsdl/AccountTransferHTTP.wsdl";
//	String wsdlfile = "at_wsdl/wsdl/AccountTransferFull.wsdl";
//	String wsdlfile = "ebay/PayPalSvc.wsdl";
	private WsdlDistiller	wsdldistiller = new WsdlDistiller();
	private Definition	defs;

	@Before
	public void init() throws IOException, URISyntaxException, WSDLException
	{

		URL wsdlUrl = this.getClass().getClassLoader().getResource(wsdlfile);
		Bus bus = BusFactory.getDefaultBus();
		WSDLManager wsdlManager = bus.getExtension(WSDLManager.class);
		defs = wsdlManager.getDefinition(wsdlUrl);
	}



	@Test
	public void testWsdlDistiller() throws Exception
	{
		logElement(wsdldistiller.processDefinitions(defs));
	}

	void logElement(List<xnode> nodes)
	{
		if( nodes == null || nodes.size()==0) return;
		for( xnode node : nodes)
		{
			if( node.toString()!=null)
				logger.info(node);
			logElement(node.getItems());
		}
	}

}
