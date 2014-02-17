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

	private String option;

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
		XNode root = distiller.processAllElement(schema), node=null;

		File outputFile = null;
		try
		{
			switch(( option) )
			{
			case "/a":
				outputFile = new File(xsdFile.toString()+".all");
				bufferedWriter = new BufferedWriter(new FileWriter(outputFile));
				write(root);
				break;
			case "/e":
				for( XNode node : root)
				break;
			case "/st":
				break;
			default:
				outputFile = new File(xsdFile.toString()+"."+option);
				node = distiller.getElement(option);
				break;
			}
			
		}
		finally
		{
			if( bufferedWriter != null)
				bufferedWriter.close();
		}

	}



	public void setOption(String option)
	{
		this.option = option;
	}
}
