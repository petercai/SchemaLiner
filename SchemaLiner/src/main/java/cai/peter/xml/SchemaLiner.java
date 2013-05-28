package cai.peter.xml;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Stack;

import org.apache.log4j.Logger;
import org.exolab.castor.xml.schema.AttributeDecl;
import org.exolab.castor.xml.schema.ComplexType;
import org.exolab.castor.xml.schema.ElementDecl;
import org.exolab.castor.xml.schema.Group;
import org.exolab.castor.xml.schema.Schema;
import org.exolab.castor.xml.schema.SimpleType;
import org.exolab.castor.xml.schema.XMLType;
import org.exolab.castor.xml.schema.reader.SchemaReader;
import org.xml.sax.InputSource;

public class SchemaLiner {
	/**
	 * Logger for this class
	 */
	private static final Logger logger = Logger
			.getLogger(SchemaLiner.class);

	private static Stack visitedTypes = new Stack();


	private BufferedWriter bufferedWriter;

	/** provide a simple method to start the dump. */
	private void processElTree(final ElementDecl elementDecl) {
		processElTree("", elementDecl);
	}

	private void processElTree(final String xpath, final ElementDecl elementDecl){
		if (elementDecl == null) {
			return;
		}
		String elName = elementDecl.getName();
		int maxOccurs = elementDecl.getMaxOccurs();
		int minOccurs = elementDecl.getMinOccurs();
		XMLType typeReference = elementDecl.getType();
		String cardinality = ""; // Mandatory
		if( minOccurs==0 && maxOccurs ==1 ) cardinality = "?"; // optional
		if( minOccurs==0 && maxOccurs == -1 ) cardinality = "*";
		if(minOccurs==1 && maxOccurs == -1) cardinality = "+";
		String typeName = typeReference.getName();
		if (typeName != null && visitedTypes.contains(typeName)) {
			// The type is already in the stack, therefore if we were to continue we would infinitely recurse.
		} else {
			if (typeName != null) {
				visitedTypes.push(typeName);
			}


			String newXpath = xpath + "/" + elName ;

			System.out.println(newXpath+cardinality);
			writeToFile(newXpath+cardinality);

			if (typeReference.isComplexType()) {
				processComplexType(newXpath, (ComplexType)typeReference);
			}
		}

		if (typeName != null && !visitedTypes.empty()) {
			visitedTypes.pop();
		}
	}

	private void processComplexType(String newXpath, ComplexType complexType)
	{
		String complexTypeName = complexType.getName();

		/*
		 * handle xsd:extension first
		 */
//		String derivationMethod = complexType.getDerivationMethod();
//		if( "extension".equals(derivationMethod))
		XMLType baseType = complexType.getBaseType();
		if( baseType != null )
		{
			/*
			 * process base type of current complex type
			 */
			if( baseType.isComplexType() )
			{
				processComplexType(newXpath, (ComplexType)baseType);
			}
			else
			{
				/*
				 * Impossible to reach here
				 */
				assert false;
			}
		}
		/*
		 * then handle attributes
		 */
		Enumeration attributes = complexType.getAttributeDecls();
		while (attributes.hasMoreElements()) {
			AttributeDecl attributeDecl = (AttributeDecl)attributes.nextElement();
			System.out.println(newXpath + "/@" + attributeDecl.getName());
			writeToFile(newXpath + "/@" + attributeDecl.getName());
		}

		/*
		 * then handle children
		 */
		Enumeration particles = complexType.enumerate();
		while (particles.hasMoreElements()) {
			Object o = particles.nextElement();
			if (o instanceof Group) {
				/*
				 * container: sequence, choice or all. But
				 * I cannot handle "all".
				 */
				Group container = (Group)o;
				processGroup(newXpath, container);
			} else {
				System.out.println(" [dump] ***** Unknown particle type: " + o.getClass().getName());
			}
		}
	}

	/**
	 * @param xpath
	 * @param group - schema abstract group. could be group, element, sequence, choice or all
	 */
	public void processGroup(String xpath, final Group group)
	{
		Enumeration particles = group.enumerate();
		while (particles.hasMoreElements())
		{
			Object o = particles.nextElement();
			if (o instanceof Group)
			{
				processGroup(xpath, (Group) o);
			}
			else if (o instanceof ElementDecl)
			{
				processElTree(xpath, (ElementDecl) o);
			}
			else
			{
				System.out.println("[dumpGroup] ***** Unknown particle type: " + o.getClass()
																					.getName());
			}
		}
	}

	public static void main(String[] args) {
		File file = new File(args[0]);
		new SchemaLiner().parseSchemaFile(file);
	}

	void writeToFile(String line)
	{
		try {
			bufferedWriter.append(line);
			bufferedWriter.append(System.getProperty("line.separator"));

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	void parseSchemaFile(File inputFile) {
		parseElements(inputFile);
	}

	private void getWriter(File inputFile) throws IOException
	{
		File outputFile = new File(inputFile.toString()+".txt");
		bufferedWriter = new BufferedWriter(new FileWriter(outputFile));
	}

	void parseElements(File inputFile) {
		try {
			getWriter(inputFile);

			Schema s = getSchema(inputFile);

			// we handle element only
			Collection<ElementDecl> elementDecls = s.getElementDecls();
			for(ElementDecl nextElement : elementDecls)
			{
				processElTree(nextElement);
			}

			bufferedWriter.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
		}
	}

	void parseServiceRequestHeader(File inputFile) {
		try {
			getWriter(inputFile);

			Schema s = getSchema(inputFile);

			// we handle element only
			ComplexType complexType = s.getComplexType("ServiceRequestHeader");
			processComplexType("/ServiceRequestHeader", complexType);

			bufferedWriter.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			System.out.println("Done.");
		}
	}

	void parseServiceHeader(File inputFile) {
		try {
			getWriter(inputFile);

			Schema s = getSchema(inputFile);

			// we handle element only
			ComplexType complexType = s.getComplexType("ServiceHeader");
			// sequence can be handled properly
			Enumeration particles = complexType.enumerate();
			while( particles.hasMoreElements())
			{
				Object o = particles.nextElement();
				if( o instanceof Group )
				{
					Group group = (Group)o;
					processGroup("/ServiceRequestHeader", group);
				}
			}

			bufferedWriter.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
		}
	}

	private Schema getSchema(File inputFile) throws IOException
	{
		String fileURI = inputFile.toURI().toString();
		InputSource inputSource = new InputSource(fileURI);
		SchemaReader a = new SchemaReader(inputSource);
		Schema s = a.read();
		return s;
	}
}
