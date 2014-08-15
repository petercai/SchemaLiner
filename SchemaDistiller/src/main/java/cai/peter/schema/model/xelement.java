/***********************************************
 * Copyright (c) 2013 Peter Cai                *
 * All rights reserved.                        *
 ***********************************************/
package cai.peter.schema.model;

import java.text.MessageFormat;


public class xelement extends xnode
{
	public String type;
	public String[] range = new String[]{null,null};
	public xelement(String name)
	{
		super(name);
	}

//	public xelement(xnode node, String type)
//	{
//		super(node.name);
//		super.path = node.path;
//		super.cardinality = node.cardinality;
//		super.children = node.children;
//		this.type = type;
//	}

	@Override
	public String toString()
	{
		StringBuilder s = new StringBuilder();
		s.append(path==null?"/":path);
		if( path !=null )
			s.append("/");
		s.append(name);
		s.append(cardinality);
		s.append(type==null?"":":"+type);
		if( range[0]!=null || range[1] != null)
			s.append(MessageFormat.format(	"[{0}.{1}]",
			                              	(range[0]==null?".":range[0]),
			                              	(range[1]==null?".":range[1])));

		return s.toString();
	}

	public void setType(String type)
	{
		this.type = type;
	}
}
