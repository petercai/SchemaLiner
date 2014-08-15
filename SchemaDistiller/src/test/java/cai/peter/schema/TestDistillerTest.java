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

import org.apache.log4j.Logger;
import org.exolab.castor.xml.schema.Schema;
import org.junit.Before;
import org.junit.Test;

import cai.peter.schema.distiller.XsdDistiller;
import cai.peter.schema.model.xnode;

public class TestDistillerTest
{
	/**
	 * Logger for this class
	 */
	private static final Logger	logger	= Logger.getLogger(TestDistillerTest.class);
	String file = "Services/External/CustomerAccountingUnitServiceIntf.xsd";
	private Schema	schema;
	private XsdDistiller	dt;
	@Before
	public void init() throws IOException, URISyntaxException
	{
		URL resource = this.getClass().getClassLoader().getResource(file);
		URI uri = resource.toURI();
		schema = CastorUtil.getSchema(new File(uri));
		dt = new XsdDistiller();

	}
	@Test
	public void testProcessAllComplexTypes() throws Exception
	{
		dt.processTypes(schema);
	}
	
	@Test
	public void testProcessElements() throws Exception
	{
		logElement(dt.processElements(schema));
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
