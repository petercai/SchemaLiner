/***********************************************
 * Copyright (c) 2013 Peter Cai                *
 * All rights reserved.                        *
 ***********************************************/
package cai.peter.schema.model;

import java.text.MessageFormat;


public class xelement extends xnode
{
	protected String type;
//	public String[] range = new String[]{null,null};
	public String rangeFrom,rangeTo;
	public xelement(String name)
	{
		super(name);
	}

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
		if( rangeFrom!=null || rangeTo != null)
			s.append(MessageFormat.format(	"[{0}.{1}]",
			                              	(rangeFrom==null?".":rangeFrom),
			                              	(rangeTo==null?".":rangeTo)));

		return s.toString();
	}

	public void setType(String type)
	{
		this.type = type;
	}
}
