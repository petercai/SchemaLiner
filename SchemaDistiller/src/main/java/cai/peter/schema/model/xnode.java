/*******************************************************************************
 * Copyright (c) 2010 Peter Cai
 * All rights reserved.
 *
 * ***************************************************************
 * Filename:    XNode.java
 * Description: SchemaDistiller
 * Created by:  Peter Cai
 * Created on:  Aug 21, 2013
 ******************************************************************************/
package cai.peter.schema.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Peter Cai
 *
 */
public class xnode
{
	protected String			name;
	protected String			path;
	protected String			cardinality;
	protected List<xnode>		children	= new ArrayList<xnode>();
	protected List<xgroup>		groups		= new ArrayList<xgroup>();
	protected List<xattribute>	attributes	= new ArrayList<xattribute>();

//	public xnode(String name, String path, String cardinality,
//		List<xnode> children)
//	{
//		super();
//		this.name = name;
//		this.path = path;
//		this.cardinality = cardinality;
//		this.children = children;
//	}

	public xnode(String name)
	{
		super();
		this.name = name;
	}

//	public xnode(xnode parent, String name)
//	{
//		super();
//		this.name = name;
//		this.path = parent.getPath();
//		parent.addChild(this);
//	}

	public String getPath()
	{
		return path==null?("/"+name):(path+"/"+name);
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

	public void addChild( xnode child)
	{
		child.setPath(getPath());
		children.add(child);
	}

	public void addAttribute( xattribute attr)
	{
		attributes.add(attr);
	}

//	public void setParent( xnode parent)
//	{
//		this.path = parent.getPath();
//		parent.addChild(this);
//	}

	@Override
	public String toString()
	{
		return path + name + cardinality;
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
