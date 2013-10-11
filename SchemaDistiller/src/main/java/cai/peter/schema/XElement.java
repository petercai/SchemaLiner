/*******************************************************************************
 * Copyright (c) 2010 Peter Cai
 * All rights reserved.
 *
 * ***************************************************************
 * Filename:    XElement.java
 * Description: SchemaDistiller
 * Created by:  Peter Cai
 * Created on:  Aug 21, 2013
 ******************************************************************************/




package cai.peter.schema;

import java.text.MessageFormat;


/**
 * @author Peter Cai
 *
 */
public class XElement extends XNode
{
	public String type = "";
	public String[] range = new String[]{null,null};
	public XElement(String name)
	{
		super(name);
	}

	public XElement(XNode node, String type)
	{
		super(node.name);
		super.path = node.path;
		super.cardinality = node.cardinality;
		super.children = node.children;
		this.type = type;
	}

	@Override
	public String toString()
	{
		StringBuilder s = new StringBuilder();
		s.append(path);
		s.append(name);
		s.append(cardinality);
		s.append((type.isEmpty()?"":(":"+type)));
		if( range[0]!=null || range[1] != null)
			s.append(MessageFormat.format(	"[{0}.{1}]",
			                              	(range[0]==null?".":range[0]),
			                              	(range[1]==null?".":range[1])));

		return s.toString();
	}
}
