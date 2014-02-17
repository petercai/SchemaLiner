package cai.peter.schema;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.exolab.castor.xml.schema.ComplexType;
import org.exolab.castor.xml.schema.ElementDecl;
import org.exolab.castor.xml.schema.Schema;
import org.exolab.castor.xml.schema.XMLType;
import org.exolab.castor.xml.schema.reader.SchemaReader;
import org.xml.sax.InputSource;

public class CastorUtil
{

	public static String getPrimitiveTypeName(XMLType xmlType)
	{
		String derivationMethod = xmlType.getDerivationMethod();
		XMLType baseType = xmlType.getBaseType();
		if( derivationMethod !=null && baseType != null )
			return getPrimitiveTypeName(baseType);
		else
			return xmlType.getName();
	}

	public static Schema getSchema(File inputFile) throws IOException
	{
		String fileURI = inputFile.toURI().toString();
		InputSource inputSource = new InputSource(fileURI);
		SchemaReader a = new SchemaReader(inputSource);
		Schema s = a.read();
		return s;
	}

	public static List<String> getRootElements(Schema schema)
	{
		List<String> result = new ArrayList<String>();
		Collection<ElementDecl> elementDecls = schema.getElementDecls();
		for (ElementDecl xElement : elementDecls)
		{
			{
				result.add(xElement.getName());
			}
		}
		return result;
	}
	public static List<String> getComplexTypes(Schema schema)
	{
		List<String> result = new ArrayList<String>();
		Collection<ComplexType> complexTypes = schema.getComplexTypes();
		for (ComplexType complexType : complexTypes)
		{
			{
				result.add(complexType.getName());
			}
		}
		return result;
	}
}
