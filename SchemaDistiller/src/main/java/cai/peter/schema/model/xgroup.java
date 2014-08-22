/***********************************************
 * Copyright (c) 2013 Peter Cai                *
 * All rights reserved.                        *
 ***********************************************/
package cai.peter.schema.model;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Joiner;

public class xgroup
{
	protected String path;
	protected String order;
	protected List<xnode> nodes = new ArrayList<xnode>();
	protected List<xgroup> groups = new ArrayList<xgroup>();
	public xgroup(String order)
	{
		super();
		this.order = order;
	}

	public void setPath(String path)
	{
		this.path = path;
	}

	List<String> toStringList(List<xnode> nodes)
	{
		ArrayList<String> result = new ArrayList<String>(nodes.size());
		for( xnode node : nodes)
		{
			result.add(node.getName());
		}
		return result;
	}

	@Override
	public String toString()
	{
		String result = null;

		if(nodes.size()>1)
		{
			StringBuilder s = new StringBuilder();

			switch(( order ))
			{
			case "choice":
				s.append(path);
				s.append("/(");
				Joiner.on("|").appendTo(s, toStringList(nodes));
				s.append(")");
				result = s.toString();
				break;
			case "all":
				s.append(path);
				s.append("/<");
				Joiner.on("|").appendTo(s, toStringList(nodes));
				s.append(">");
				result = s.toString();
				break;
			}
		}
		return result;
	}

	public List<xgroup> getGroups()
	{
		return groups;
	}

	public void addGroup(xgroup group)
	{
		groups.add(group);
	}

	public List<xnode> getNodes()
	{
		return nodes;
	}

	public void addNode(xnode item)
	{
		nodes.add(item);
	}

	public String getOrder()
	{
		return order;
	}

	public void setOrder(String order)
	{
		this.order = order;
	}

}
