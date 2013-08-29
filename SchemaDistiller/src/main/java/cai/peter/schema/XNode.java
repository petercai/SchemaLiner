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
package cai.peter.schema;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Peter Cai
 *
 */
public class XNode
{
	public String	name = "";
	public String				path		= ""; // always ends with "/" except root
	public String				cardinality	= "";	// Mandatory
	public List<XNode>		children = new ArrayList<XNode>();
	public List<XGroup>		groups = new ArrayList<XGroup>();
	public List<XAttribute>	attributes = new ArrayList<XAttribute>();

	public XNode(String name, String path, String cardinality,
		List<XNode> children)
	{
		super();
		this.name = name;
		this.path = path;
		this.cardinality = cardinality;
		this.children = children;
	}

	public XNode(String name)
	{
		super();
		this.name = name;
	}

	public XNode(XNode parent, String name)
	{
		super();
		this.name = name;
		this.path = parent.getPath();
		parent.addChild(this);
	}

	public String getPath()
	{
		return path +  name + "/";
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

	public void addChild( XNode node)
	{
		children.add(node);
	}

	public void addAttribute( XAttribute attr)
	{
		attributes.add(attr);
	}

	public void setParent( XNode parent)
	{
		this.path = parent.getPath();
		parent.addChild(this);
	}

	@Override
	public String toString()
	{
		return path + name + cardinality;
	}
}
