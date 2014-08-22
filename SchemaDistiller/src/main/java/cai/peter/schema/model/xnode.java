/***********************************************
 * Copyright (c) 2013 Peter Cai                *
 * All rights reserved.                        *
 ***********************************************/
package cai.peter.schema.model;

import java.util.ArrayList;
import java.util.List;

public class xnode
{
	protected String			path;
	protected String			name;
	protected String			ns;
	protected String			cardinality="";
	protected List<xnode>		children	= new ArrayList<xnode>();
	protected List<xgroup>		groups		= new ArrayList<xgroup>();
	protected List<xattribute>	attributes	= new ArrayList<xattribute>();


	public xnode(String name)
	{
		super();
		this.name = name;
	}

	public xnode(String ns, String name)
	{
		super();
		this.ns = ns;
		this.name = name;
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

	/*
	 * TODO
	 */
	public void setCardinality(long minOccurs, long maxOccurs)
	{
		if (minOccurs == 0 && maxOccurs == 1)
			cardinality = "?"; // optional
		if (minOccurs == 0  && maxOccurs == -1 )
			cardinality = "*";
		if (minOccurs == 1  && maxOccurs == -1 )
			cardinality = "+";
	}

	public void addChild( xnode child)
	{
		children.add(child);
		child.setPath(getPath());
	}

	public void addAttribute( xattribute attr)
	{
		attributes.add(attr);
	}

	public String getQName()
	{
		return ns!=null?ns+":"+name:name;
	}

	@Override
	public String toString()
	{
		StringBuilder s = new StringBuilder();
		s.append(path==null?"/":path);
		if( path !=null )
			s.append("/");
		s.append(ns!=null?ns+":"+name:name);
		s.append(cardinality);
		return s.toString();
	}

	public String getNs()
	{
		return ns;
	}

	public void setNs(String ns)
	{
		this.ns = ns;
	}

	public String getName()
	{
		return name;
	}

	public String getCardinality()
	{
		return cardinality;
	}

	public List<xnode> getChildren()
	{
		return children;
	}

	public List<xgroup> getGroups()
	{
		return groups;
	}
	public void addGroup(xgroup  group)
	{
		groups.add(group);
		group.setPath(getPath());
	}

	public List<xattribute> getAttributes()
	{
		return attributes;
	}

	public void setPath(String path)
	{
		this.path = path;
	}
}
