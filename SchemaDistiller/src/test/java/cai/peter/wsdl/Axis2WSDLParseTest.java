package cai.peter.wsdl;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.wsdl.Binding;
import javax.wsdl.BindingOperation;
import javax.wsdl.Definition;
import javax.wsdl.Fault;
import javax.wsdl.Input;
import javax.wsdl.Message;
import javax.wsdl.Operation;
import javax.wsdl.Output;
import javax.wsdl.Part;
import javax.wsdl.Port;
import javax.wsdl.PortType;
import javax.wsdl.Service;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.AttributeExtensible;
import javax.wsdl.extensions.ExtensionRegistry;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.WSDL11ToAllAxisServicesBuilder;
import org.apache.axis2.description.WSDL11ToAxisServiceBuilder;
import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.service.model.SchemaInfo;
import org.apache.cxf.service.model.ServiceInfo;
import org.apache.cxf.wsdl.WSDLManager;
import org.apache.cxf.wsdl11.WSDLServiceBuilder;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.junit.Test;

public class Axis2WSDLParseTest {

    public Definition readInTheWSDLFile(final String uri) throws WSDLException {

        WSDLReader reader = WSDLFactory.newInstance().newWSDLReader();
        reader.setFeature("javax.wsdl.importDocuments", true);

        ExtensionRegistry extReg = WSDLFactory.newInstance().newPopulatedExtensionRegistry();
        extReg.registerExtensionAttributeType(Input.class,
                new QName(AddressingConstants.Final.WSAW_NAMESPACE, AddressingConstants.WSA_ACTION),
                AttributeExtensible.STRING_TYPE);
        extReg.registerExtensionAttributeType(Output.class,
                new QName(AddressingConstants.Final.WSAW_NAMESPACE, AddressingConstants.WSA_ACTION),
                AttributeExtensible.STRING_TYPE);
        reader.setExtensionRegistry(extReg);

        return reader.readWSDL(uri);
        
    }

	
	@Test
	public void test() throws WSDLException, AxisFault
	{
		URL wsdlUrl = this.getClass().getClassLoader().getResource("at_wsdl/wsdl/AccountTransferHTTP.wsdl");
//		URL wsdlUrl = this.getClass().getClassLoader().getResource("at_wsdl/wsdl/AccountTransferFull.wsdl");

		Definition defs = readInTheWSDLFile(wsdlUrl.toString());


		out("-------------- WSDL Details --------------");
		out("TargenNamespace: \t" + defs.getTargetNamespace());
//		if (defs.getDocumentation() != null) {
//			out("Documentation: \t\t" + defs.getDocumentation());
//		}
		out("\n");

		/* For detailed schema information see the FullSchemaParser.java sample.*/
		out("Schemas: ");
//		WSDLServiceBuilder wsdlServiceBuilder = new WSDLServiceBuilder(bus);
//		List<ServiceInfo> serviceInfos = wsdlServiceBuilder.buildServices(defs);
//		for( ServiceInfo serviceInfo : serviceInfos)
//		{
//			List<SchemaInfo> schemas = serviceInfo.getSchemas();
//			for( SchemaInfo schemaInfo : schemas )
//			{
//				XmlSchema schema = schemaInfo.getSchema();
//				out("  TargetNamespace: \t" + schema.getTargetNamespace());
//			}
//
//		}
		WSDL11ToAllAxisServicesBuilder builder;
        builder = new WSDL11ToAllAxisServicesBuilder(defs);
        List<AxisService> allServices = builder.populateAllServices();
        for( AxisService service : allServices)
        {
        	ArrayList<XmlSchema> schemas = service.getSchema();
        	for( XmlSchema schema : schemas )
        	{
				out("  TargetNamespace: \t" + schema.getTargetNamespace());
				Map<QName, XmlSchemaElement> elements = schema.getElements();
				for(Map.Entry<QName, XmlSchemaElement> entry: elements.entrySet())
				{
					QName key = entry.getKey();
					out("  \tXmlSchemaElement.QName: \t" + key.toString());
					XmlSchemaElement value = entry.getValue();
				}

        	}
        }
		out("\n");

		out("Services: ");
		for (Service service : (Collection<Service>) defs.getServices().<Service>values()) {
			out("  Service Name: " + service.getQName().getLocalPart());
			out("  Service Potrs: ");
			for (Port port : (Collection<Port>) service.getPorts().<Port>values()) {
				out("    Port Name: " + port.getName());
				out("    Port Binding: " + port.getBinding().getQName().getLocalPart());
//				out("    Port Address Location: " + port.getAddress().getLocation()
//				    + "\n");
			}
		}
		out("");
		out("Bindings: ");
		Collection<Binding> values = defs.getBindings().<Binding>values();
		for (Binding bnd : values) {
			out("  Binding Name: " + bnd.getQName().getLocalPart());
			out("  Binding Type: " + bnd.getPortType().getQName().getLocalPart());
//			out("  Binding Protocol: " + bnd.getBinding().getProtocol());
//			out("  Binding Style: " + bnd.getStyle());
			out("  Binding Operations: ");
			List<BindingOperation> bindingOperations = bnd.<BindingOperation>getBindingOperations();
			for (BindingOperation bop : bindingOperations) {
				out("    Operation Name: " + bop.getName());
//				if(bnd.getBinding() instanceof AbstractSOAPBinding) {
//					out("    Operation SoapAction: " + bop.getOperation().getSoapAction());
//					out("    SOAP Body Use: " + bop.getInput().getBindingElements().get(0).getUse());
//				}
			}
			out("");
		}
		out("");


		out("PortTypes: ");
		Collection<PortType> pts = defs.getPortTypes().<PortType>values();
		for (PortType pt : pts) {
			out("  PortType Name: " + pt.getQName().getLocalPart());
			out("  PortType Operations: ");
			List<Operation> operations = pt.<Operation>getOperations();
			for (Operation op : operations) {
				out("    Operation Name: " + op.getName());
				out("    Operation Input Name: "
				    + ((op.getInput().getName() != null) ? op.getInput().getName() : "not available!"));
				out("    Operation Input Message: "
				    + op.getInput().getMessage().getQName()/*.getLocalPart()*/);
				out("    Operation Output Name: "
				    + ((op.getOutput().getName() != null) ? op.getOutput().getName() : "not available!"));
				out("    Operation Output Message: "
				    + op.getOutput().getMessage().getQName()/*.getLocalPart()*/);
				out("    Operation Faults: ");
				if (op.getFaults().size() > 0) {
					for (Fault fault : (Collection<Fault>) op.getFaults().<Fault>values()) {
						out("      Fault Name: " + fault.getName());
						out("      Fault Message: " + fault.getMessage().getQName()/*.getLocalPart()*/);
					}
				} else out("      There are no faults available!");

			}
			out("");
		}
		out("");

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
			for ( Object obj: msg.getParts().values()) {
				Part part  = (Part)obj;
				out("    Part Name: " + part.getName());
				out("    Part Element: " + ((part.getElementName() != null) ? part.getElementName() : "not available!"));
				out("    Part Type: " + ((part.getTypeName() != null) ? part.getTypeName() : "not available!" ));
				out("");
			}
		}
		out("");

	}

	private static void out(String str) {
		System.out.println(str);
	}
}
