package cai.peter.schema;

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
			System.out.println(result.toString());
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
			System.out.println(el.toString());
		}

		return result;
	}

	void processFacets(XElement el, SimpleType type)
	{
		Enumeration<Facet> facets = type.getFacets();
		while( facets.hasMoreElements())
		{
			Facet facet = facets.nextElement();
			HashMap<String, Integer> option = new HashMap<String, Integer>(4){{
				put("maxLength",1);
				put("minLength",2);
				put("totalDigits",3);
				put("fractionDigits",4);
			}};
			switch( option.get( facet.getName()))
			{
			case 1:
				el.range[1] = facet.getValue();
				break;
			case 2:
				el.range[0] = facet.getValue();
				break;
			case 3:
				el.range[0] = facet.getValue();
				break;
			case 4:
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
		/*
		 * process attributes
		 */
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
//				XGroup xg = new XGroup(container.getOrder().name());
//				// add group to element
//				parent.groups.add(xg);
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
			System.out.println(node.getPath() + "/@" + attribute.name);
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

	public Map<String, XNode> elements = new HashMap<String, XNode>();

	public XNode processAll(Schema schema) throws Exception
	{
		XNode root = new XNode("");
		Collection<ElementDecl> elementDecls = schema.getElementDecls();
		for (ElementDecl xElement : elementDecls)
		{
			elements.put(xElement.getName(), processElement(root, xElement));
		}

		return root;
	}
}
