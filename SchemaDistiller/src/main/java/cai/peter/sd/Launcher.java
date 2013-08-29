/*******************************************************************************
 * Copyright (c) 2010 Peter Cai
 * All rights reserved.
 *
 * ***************************************************************
 * Filename:    Launcher.java
 * Description: SchemaDistiller
 * Created by:  Peter Cai
 * Created on:  Aug 22, 2013
 ******************************************************************************/




package cai.peter.sd;

import java.io.File;

import cai.peter.schema.CastorUtil;

/**
 * @author Peter Cai
 *
 */
public class Launcher
{
	public static void main(String[] args) throws Exception
	{

		String filename = args[0];
		String command = args.length > 1 ? args[1]: "/a";
		DistillerWriter distillerWriter = new DistillerWriter(command);
		switch(command)
		{
		case "/e":
			for(String el : CastorUtil.getRootElements(CastorUtil.getSchema(new File(filename))))
			{
				System.out.println(el);
			}
			break;
		default:
			distillerWriter.transform(new File(filename));
			break;
		}
	}
}
