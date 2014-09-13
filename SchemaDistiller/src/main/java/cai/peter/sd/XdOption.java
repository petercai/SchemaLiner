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
//	public boolean showType=false;
//	public boolean showCardinality=false;
	public boolean showMultiFile=false;
	
	public enum Option {PATH_ONLY, SHOW_TYPEINFO, SHOW_CARDINALITY}
	public Option opt = Option.PATH_ONLY;
	
	static private XdOption instance = new XdOption();
	
	public static XdOption getInstance()
	{
		return instance;
	}
}
