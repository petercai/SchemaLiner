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
	protected String	name;
	protected String	path;
	


	public xnode(String name)
	{
		super();
		this.name = name;
	}


	public void addItem( xnode child)
	{
		items.add(child);
		child.setPath(getPath());
	}

	public List<xnode> getItems()
	{
		return items;
	}

	public String getName()
	{
		return name;
	}
	
	abstract public String getPath();

	public void setPath(String path)
	{
		this.path = path;
	}


	public void setName(String name)
	{
		this.name = name;
	}
}
