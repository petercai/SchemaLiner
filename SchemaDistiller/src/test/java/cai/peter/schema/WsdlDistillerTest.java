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
import javax.wsdl.Input;
import javax.wsdl.Output;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.AttributeExtensible;
import javax.wsdl.extensions.ExtensionRegistry;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;

import org.apache.axis2.addressing.AddressingConstants;
import org.apache.log4j.Logger;
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
	String wsdlATSeperated = "at_wsdl/wsdl/AccountTransferHTTP.wsdl";
	String wsdlATFull = "at_wsdl/wsdl/AccountTransferFull.wsdl";
	String wsdlETransfer = "ETransfer_SFS_20140815/ETransferIncomingV1_0_0.wsdl";
	String wsdlfile = "ebay/PayPalSvc.wsdl";
	private WsdlDistiller	wsdldistiller = new WsdlDistiller();
//	private Definition	defs;

	@Before
	public void init() throws IOException, URISyntaxException, WSDLException
	{
//		Definition definition = getWSDLDefinition();
//        
//        defs = definition;
	}



	Definition getWSDLDefinition(String filename) throws WSDLException,
			IllegalArgumentException
	{
		Definition definition = null;

        WSDLReader reader = WSDLFactory.newInstance().newWSDLReader();
        reader.setFeature("javax.wsdl.importDocuments", true);

        ExtensionRegistry extReg = WSDLFactory.newInstance().newPopulatedExtensionRegistry();
        extReg.registerExtensionAttributeType(Input.class,
                new QName(AddressingConstants.Final.WSAW_NAMESPACE, AddressingConstants.WSA_ACTION),
                AttributeExtensible.STRING_TYPE);
        extReg.registerExtensionAttributeType(Output.class,
                new QName(AddressingConstants.Final.WSAW_NAMESPACE, AddressingConstants.WSA_ACTION),
                AttributeExtensible.STRING_TYPE);
        reader.setExtensionRegistry(extReg);

        definition =  reader.readWSDL(filename);
		return definition;
	}



	@Test
	public void testWsdlDistillerFull() throws Exception
	{
		URL url = this.getClass().getClassLoader().getResource(wsdlATFull);
		Definition wsdlDefinition = getWSDLDefinition(url.toURI().toString());
		logElement(wsdldistiller.processDefinitions(wsdlDefinition));
	}

	@Test
	public void testWsdlDistillerFull2() throws Exception
	{
		URL url = this.getClass().getClassLoader().getResource(wsdlETransfer);
		Definition wsdlDefinition = getWSDLDefinition(url.toURI().toString());
		logElement(wsdldistiller.processDefinitions(wsdlDefinition));
	}
	
	@Test
	public void testWsdlDistillerSeperated() throws Exception
	{
		URL url = this.getClass().getClassLoader().getResource(wsdlATSeperated);
		Definition wsdlDefinition = getWSDLDefinition(url.toURI().toString());
		logElement(wsdldistiller.processDefinitions(wsdlDefinition));
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
