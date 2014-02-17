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
//	@SuppressWarnings("serial")
//	public static HashMap<String, Option> argsMap = new HashMap<String, Option>(){{
//		put("/a", Option.ALL);
//		put("/e",Option.ELEMENT);
//		put("/t",Option.TYPE);
//		put("/st",Option.SHOW_TYPE);
//		put("/sc",Option.SHOW_CARDI);
//		put("/sr",Option.SHOW_RANGE);
//	}};

	public static void main(String[] args) throws Exception
	{

		String filename = args[0];
		String command = args.length > 1 ? args[1]: "/a";
		DistillerWriter distillerWriter = new DistillerWriter(command);
		distillerWriter.setOption(command);
		switch((command))
		{
		case "/le": // list all element in console
			for(String el : CastorUtil.getRootElements(CastorUtil.getSchema(new File(filename))))
			{
				System.out.println(el);
			}
			break;
		case "/lt": // list all comple types in console
		case "/a":
			distillerWriter.transform(new File(filename));
			break;
		}
	}
}
