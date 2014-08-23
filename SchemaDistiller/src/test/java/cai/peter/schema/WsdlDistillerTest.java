/***********************************************
 * Copyright (c) 2013 Peter Cai                *
 * All rights reserved.                        *
 ***********************************************/

package cai.peter.schema;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import javax.wsdl.Definition;
import javax.wsdl.WSDLException;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.wsdl.WSDLManager;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import cai.peter.schema.distiller.WsdlDistiller;
import cai.peter.schema.model.xelement;
import cai.peter.schema.model.xnode;

public class WsdlDistillerTest
{
	/**
	 * Logger for this class
	 */
	private static final Logger	logger	= Logger.getLogger(WsdlDistillerTest.class);
//	String wsdlfile = "at_wsdl/wsdl/AccountTransferHTTP.wsdl";
	String wsdlfile = "at_wsdl/wsdl/AccountTransferFull.wsdl";
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

	void logElement(List<xelement> nodes)
	{
		if( nodes == null ) return;
		for( xelement node : nodes)
		{
			logger.info(node);
			logElement(node.getItems());
		}
	}

}
