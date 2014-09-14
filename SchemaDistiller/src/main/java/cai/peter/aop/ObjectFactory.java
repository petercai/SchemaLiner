/***********************************************
 * Copyright (c) 2014 Peter Cai                *
 * All rights reserved.                        *
 ***********************************************/
package cai.peter.aop;

import cai.peter.schema.model.xelement;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.matcher.Matchers;

public class ObjectFactory
{
	protected Injector	injector;

	protected String	path;

	protected String	name;

	public ObjectFactory()
	{
		injector = Guice.createInjector(new AbstractModule()
		{
			@Override
			protected void configure()
			{
				try
				{
					bindInterceptor( Matchers.only(xelement.class),
							Matchers.only(xelement.class.getDeclaredMethod("toString", null)), 
							new ToStringMethodInterceptor());
				}
				catch (NoSuchMethodException e)
				{
					e.printStackTrace();
				}
				catch (SecurityException e)
				{
					e.printStackTrace();
				}
			}
		});
	}

	public ObjectFactory(String path, String name)
	{
		this();
		this.path = path;
		this.name = name;
	}

	public ObjectFactory(String name)
	{
		this();
		this.name = name;
	}

	public xelement newInstance()
	{
		xelement e = injector.getInstance(xelement.class);
		e.setPath(path);
		e.setName(name);
		return e;
	}
	
	
}
