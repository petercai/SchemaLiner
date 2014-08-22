package cai.peter.schema;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.exolab.castor.xml.schema.ComplexType;
import org.exolab.castor.xml.schema.ElementDecl;
import org.exolab.castor.xml.schema.Schema;
import org.exolab.castor.xml.schema.reader.SchemaReader;
import org.xml.sax.InputSource;

public class CastorUtil
{

	public static Schema getSchema(File inputFile) throws IOException
	{
		String fileURI = inputFile.toURI().toString();
		InputSource inputSource = new InputSource(fileURI);
		SchemaReader a = new SchemaReader(inputSource);
		Schema s = a.read();
		return s;
	}
}
