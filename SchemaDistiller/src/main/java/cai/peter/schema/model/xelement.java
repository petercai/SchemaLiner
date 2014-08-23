/***********************************************
 * Copyright (c) 2013 Peter Cai                *
 * All rights reserved.                        *
 ***********************************************/
package cai.peter.schema.model;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;


public class xelement extends xnode
{
	protected String type;
	public String rangeFrom,rangeTo;
	protected String	path;
	protected String	ns;
	protected String	cardinality	= "";
	protected List<xattribute>	attributes	= new ArrayList<xattribute>();
	public xelement(String name)
	{
		super(name);
	}

	public xelement(String ns, String name )
	{
		super(name);
		this.ns = ns;
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

	public String getPath()
	{
		return path==null?("/"+getQName()):(path+"/"+getQName());
	}

	public void setCardinality(int minOccurs, int maxOccurs)
	{
		if (minOccurs == 0 && maxOccurs == 1)
			cardinality = "?"; // optional
		if (minOccurs == 0  && maxOccurs == -1 )
			cardinality = "*";
		if (minOccurs == 1  && maxOccurs == -1 )
			cardinality = "+";
	}

	public void setCardinality(long minOccurs, long maxOccurs)
	{
		if (minOccurs == 0 && maxOccurs == 1)
			cardinality = "?"; // optional
		if (minOccurs == 0  && maxOccurs == -1 )
			cardinality = "*";
		if (minOccurs == 1  && maxOccurs == -1 )
			cardinality = "+";
	}

	public void addAttribute(xattribute attr)
	{
		attributes.add(attr);
	}

	public String getQName()
	{
		return ns!=null?ns+":"+name:name;
	}

	public String getNs()
	{
		return ns;
	}

	public void setNs(String ns)
	{
		this.ns = ns;
	}

	public String getCardinality()
	{
		return cardinality;
	}

	public List<xattribute> getAttributes()
	{
		return attributes;
	}

	public void setPath(String path)
	{
		this.path = path;
	}

	@Override
	public void addItem(xnode child)
	{
		super.addItem(child);
		((xelement)child).setPath(getPath());
	}

	@Override
	public List<xelement> getItems()
	{
		ArrayList<xelement> result = new ArrayList<xelement>(items.size());
		for( xnode n : items)
		{
			result.add((xelement)n);
		}
		return result;
	}

	@Override
	public void addGroup(xgroup group)
	{
		super.addGroup(group);
		group.setPath(getPath());
	}
}
