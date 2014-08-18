/***********************************************
 * Copyright (c) 2013 Peter Cai                *
 * All rights reserved.                        *
 ***********************************************/
package cai.peter.sd;

import java.io.File;

public class Launcher
{
	public static void main(String[] args) throws Exception
	{
		String filename = args[0];
		SchemaTransformer.transform(new File(filename));
	}
}
