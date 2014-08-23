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

import cai.peter.schema.model.xattribute;
import cai.peter.schema.model.xelement;
import cai.peter.schema.model.xgroup;

public class XsdDistiller
{

	public xelement processElement(final ElementDecl elementDecl, final String path) throws Exception
	{
		xelement node = new xelement(elementDecl.getName());
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
				el.rangeTo = facet.getValue();
				break;
			case "minLength":
				el.rangeFrom = facet.getValue();
				break;
			case "totalDigits":
				el.rangeFrom = facet.getValue();
				break;
			case "fractionDigits":
				el.rangeTo = facet.getValue();
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

	private xgroup processGroup(xelement container, final Group group) throws Exception
	{
		xgroup result = new xgroup(group.getOrder().name());
		container.addGroup(result);

		Enumeration<Annotated> particles = group.enumerate();
		while (particles.hasMoreElements())
		{
			Object particle = particles.nextElement();
			if (particle instanceof Group)
			{
				result.addGroup(processGroup(container, (Group)particle));
			}
			else if (particle instanceof ElementDecl )
			{
				xelement element = processElement((ElementDecl)particle, container.getPath());
				container.addChild(element);
				result.addNode(element);
			}
			else if( particle instanceof Wildcard )
			{
				xelement node = processWildard((Wildcard) particle);
				container.addChild(node);
				result.addNode(node);

			}
			else
			{
				throw new Exception("Unknown particle type: " + particle.getClass().getName());
			}
		}

		return result;
	}

	xelement processWildard(Wildcard content)
	{
		xelement result = new xelement(content.getProcessContent());
		result.setCardinality(content.getMinOccurs(),
		                      content.getMaxOccurs());
		return result;
	}


	public List<xelement> processElements(Schema schema) throws Exception
	{
		List<xelement> result = new ArrayList<xelement>();
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
