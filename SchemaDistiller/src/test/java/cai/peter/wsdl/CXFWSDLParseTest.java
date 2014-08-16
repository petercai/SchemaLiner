package cai.peter.wsdl;

import java.net.URL;
import java.util.List;

import javax.wsdl.Definition;
import javax.wsdl.Message;
import javax.wsdl.WSDLException;
import javax.xml.namespace.QName;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.service.model.SchemaInfo;
import org.apache.cxf.service.model.ServiceInfo;
import org.apache.cxf.wsdl.WSDLManager;
import org.apache.cxf.wsdl11.WSDLServiceBuilder;
import org.apache.ws.commons.schema.XmlSchema;
import org.junit.Test;

public class CXFWSDLParseTest {

	@Test
	public void test() throws WSDLException 
	{
		URL wsdlUrl = this.getClass().getClassLoader().getResource("ebay/PayPalSvc.wsdl");

		Bus bus = BusFactory.getDefaultBus();
		WSDLManager wsdlManager = bus.getExtension(WSDLManager.class);
		Definition defs = wsdlManager.getDefinition(wsdlUrl);
		WSDLServiceBuilder wsdlServiceBuilder = new WSDLServiceBuilder(bus);
		List<ServiceInfo> serviceInfos = wsdlServiceBuilder.buildServices(defs);
		ServiceInfo serviceInfo = serviceInfos.get(0);
		List<SchemaInfo> schemas = serviceInfo.getSchemas();
		SchemaInfo schemaInfo = schemas.get(0);
		XmlSchema schema = schemaInfo.getSchema();


		out("-------------- WSDL Details --------------");
		out("TargenNamespace: \t" + defs.getTargetNamespace());
//		if (defs.getDocumentation() != null) {
//			out("Documentation: \t\t" + defs.getDocumentation());
//		}
		out("\n");

		/* For detailed schema information see the FullSchemaParser.java sample.*/
//		out("Schemas: ");
//		for (Schema schema : defs.getSchemas()) {
//			out("  TargetNamespace: \t" + schema.getTargetNamespace());
//		}
//		out("\n");
		
		out("Messages: ");
//		Map messages = defs.getMessages();
		for (Object key : defs.getMessages().keySet())
		{
			QName qName = (QName)key;
			String localPart = qName.getLocalPart();
			Message msg = defs.getMessage(qName);
			out("");
			out("  Message Name: " + localPart);
			out("  Message Parts: ");
			for (Part part : msg.getParts()) {
				out("    Part Name: " + part.getName());
				out("    Part Element: " + ((part.getElement() != null) ? part.getElement() : "not available!"));
				out("    Part Type: " + ((part.getType() != null) ? part.getType() : "not available!" ));
				out("");
			}
		}
		out("");

		out("PortTypes: ");
//		for (PortType pt : defs.getPortTypes()) {
//			out("  PortType Name: " + pt.getName());
//			out("  PortType Operations: ");
//			for (Operation op : pt.getOperations()) {
//				out("    Operation Name: " + op.getName());
//				out("    Operation Input Name: "
//				    + ((op.getInput().getName() != null) ? op.getInput().getName() : "not available!"));
////				out("    Operation Input Message: "
////				    + op.getInput().getMessage().getQname());
//				out("    Operation Output Name: "
//				    + ((op.getOutput().getName() != null) ? op.getOutput().getName() : "not available!"));
////				out("    Operation Output Message: "
////				    + op.getOutput().getMessage().getQname());
//				out("    Operation Faults: ");
//				if (op.getFaults().size() > 0) {
//					for (Fault fault : op.getFaults()) {
//						out("      Fault Name: " + fault.getName());
////						out("      Fault Message: " + fault.getMessage().getQname());
//					}
//				} else out("      There are no faults available!");
//				
//			}
//			out("");
//		}
		out("");

		out("Bindings: ");
//		for (Binding bnd : defs.getBindings()) {
//			out("  Binding Name: " + bnd.getName());
//			out("  Binding Type: " + bnd.getPortType().getName());
//			out("  Binding Protocol: " + bnd.getBinding().getProtocol());
//			out("  Binding Style: " + bnd.getStyle());
//			out("  Binding Operations: ");
//			for (BindingOperation bop : bnd.getOperations()) {
//				out("    Operation Name: " + bop.getName());
//				if(bnd.getBinding() instanceof AbstractSOAPBinding) {
//					out("    Operation SoapAction: " + bop.getOperation().getSoapAction());
//					out("    SOAP Body Use: " + bop.getInput().getBindingElements().get(0).getUse());
//				}
//			}
//			out("");
//		}
		out("");

		out("Services: ");
//		for (Service service : defs.getServices()) {
//			out("  Service Name: " + service.getName());
//			out("  Service Potrs: ");
//			for (Port port : service.getPorts()) {
//				out("    Port Name: " + port.getName());
//				out("    Port Binding: " + port.getBinding().getName());
//				out("    Port Address Location: " + port.getAddress().getLocation()
//				    + "\n");
//			}
//		}
		out("");
	}

	private static void out(String str) {
		System.out.println(str);
	}
}
