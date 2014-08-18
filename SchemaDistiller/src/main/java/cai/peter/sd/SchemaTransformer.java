/***********************************************
 * Copyright (c) 2013 Peter Cai                *
 * All rights reserved.                        *
 ***********************************************/
package cai.peter.sd;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.exolab.castor.xml.schema.Schema;

import cai.peter.schema.CastorUtil;
import cai.peter.schema.distiller.XsdDistiller;
import cai.peter.schema.model.xgroup;
import cai.peter.schema.model.xnode;

public class SchemaTransformer
{
	private BufferedWriter bufferedWriter = null;
	
	public SchemaTransformer(File outputFile) throws IOException
	{
		bufferedWriter = new BufferedWriter(new FileWriter(outputFile));
	}

	protected void transform(xgroup group) throws IOException
	{
		String line = group.toString();
		if( line != null )
		{
			bufferedWriter.append(line);
			bufferedWriter.append(System.getProperty("line.separator"));
		}
		List<xgroup> groups = group.getGroups();
		if( !groups.isEmpty())
		{
			for( xgroup subgroup : groups)
				transform(subgroup);
		}
	}

	protected void transform(xnode root) throws IOException
	{
		if( !root.getName().isEmpty())
		{
			bufferedWriter.append(root.toString());
			bufferedWriter.append(System.getProperty("line.separator"));
		}

		for( xgroup group: root.getGroups())
		{
				transform(group);
		}

		for( xnode node : root.getChildren())
		{
			transform(node);
		}
	}

	public static void transform(File xsdFile) throws Exception
	{
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

	protected void close() throws IOException
	{
		if( bufferedWriter != null)
			bufferedWriter.close();
	}
}