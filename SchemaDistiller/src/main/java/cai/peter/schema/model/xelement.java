/***********************************************
 * Copyright (c) 2013 Peter Cai                *
 * All rights reserved.                        *
 ***********************************************/
package cai.peter.schema.model;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;


public class xelement extends xnode
{
	protected String type;
//	public String minOrTotal,maxOrFraction;
	protected String min,max;
	protected String total,fraction;
	protected String	ns;
	protected String	cardinality	= "";
	protected List<xattribute>	attributes	= new ArrayList<xattribute>();
	public xelement(String name)
	{
		super(name);
	}

	public xelement(String ns, String name )
	{
		super(name);
		this.ns = ns;
	}

	@Override
	public String toString()
	{
		StringBuilder s = new StringBuilder();
		s.append(path==null?"/":path);
		if( path !=null )
			s.append("/");
		s.append(getQName());
		s.append(cardinality);
		s.append(type==null?"":":"+type);
		if( min!=null || max!= null)
			s.append(MessageFormat.format(	"[{0}.{1}]",
			                              	(min==null?".":min),
			                              	(max==null?".":max)));
		if( total!=null )
			s.append(MessageFormat.format(	"[{0}.{1}]",
					(total),
					(fraction==null?"0":fraction)));

		return s.toString();
	}

	public String toPathInfo()
	{
		StringBuilder s = new StringBuilder();
		s.append(path==null?"/":path);
		if( path !=null )
			s.append("/");
		s.append(getQName());
		
		return s.toString();
	}
	
	public String toCardinlityInfo()
	{
		return toPathInfo()+cardinality;
	}
	
	public String toTypeInfo()
	{
		if( type == null ) return toCardinlityInfo();
		
		StringBuilder s = new StringBuilder(toCardinlityInfo());
		s.append(":"+type);
		if( min!=null || max!= null)
			s.append(MessageFormat.format(	"[{0}.{1}]",
					(min==null?".":min),
					(max==null?".":max)));
		if( total!=null )
			s.append(MessageFormat.format(	"[{0}.{1}]",
					(total),
					(fraction==null?"0":fraction)));
		
		return s.toString();
	}
	
	public void setType(String type)
	{
		this.type = type;
	}

	public void setTypeInfo( TypeInfo info)
	{
		this.type = info.getName();
		this.min= info.getMin();
		this.max= info.getMax();
		this.total = info.getTotal();
		this.fraction = info.getFraction();
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

	public void setCardinality(long minOccurs, long maxOccurs)
	{
		if (minOccurs == 0 && maxOccurs == 1)
			cardinality = "?"; // optional
		if (minOccurs == 0  && maxOccurs == -1 )
			cardinality = "*";
		if (minOccurs == 1  && maxOccurs == -1 )
			cardinality = "+";
	}

	public void addAttribute(xattribute attr)
	{
		attributes.add(attr);
	}

	public String getQName()
	{
		return ns!=null?ns+":"+name:name;
	}

	public String getNs()
	{
		return ns;
	}

	public void setNs(String ns)
	{
		this.ns = ns;
	}

	public String getCardinality()
	{
		return cardinality;
	}

	public List<xattribute> getAttributes()
	{
		return attributes;
	}

	public String getMin()
	{
		return min;
	}

	public void setMin(String min)
	{
		this.min = min;
	}

	public String getMax()
	{
		return max;
	}

	public void setMax(String max)
	{
		this.max = max;
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
