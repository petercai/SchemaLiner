/***********************************************
 * Copyright (c) 2014 Peter Cai                *
 * All rights reserved.                        *
 ***********************************************/
package cai.peter.aop;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import cai.peter.schema.model.xelement;
import cai.peter.sd.XdOption;

public class ToStringMethodInterceptor implements MethodInterceptor
{

	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable
	{
		String result = "";
		xelement e = (xelement)invocation.getThis();
		XdOption option = XdOption.getInstance();
		switch(option.opt)
		{
		case SHOW_CARDINALITY:
			result = e.toCardinlityInfo();
			break;
		case SHOW_TYPEINFO:
			result = e.toTypeInfo();
			break;
		default:
			result = e.toPathInfo();
			break;
		}
		
		return result;
	}
}
