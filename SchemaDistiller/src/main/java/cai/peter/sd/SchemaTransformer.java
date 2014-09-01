/***********************************************
 * Copyright (c) 2013 Peter Cai                *
 * All rights reserved.                        *
 ***********************************************/
package cai.peter.sd;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.List;

import javax.wsdl.Definition;
import javax.wsdl.Input;
import javax.wsdl.Output;
import javax.wsdl.extensions.AttributeExtensible;
import javax.wsdl.extensions.ExtensionRegistry;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;

import org.apache.axis2.addressing.AddressingConstants;
import org.exolab.castor.xml.schema.Schema;

import cai.peter.schema.CastorUtil;
import cai.peter.schema.distiller.WsdlDistiller;
import cai.peter.schema.distiller.XsdDistiller;
import cai.peter.schema.model.xnode;

public class SchemaTransformer
{
	private BufferedWriter bufferedWriter = null;

	public SchemaTransformer(File outputFile) throws IOException
	{
		bufferedWriter = new BufferedWriter(new FileWriter(outputFile));
	}


	protected void transform(xnode root) throws IOException
	{
		if( /*!root.getName().isEmpty() &&*/ root.toString()!=null)
		{
			bufferedWriter.append(root.toString());
			bufferedWriter.append(System.getProperty("line.separator"));
		}

		for( xnode node : root.getItems())
		{
			transform(node);
		}
	}

	public static void transform(File file) throws Exception
	{
		if( file.isDirectory())
		{
			/*
			 * XSD
			 */
			File[] listFiles = file.listFiles(new FilenameFilter()
			{
				@Override
				public boolean accept(File dir, String name)
				{
					String lowerCase = name.toLowerCase();
					return lowerCase.endsWith(".xsd");
				}
			});
			for( File f : listFiles)
			{
				transformXsd(f);
			}
			/*
			 * WSDL
			 */
			listFiles = file.listFiles(new FilenameFilter()
			{
				@Override
				public boolean accept(File dir, String name)
				{
					String lowerCase = name.toLowerCase();
					return lowerCase.endsWith(".wsdl");
				}
			});

		}
		else
		{
			String filename = file.getName().toLowerCase();
			if( filename.endsWith(".xsd"))
				transformXsd(file);
			else if( filename.endsWith(".wsdl"))
				transformWsdl(file);
		}
	}

	protected static void transformXsd(File xsdFile) throws Exception
	{
		System.out.println(xsdFile.toString());
		Schema schema = CastorUtil.getSchema(xsdFile);
		XsdDistiller distiller = new XsdDistiller();
		List<xnode> elements = distiller.processElements(schema);

		SchemaTransformer allInOneFile = new SchemaTransformer(new File(xsdFile.toString()+".all"));
		for( xnode node : elements)
		{
			allInOneFile.transform(node);
			String name = node.getName();
			File outputFile = new File(xsdFile.toString()+"."+name);
			SchemaTransformer elementFile = new SchemaTransformer((outputFile));
			elementFile.transform(node);
			elementFile.close();
		}
		allInOneFile.close();
	}

	protected static void transformWsdl(File wsdlFile) throws Exception
	{
		WsdlDistiller distiller = new WsdlDistiller();


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

        Definition defs = reader.readWSDL(wsdlFile.toString());
		List<xnode> elements = distiller.processDefinitions(defs);

		SchemaTransformer allInOneFile = new SchemaTransformer(new File(wsdlFile.toString()+".all"));
		for( xnode node : elements)
		{
			allInOneFile.transform(node);
			String name = node.getName();
			File outputFile = new File(wsdlFile.toString()+"."+name);
			SchemaTransformer elementFile = new SchemaTransformer((outputFile));
			elementFile.transform(node);
			elementFile.close();
		}
		allInOneFile.close();
	}


	protected void close() throws IOException
	{
		if( bufferedWriter != null)
			bufferedWriter.close();
	}
}
