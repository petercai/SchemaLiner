/*******************************************************************************
 * Copyright (c) 2010 Peter Cai
 * All rights reserved.
 *
 * ***************************************************************
 * Filename:    DistillerWriter.java
 * Description: SchemaDistiller
 * Created by:  Peter Cai
 * Created on:  Aug 28, 2013
 ******************************************************************************/




package cai.peter.sd;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.exolab.castor.xml.schema.Schema;

import cai.peter.schema.CastorUtil;
import cai.peter.schema.Distiller;
import cai.peter.schema.XGroup;
import cai.peter.schema.XNode;

/**
 * @author Peter Cai
 *
 */
public class DistillerWriter
{
	private BufferedWriter bufferedWriter = null;


	String option;

	public DistillerWriter(String option)
	{
		super();
		this.option = option;
	}



	private void write(XGroup group) throws IOException
	{
		String line = group.toString();
		if( line != null )
		{
			bufferedWriter.append(line);
			bufferedWriter.append(System.getProperty("line.separator"));

		}
		if( !group.groups.isEmpty())
		{
			for( XGroup subgroup : group.groups)
				write(subgroup);
		}
	}

	private void write(XNode root) throws IOException
	{
		if( !root.name.isEmpty())
		{
			bufferedWriter.append(root.toString());
			bufferedWriter.append(System.getProperty("line.separator"));

		}

		for( XGroup group: root.groups)
		{
				write(group);
		}

		for( XNode node : root.children)
		{
			write(node);
		}
	}

	public void transform(File xsdFile) throws Exception
	{
		Schema schema = CastorUtil.getSchema(xsdFile);
		Distiller distiller = new Distiller();
		XNode root = distiller.processAll(schema), node=null;

		File outputFile = null;
		try
		{
			switch( option )
			{
			case "/a":
				outputFile = new File(xsdFile.toString()+".all");
				node = root;
				break;
			case "/e":
				break;
			case "/t":
				break;
			default:
				outputFile = new File(xsdFile.toString()+"."+option);
				node = distiller.elements.get(option);
				break;
			}
			if( node != null )
			{
				bufferedWriter = new BufferedWriter(new FileWriter(outputFile));
				write(node);
			}
		}
		finally
		{
			if( bufferedWriter != null)
				bufferedWriter.close();
		}

	}
}
