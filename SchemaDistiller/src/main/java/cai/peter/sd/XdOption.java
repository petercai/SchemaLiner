/***********************************************
 * Copyright (c) 2014 Peter Cai                *
 * All rights reserved.                        *
 ***********************************************/
package cai.peter.sd;

public class XdOption
{
	private XdOption()
	{
		super();
		
	}
	public boolean showType=false;
	public boolean showCardinality=false;
	public boolean showMultiFile=false;
	
	static private XdOption instance = new XdOption();
	
	public static XdOption getInstance()
	{
		return instance;
	}
}
