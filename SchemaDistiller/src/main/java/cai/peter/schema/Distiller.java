package cai.peter.schema;

import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

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

public class Distiller {
	/**
	 * Logger for this class
	 */
	private static final Logger	logger	= Logger.getLogger(Distiller.class);


	/**
	 * @param parent
	 * @param elementDecl
	 * @return current XNode
	 * @throws Exception
	 */
	public XNode processElement(XNode parent, final ElementDecl elementDecl) throws Exception
	{
		assert parent != null;
		assert elementDecl != null;
		XNode result = new XNode(elementDecl.getName());
		result.setCardinality(elementDecl.getMinOccurs(),
							elementDecl.getMaxOccurs());
		XMLType typeReference = elementDecl.getType();
		if (typeReference.isComplexType())
		{
			result.setParent(parent);
//			System.out.println(result.toString());
			processComplexType(	result,
								(ComplexType) typeReference);
		}
		else
		{
			String primitiveTypeName = CastorUtil.getPrimitiveTypeName(typeReference);

			// object copy
			XElement el = new XElement(	result,
										primitiveTypeName);
			el.setParent(parent);
			if( typeReference instanceof SimpleType)
			{
				processFacets(el,(SimpleType)typeReference );
			}

			// discard old node
			result = el;
//			System.out.println(el.toString());
		}

		return result;
	}

	void processFacets(XElement el, SimpleType type)
	{
		Enumeration<Facet> facets = type.getFacets();
		while( facets.hasMoreElements())
		{
			Facet facet = facets.nextElement();

			switch( ( facet.getName()))
			{
			case "maxLength":
				el.range[1] = facet.getValue();
				break;
			case "minLength":
				el.range[0] = facet.getValue();
				break;
			case "totalDigits":
				el.range[0] = facet.getValue();
				break;
			case "fractionDigits":
				el.range[1] = facet.getValue();
				break;
			}

		}

	}

	public void processComplexType(XNode parent, ComplexType complexType) throws Exception
	{
		/*
		 * handle xsd:extension first
		 */
		XMLType baseType = complexType.getBaseType();
		if( baseType != null )
		{
			/*
			 * process base type of current complex type
			 */
			if( baseType.isComplexType() )
			{
				processComplexType(parent, (ComplexType)baseType);
			}
			else
			{
				throw new Exception("Unsupported feature/type: "+baseType.getClass().getName());
			}
		}

		processTypeAttributes(parent, complexType);

		/*
		 * process children
		 */
		Enumeration<?> particles = complexType.enumerate();
		while (particles.hasMoreElements())
		{
			Object o = particles.nextElement();
			if (o instanceof Group)
			{
				Group container = (Group) o;
				processGroup(	parent,
								container);

			}
			else
			{
				throw new Exception("Unknown particle type: " + o.getClass().getName());
			}
		}
	}

	private void processTypeAttributes(XNode node, ComplexType complexType)
	{
		Enumeration<?> attributes = complexType.getAttributeDecls();
		while (attributes.hasMoreElements()) {
			AttributeDecl attributeDecl = (AttributeDecl)attributes.nextElement();
			XAttribute attribute = new XAttribute(attributeDecl.getName());
			node.addAttribute(attribute);
//			System.out.println(node.getPath() + "/@" + attribute.name);
		}
	}

	/**
	 * @param container
	 * @param group - schema abstract group. could be group, element, sequence, choice or all
	 * @throws Exception
	 */
	private XGroup processGroup(XNode container, final Group group) throws Exception
	{
		XGroup result = new XGroup(group.getOrder().name());
		result.setPath(container.getPath());
		container.groups.add(result);

		Enumeration<Annotated> particles = group.enumerate();
		while (particles.hasMoreElements())
		{
			Object o = particles.nextElement();
			if (o instanceof Group)
			{
				result.groups.add(processGroup(container, (Group) o));
			}
			else if (o instanceof ElementDecl )
			{
				XNode element = processElement(container, (ElementDecl) o);
				result.items.add(element.name);
			}
			else if( o instanceof Wildcard )
			{
				XNode node = processWildard(container, (Wildcard) o);
				result.items.add(node.name);

			}
			else
			{
				throw new Exception("Unknown particle type: " + o.getClass().getName());
			}
		}

		return result;
	}

	XNode processWildard( XNode parent, Wildcard content)
	{
		assert parent != null;
		assert content != null;
		XNode result = new XNode(content.getProcessContent());
		result.setCardinality(content.getMinOccurs(),
		                      content.getMaxOccurs());
		result.setParent(parent);

		return result;
	}

	private Map<String, XNode> elements = new HashMap<String, XNode>();
	private Map<String, XNode> complexTypes = new HashMap<String, XNode>();

	public XNode processAllElement(Schema schema) throws Exception
	{
		XNode root = new XNode("");
		Collection<ElementDecl> elementDecls = schema.getElementDecls();
		for (ElementDecl xElement : elementDecls)
		{
			String elementName = xElement.getName();
			logger.info("element: "+elementName);
			elements.put(elementName, processElement(root, xElement));
		}

		return root;
	}
	public XNode processAllComplexTypes(Schema schema) throws Exception
	{
		XNode root = new XNode("");
		Collection<ComplexType> types = schema.getComplexTypes();
		for (ComplexType xElement : types)
		{
			String elementName = xElement.getName();
			logger.info("ComplexType: "+elementName);
//			elements.put(elementName, processElement(root, xElement));
		}
		
		return root;
	}

	public Map<String, XNode> getElements()
	{
		return elements;
	}
	public XNode getElement(String name)
	{
		return elements.get(name);
	}
}
