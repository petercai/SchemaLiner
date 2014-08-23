/***********************************************
 * Copyright (c) 2013 Peter Cai                *
 * All rights reserved.                        *
 ***********************************************/
package cai.peter.schema.model;

import java.util.ArrayList;
import java.util.List;

public abstract class xnode
{
	protected List<xnode>		items	= new ArrayList<xnode>();
	protected List<xgroup>		groups		= new ArrayList<xgroup>();
	protected String	name;
	protected String	path;
	


	public xnode(String name)
	{
		super();
		this.name = name;
	}

//	public xnode(String ns, String name)
//	{
//		super();
//		this.ns = ns;
//		this.name = name;
//	}

	public void addItem( xnode child)
	{
		items.add(child);
//		child.setPath(getPath());
	}

//	@Override
//	public String toString()
//	{
//		StringBuilder s = new StringBuilder();
//		s.append(path==null?"/":path);
//		if( path !=null )
//			s.append("/");
//		s.append(ns!=null?ns+":"+name:name);
//		s.append(cardinality);
//		return s.toString();
//	}

	public List<? extends xnode> getItems()
	{
		return items;
	}

	public List<xgroup> getGroups()
	{
		return groups;
	}
	public void addGroup(xgroup  group)
	{
		groups.add(group);
		//TODO
//		group.setPath(getPath());
	}

	public String getName()
	{
		return name;
	}
	
	abstract String getPath();

	public void setPath(String path)
	{
		this.path = path;
	}
}
