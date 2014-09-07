/***********************************************
 * Copyright (c) 2014 Peter Cai                *
 * All rights reserved.                        *
 ***********************************************/
package cai.peter.schema.model;

import java.util.ArrayList;
import java.util.List;

public class TypeInfo
{
	public TypeInfo()
	{
		super();
		
	}
	public TypeInfo(String name)
	{
		super();
		this.name = name;
	}
	String name;
	String max, min, total, fraction;
	List<String> enumeration = new ArrayList<String>();
	public String getName()
	{
		return name;
	}
	public void setName(String name)
	{
		this.name = name;
	}
	public String getMax()
	{
		return max;
	}
	public void setMax(String max)
	{
		this.max = max;
	}
	public String getMin()
	{
		return min;
	}
	public void setMin(String min)
	{
		this.min = min;
	}
	public List<String> getEnumeration()
	{
		return enumeration;
	}
	
	public void addEnumeration(String val)
	{
		this.enumeration.add(val);
	}
	public String getTotal()
	{
		return total;
	}
	public void setTotal(String total)
	{
		this.total = total;
	}
	public String getFraction()
	{
		return fraction;
	}
	public void setFraction(String fraction)
	{
		this.fraction = fraction;
	}
}
