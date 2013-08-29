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
			switch( order )
			{
			case "choice":
				s.append(path);
				s.append("(");
				for(String item : items)
				{
					s.append(item);
					s.append("|");
				}
				s.append(")");
				result = s.toString();
				break;
			case "all":
				s.append(path);
				s.append("<");
				for(String item : items)
				{
					s.append(item);
					s.append("|");
				}
				s.append(">");
				result = s.toString();
				break;
			}
		}
		return result;
	}

}
