package cai.peter.xml;

import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Stack;

import org.apache.log4j.Logger;
import org.exolab.castor.xml.schema.AttributeDecl;
import org.exolab.castor.xml.schema.ComplexType;
import org.exolab.castor.xml.schema.ElementDecl;
import org.exolab.castor.xml.schema.Group;
import org.exolab.castor.xml.schema.Schema;
import org.exolab.castor.xml.schema.XMLType;
import org.exolab.castor.xml.schema.reader.SchemaReader;
import org.xml.sax.InputSource;

public class SchemaLiner {
	/**
	 * Logger for this class
	 */
	private static final Logger logger = Logger
			.getLogger(XpathFromSchema.class);

	private static Stack visitedTypes = new Stack();

	/** provide a simple method to start the dump. */
	private void walkThroughElTree(final ElementDecl elementDecl) {
		walkThroughElTree("", elementDecl);
	}

	/** there may be some situations where the starting xpath is actually
	 * a prefix because the starting node can be different from the root node.
	 * In either case, allowing an xpath as a parameter provides flexibility
	 * for unforeseen needs.
	 */
	private void walkThroughElTree(final String xpath, final ElementDecl elementDecl) {
		if (elementDecl == null) {
			return;
		}
		List forcedXpaths = new ArrayList();
		XMLType typeReference = elementDecl.getType();

		if (typeReference.getName() != null && visitedTypes.contains(typeReference.getName())) {
			// The type is already in the stack, therefore if we were to continue we would infinitely recurse.
		} else {
			if (typeReference.getName() != null) {
				visitedTypes.push(typeReference.getName());
			}

			String newXpath = xpath + "/" + elementDecl.getName();

			System.out.println(newXpath);

			if (typeReference.isComplexType()) {
				ComplexType ct = (ComplexType)typeReference;
				Enumeration attributes = ct.getAttributeDecls();
				while (attributes.hasMoreElements()) {
					AttributeDecl attributeDecl = (AttributeDecl)attributes.nextElement();
					System.out.println(newXpath + "/@" + attributeDecl.getName());
				}
				Enumeration particles = ct.enumerate();
				while (particles.hasMoreElements()) {
					Object o = particles.nextElement();
					if (o instanceof Group) {
						processGroup(newXpath, (Group)o);
					} else {
						System.out.println(" [dump] ***** Unknown particle type: " + o.getClass().getName());
					}
				}
			}
		}

		if (typeReference.getName() != null && !visitedTypes.empty()) {
			visitedTypes.pop();
		}
	}

	/** I have no idea what a group is, but a little experimentation
	 * showed the follow method to work.
	 */
	public void processGroup(String xpath, final Group group) {
		Enumeration particles = group.enumerate();
		while (particles.hasMoreElements()) {
			Object o = particles.nextElement();
			if (o instanceof Group) {
				processGroup(xpath, (Group)o);
			} else if (o instanceof ElementDecl) {
				walkThroughElTree(xpath, (ElementDecl)o);
			} else {
				System.out.println("[dumpGroup] ***** Unknown particle type: " + o.getClass().getName());
			}
		}
	}

	public static void main(String[] args) {
		File file = new File(args[0]);
		new SchemaLiner().parseSchemaFile(file);
	}

	void parseSchemaFile(File file) {
		try {
			String systemId = file.toURI().toString();
			InputSource inputSource = new InputSource(systemId);
			SchemaReader a = new SchemaReader(inputSource);
			Schema s = a.read();

			Enumeration elementDecls = s.getElementDecls();
			while( elementDecls.hasMoreElements())
			{
				ElementDecl nextElement = (ElementDecl)elementDecls.nextElement();
//				logger.info("main(String[]) - Object nextElement="
//						+ nextElement);
				walkThroughElTree(nextElement);
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			System.out.println("Done.");
		}
	}
}
