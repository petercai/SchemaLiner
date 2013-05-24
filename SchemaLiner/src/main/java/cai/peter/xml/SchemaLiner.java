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

	private File outputFile;

	private BufferedWriter bufferedWriter;

	/** provide a simple method to start the dump. */
	private void walkThroughElTree(final ElementDecl elementDecl) {
		walkThroughElTree("", elementDecl);
	}

	private void walkThroughElTree(final String xpath, final ElementDecl elementDecl){
		if (elementDecl == null) {
			return;
		}
		String elName = elementDecl.getName();
//		logger.info("walkThroughElTree() - String elName=" + elName);
		int maxOccurs = elementDecl.getMaxOccurs();
		int minOccurs = elementDecl.getMinOccurs();
//		List forcedXpaths = new ArrayList();
		XMLType typeReference = elementDecl.getType();
		String cardinality = ""; // Mandatory
		if( minOccurs==0 && maxOccurs ==1 ) cardinality = "?"; // optional
		if( minOccurs==0 && maxOccurs == -1 ) cardinality = "*";
		if(minOccurs==1 && maxOccurs == -1) cardinality = "+";
		String typeName = typeReference.getName();
//		logger.info("walkThroughElTree() - String typeName=" + typeName);
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
		logger.info("processComplexType() - String complexTypeName=" + complexTypeName);
//		newXpath.concat("/" + complexTypeName);
		/*
		 * TODO: handle xsd:extension first
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
			else if(baseType.isSimpleType() )
			{
				assert false;
			}
			else if( baseType.isAnyType() )
			{

				assert false;
			}
			else
			{
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
				 * container: sequence, choice or all
				 */
				Group container = (Group)o;
				String name = container.getName();
				if( name == null )
					name  = container.getOrder().name();
//						logger.info("processComplexType() - Group container=" + name);
				processGroup(newXpath, container);
			} else {
				System.out.println(" [dump] ***** Unknown particle type: " + o.getClass().getName());
			}
		}
	}

	/** I have no idea what a group is, but a little experimentation
	 * showed the follow method to work.
	 */
	public void processGroup(String xpath, final Group group)
	{
		Enumeration particles = group.enumerate();
		while (particles.hasMoreElements())
		{
			// TODO: caster cannot handle
			Object o = particles.nextElement();
			if (o instanceof Group)
			{
//				logger.info("processGroup() - Object o=" + ((Group)o).getName());
				processGroup(xpath, (Group) o);
			}
			else if (o instanceof ElementDecl)
			{
//				logger.info("processGroup() - Object o=" + ((ElementDecl)o).getName());
				walkThroughElTree(xpath, (ElementDecl) o);
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
		try {
			outputFile = new File(inputFile.toString()+".txt");
			bufferedWriter = new BufferedWriter(new FileWriter(outputFile));

			String fileURI = inputFile.toURI().toString();
			InputSource inputSource = new InputSource(fileURI);
			SchemaReader a = new SchemaReader(inputSource);
			Schema s = a.read();

			// we handle element only
			Collection<ElementDecl> elementDecls = s.getElementDecls();
			for( ElementDecl nextElement : elementDecls)
			{
				walkThroughElTree(nextElement);
			}

			bufferedWriter.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			System.out.println("Done.");
		}
	}

	void parseElements(File inputFile) {
		try {
			outputFile = new File(inputFile.toString()+".txt");
			bufferedWriter = new BufferedWriter(new FileWriter(outputFile));

			String fileURI = inputFile.toURI().toString();
			InputSource inputSource = new InputSource(fileURI);
			SchemaReader a = new SchemaReader(inputSource);
			Schema s = a.read();

			// we handle element only
			Collection<ElementDecl> elementDecls = s.getElementDecls();
			for(ElementDecl nextElement : elementDecls)
			{
				walkThroughElTree(nextElement);
			}

			bufferedWriter.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			System.out.println("Done.");
		}
	}

	void parseServiceRequestHeader(File inputFile) {
		try {
			outputFile = new File(inputFile.toString()+".txt");
			bufferedWriter = new BufferedWriter(new FileWriter(outputFile));

			String fileURI = inputFile.toURI().toString();
			InputSource inputSource = new InputSource(fileURI);
			SchemaReader a = new SchemaReader(inputSource);
			Schema s = a.read();

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
			outputFile = new File(inputFile.toString()+".txt");
			bufferedWriter = new BufferedWriter(new FileWriter(outputFile));

			String fileURI = inputFile.toURI().toString();
			InputSource inputSource = new InputSource(fileURI);
			SchemaReader a = new SchemaReader(inputSource);
			Schema s = a.read();

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
					String groupName = group.getName();
//					logger.info("parseComplexType() - String groupName=" + groupName);
					if( groupName == null )
						groupName = group.getOrder().name();
					processGroup("/ServiceRequestHeader", group);
				}
			}

			bufferedWriter.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			System.out.println("Done.");
		}
	}
}
