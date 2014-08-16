package cai.peter.wsdl;

import java.io.InputStream;

import org.junit.Test;

import com.predic8.schema.Schema;
import com.predic8.wsdl.AbstractSOAPBinding;
import com.predic8.wsdl.Binding;
import com.predic8.wsdl.BindingOperation;
import com.predic8.wsdl.Definitions;
import com.predic8.wsdl.Fault;
import com.predic8.wsdl.Message;
import com.predic8.wsdl.Operation;
import com.predic8.wsdl.Part;
import com.predic8.wsdl.Port;
import com.predic8.wsdl.PortType;
import com.predic8.wsdl.Service;
import com.predic8.wsdl.WSDLParser;

public class WSDLParseTest {

	@Test
	public void test() 
	{
		WSDLParser parser = new WSDLParser();
//		InputStream stream = ClassLoader.getSystemResourceAsStream("wsdl/BLZService.wsdl");
//		Definitions defs = parser.parse("http://wsf.cdyne.com/WeatherWS/Weather.asmx?WSDL");
//		Definitions defs = parser.parse("http://queue.amazonaws.com/doc/2012-11-05/QueueService.wsdl");
//		Definitions defs = parser.parse("https://www.paypalobjects.com/wsdl/PayPalSvc.wsdl");
//		InputStream stream = ClassLoader.getSystemResourceAsStream("ebay/PayPalSvc.wsdl");
		InputStream stream = this.getClass().getClassLoader().getResourceAsStream("ebay/PayPalSvc.wsdl");
		Definitions defs = parser.parse(stream);

		out("-------------- WSDL Details --------------");
		out("TargenNamespace: \t" + defs.getTargetNamespace());
		if (defs.getDocumentation() != null) {
			out("Documentation: \t\t" + defs.getDocumentation());
		}
		out("\n");

		/* For detailed schema information see the FullSchemaParser.java sample.*/
		out("Schemas: ");
		for (Schema schema : defs.getSchemas()) {
			out("  TargetNamespace: \t" + schema.getTargetNamespace());
		}
		out("\n");
		
		out("Messages: ");
		for (Message msg : defs.getMessages()) {
			out("  Message Name: " + msg.getName());
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
		for (PortType pt : defs.getPortTypes()) {
			out("  PortType Name: " + pt.getName());
			out("  PortType Operations: ");
			for (Operation op : pt.getOperations()) {
				out("    Operation Name: " + op.getName());
				out("    Operation Input Name: "
				    + ((op.getInput().getName() != null) ? op.getInput().getName() : "not available!"));
//				out("    Operation Input Message: "
//				    + op.getInput().getMessage().getQname());
				out("    Operation Output Name: "
				    + ((op.getOutput().getName() != null) ? op.getOutput().getName() : "not available!"));
//				out("    Operation Output Message: "
//				    + op.getOutput().getMessage().getQname());
				out("    Operation Faults: ");
				if (op.getFaults().size() > 0) {
					for (Fault fault : op.getFaults()) {
						out("      Fault Name: " + fault.getName());
//						out("      Fault Message: " + fault.getMessage().getQname());
					}
				} else out("      There are no faults available!");
				
			}
			out("");
		}
		out("");

		out("Bindings: ");
		for (Binding bnd : defs.getBindings()) {
			out("  Binding Name: " + bnd.getName());
			out("  Binding Type: " + bnd.getPortType().getName());
			out("  Binding Protocol: " + bnd.getBinding().getProtocol());
			out("  Binding Style: " + bnd.getStyle());
			out("  Binding Operations: ");
			for (BindingOperation bop : bnd.getOperations()) {
				out("    Operation Name: " + bop.getName());
				if(bnd.getBinding() instanceof AbstractSOAPBinding) {
					out("    Operation SoapAction: " + bop.getOperation().getSoapAction());
					out("    SOAP Body Use: " + bop.getInput().getBindingElements().get(0).getUse());
				}
			}
			out("");
		}
		out("");

		out("Services: ");
		for (Service service : defs.getServices()) {
			out("  Service Name: " + service.getName());
			out("  Service Potrs: ");
			for (Port port : service.getPorts()) {
				out("    Port Name: " + port.getName());
				out("    Port Binding: " + port.getBinding().getName());
				out("    Port Address Location: " + port.getAddress().getLocation()
				    + "\n");
			}
		}
		out("");
	}

	private static void out(String str) {
		System.out.println(str);
	}
}
