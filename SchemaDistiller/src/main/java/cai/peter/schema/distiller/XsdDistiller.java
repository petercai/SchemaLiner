/***********************************************
 * Copyright (c) 2013 Peter Cai                *
 * All rights reserved.                        *
 ***********************************************/
package cai.peter.schema.distiller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;

import org.exolab.castor.xml.schema.Annotated;
import org.exolab.castor.xml.schema.AttributeDecl;
import org.exolab.castor.xml.schema.ComplexType;
import org.exolab.castor.xml.schema.ElementDecl;
import org.exolab.castor.xml.schema.Facet;
import org.exolab.castor.xml.schema.Group;
import org.exolab.castor.xml.schema.Schema;
import org.exolab.castor.xml.schema.SimpleType;
import org.exolab.castor.xml.schema.Wildcard;
import org.exolab.castor.xml.schema.XMLType;

import cai.peter.aop.ObjectFactory;
import cai.peter.aop.ToStringMethodInterceptor;
import cai.peter.schema.model.xattribute;
import cai.peter.schema.model.xelement;
import cai.peter.schema.model.xgroup;
import cai.peter.schema.model.xnode;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.matcher.Matchers;

public class XsdDistiller
{

	public xelement processElement(final ElementDecl elementDecl, final String path) throws Exception
	{
		xelement node = new ObjectFactory(elementDecl.getName()).newInstance();
		node.setPath(path);
		node.setCardinality(elementDecl.getMinOccurs(),elementDecl.getMaxOccurs());
		XMLType typeReference = elementDecl.getType();
		if (typeReference.isComplexType())
		{
			processComplexType(	node,(ComplexType) typeReference);
		}
		else
		{
			String primitiveTypeName = getPrimitiveTypeName(typeReference);

			node.setType(primitiveTypeName);
			if( typeReference instanceof SimpleType)
			{
				populateFacets(node,(SimpleType)typeReference );
			}
		}
		return node;
	}

	void populateFacets(xelement el, SimpleType type)
	{
		Enumeration<Facet> facets = type.getFacets();
		while( facets.hasMoreElements())
		{
			Facet facet = facets.nextElement();

			switch( ( facet.getName()))
			{
			case "maxLength":
				el.setMax(facet.getValue());
				break;
			case "minLength":
				el.setMin(facet.getValue());
				break;
			case "totalDigits":
				el.setTotal(facet.getValue());
				break;
			case "fractionDigits":
				el.setFraction (facet.getValue());
				break;
			}
		}
	}

	public void processComplexType(xelement node, ComplexType complexType) throws Exception
	{
		/*
		 * handle xsd:extension first
		 */
		XMLType xsdExtension = complexType.getBaseType();
		if( xsdExtension != null )
		{
			/*
			 * process base type of current complex type
			 */
			if( xsdExtension.isComplexType() )
			{
				processComplexType(node, (ComplexType)xsdExtension);
			}
			else
			{
				throw new Exception("Unsupported feature/type: "+xsdExtension.getClass().getName());
			}
		}

		processTypeAttributes(node, complexType);

		/*
		 * particles are groups
		 */
		Enumeration<?> particles = complexType.enumerate();
		while (particles.hasMoreElements())
		{
			Object particle = particles.nextElement();
			if (particle instanceof Group)
			{
				processGroup(node,(Group) particle);
			}
//			else if( particle instanceof Wildcard)
//			{
//				logger.error("processComplexType() - Unexpected partical: Wildcard");
//			}
//			else if( particle instanceof ElementDecl)
//			{
//				logger.error("processComplexType() - Unexpected partical: ElementDecl.name="+((ElementDecl)particle).getName());
//			}
			else
			{
				throw new Exception("Unknown particle type: " + particle.getClass().getName());
			}
		}
	}

	private void processTypeAttributes(xelement node, ComplexType complexType)
	{
		Enumeration<?> attributes = complexType.getAttributeDecls();
		while (attributes.hasMoreElements())
		{
			AttributeDecl attributeDecl = (AttributeDecl) attributes
					.nextElement();
			xattribute attribute = new xattribute(attributeDecl.getName());
			node.addAttribute(attribute);
		}
	}

	private void processGroup(xnode parent, final Group group) throws Exception
	{
		xgroup newGroup = new xgroup(group.getOrder().name());
		newGroup.setPath(parent.getPath());
		parent.addItem(newGroup);

		Enumeration<Annotated> particles = group.enumerate();
		while (particles.hasMoreElements())
		{
			Object particle = particles.nextElement();
			if (particle instanceof Group)
			{
//				newGroup.addItem(
						processGroup(newGroup, (Group)particle);
//						);
			}
			else if (particle instanceof ElementDecl )
			{
				xnode element = processElement((ElementDecl)particle, parent.getPath());
//				parent.addItem(element);
				newGroup.addItem(element);
			}
			else if( particle instanceof Wildcard )
			{
				xnode node = processWildard((Wildcard) particle);
//				parent.addItem(node);
				newGroup.addItem(node);

			}
			else
			{
				throw new Exception("Unknown particle type: " + particle.getClass().getName());
			}
		}

//		return newGroup;
	}

	xnode processWildard(Wildcard content)
	{
		xelement result = new ObjectFactory(content.getProcessContent()).newInstance();
		result.setCardinality(content.getMinOccurs(),
		                      content.getMaxOccurs());
		return result;
	}


	public List<xnode> processElements(Schema schema) throws Exception
	{
		List<xnode> result = new ArrayList<xnode>();
		Collection<ElementDecl> elementDecls = schema.getElementDecls();
		for (ElementDecl e : elementDecls)
		{
			result.add(processElement(e,null));
		}
		return result;
	}


	public String getPrimitiveTypeName(XMLType xmlType)
	{
		String derivationMethod = xmlType.getDerivationMethod();
		XMLType baseType = xmlType.getBaseType();
		if( derivationMethod !=null && baseType != null )
			return getPrimitiveTypeName(baseType);
		else
			return xmlType.getName();
	}


}
