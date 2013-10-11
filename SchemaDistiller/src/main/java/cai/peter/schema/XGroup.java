/*******************************************************************************
 * Copyright (c) 2010 Peter Cai
 * All rights reserved.
 *
 * ***************************************************************
 * Filename:    XGroup.java
 * Description: SchemaDistiller
 * Created by:  Peter Cai
 * Created on:  Aug 28, 2013
 ******************************************************************************/




package cai.peter.schema;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * @author Peter Cai
 *
 */
public class XGroup
{
	public String path;
	public void setPath(String path)
	{
		this.path = path;
	}

	public String order;
	public XGroup(String order)
	{
		super();
		this.order = order;
	}

	public List<String> items = new ArrayList<String>();
	public List<XGroup> groups = new ArrayList<XGroup>();

	public void setParent( XNode parent)
	{
		parent.groups.add(this);
	}

	@Override
	public String toString()
	{
		String result = null;

		if(items.size()>1)
		{
			StringBuilder s = new StringBuilder();
			@SuppressWarnings("serial")
			HashMap<String, Integer> orderMap = new HashMap<String, Integer>(2){{
				put("choice",1);
				put("all",2);
			}};
			switch(orderMap.get( order ))
			{
			case 1:
				s.append(path);
				s.append("(");
				s.append(Arrays.toString(items.toArray()).replaceAll(",", "|"));
				s.append(")");
				result = s.toString();
				break;
			case 2:
				s.append(path);
				s.append("<");
				s.append(Arrays.toString(items.toArray()).replaceAll(",", "|"));
				s.append(">");
				result = s.toString();
				break;
			}
		}
		return result;
	}

}
