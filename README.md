SchemaDistiller
===========
Intro
-----------
SchemaDistiller is a handy tool to transform XML Schema(XSD) and WSDL to xpath-like plain text file. It represents the XML Schema's elements and their content in xpath format, which is closer to the XML that Schema described. It can be used to understand, review or even compare XML Schema or WSDL file. With the assist of this tool, SOA/Web Service developer or designer can quickly get the whole picture of a complex WSDL/XSD service contract definition. 

Features
-----------
###1. Transform XSD file to plain text file

XML Schema:

	<xs:element name="AddRequest">
		<xs:complexType>
			<xs:sequence>
				<xs:element name="ParameterOne" type="xs:int" minOccurs="1" maxOccurs="1"/>
				<xs:element name="Parameter2" minOccurs="1" maxOccurs="1">
					<xs:simpleType>
						<xs:restriction base="xs:int">
							<xs:totalDigits value="8"/>
							<xs:fractionDigits value="2"/>
						</xs:restriction>
					</xs:simpleType>
				</xs:element>
				<xs:element name="ParameterTwo" type="xs:string" minOccurs="1" maxOccurs="1"/>
				<xs:element name="Parameter3" type="xs:string" minOccurs="0" maxOccurs="1"/>
				<xs:element name="Parameter4" minOccurs="1" maxOccurs="1">
					<xs:simpleType>
						<xs:restriction base="xs:string">
							<xs:minLength value="2"/>
							<xs:maxLength value="5"/>
						</xs:restriction>
					</xs:simpleType>
				</xs:element>
			</xs:sequence>
		</xs:complexType>
	</xs:element>

XPath-style plain text:

	/AddRequest
	/AddRequest/ParameterOne:int
	/AddRequest/Parameter2:int[8.2]
	/AddRequest/ParameterTwo:string
	/AddRequest/Parameter3?:string
	/AddRequest/Parameter4:string[2.5]
	
	
###2. Transform WSDL file to plain text file

WSDL:

	<wsdl:definitions>
	  <wsdl:types>
		<xs:schema xmlns:ns="http://axisversion.sample/xsd" attributeFormDefault="qualified"
				   elementFormDefault="unqualified" targetNamespace="http://axisversion.sample/xsd">
		  <xs:element name="ExceptionFault">
			<xs:complexType>
			  <xs:sequence>
				<xs:element minOccurs="0" name="Exception" nillable="true"
							type="xs:anyType"/>
			  </xs:sequence>
			</xs:complexType>
		  </xs:element>
		  <xs:element name="getVersionResponse">
			<xs:complexType>
			  <xs:sequence>
				<xs:element minOccurs="0" name="return" nillable="true" type="xs:string"/>
			  </xs:sequence>
			</xs:complexType>
		  </xs:element>
		</xs:schema>
	  </wsdl:types>
	  <wsdl:message name="getVersionRequest"/>
	  <wsdl:message name="getVersionResponse">
		<wsdl:part name="parameters" element="ns0:getVersionResponse"/>
	  </wsdl:message>
	  <wsdl:message name="getVersionFault">
		<wsdl:part name="parameters" element="ns0:ExceptionFault"/>
	  </wsdl:message>
	  <wsdl:portType name="VersionPortType">
		<wsdl:operation name="getVersion">
		  <wsdl:input message="ns0:getVersionRequest" wsaw:Action="urn:getVersion"/>
		  <wsdl:output message="ns0:getVersionResponse" wsaw:Action="urn:getVersionResponse"/>
		  <wsdl:fault message="ns0:getVersionFault" name="getVersionFault"
					  wsaw:Action="urn:getVersionFault"/>
		</wsdl:operation>
		<wsdl:operation name="foo-bar">
		  <wsdl:input message="getVersionRequest"/>
		</wsdl:operation>
		<wsdl:operation name="Foo-ba.r2">
		  <wsdl:input message="getVersionRequest"/>
		</wsdl:operation>
	  </wsdl:portType>
	  <wsdl:binding name="VersionSOAP11Binding" type="ns0:VersionPortType">
		<soap:binding transport="http://schemas.xmlsoap.org/soap/http" style="document"/>
		<wsdl:operation name="getVersion">
		  <soap:operation soapAction="urn:getVersion" style="document"/>
		  <wsdl:input>
			<soap:body use="literal"/>
		  </wsdl:input>
		  <wsdl:output>
			<soap:body use="literal"/>
		  </wsdl:output>
		  <wsdl:fault name="getVersionFault">
			<soap:fault use="literal" name="getVersionFault"/>
		  </wsdl:fault>
		</wsdl:operation>
		<wsdl:operation name="foo-bar">
		  <soap:operation soapAction="urn:foo-bar" style="document"/>
		  <wsdl:input>
			<soap:body use="literal"/>
		  </wsdl:input>
		</wsdl:operation>
		<wsdl:operation name="Foo-ba.r2">
		  <soap:operation soapAction="urn:foo-bar" style="document"/>
		  <wsdl:input>
			<soap:body use="literal"/>
		  </wsdl:input>
		</wsdl:operation>

	  </wsdl:binding>
	</wsdl:definitions>

XPath-style plain text:

	/PortType:VersionPortType
	/Operation:getVersion
	/Operation:getVersion/Input:getVersionRequest
	/Operation:getVersion/Output:getVersionResponse
	/Operation:getVersion/Output:getVersionResponse/getVersionResponse
	/Operation:getVersion/Output:getVersionResponse/getVersionResponse/return?:string
	/Operation:getVersion/Fault:getVersionFault
	/Operation:getVersion/Fault:getVersionFault/ExceptionFault
	/Operation:getVersion/Fault:getVersionFault/ExceptionFault/Exception?:anyType
	/Operation:foo-bar
	/Operation:foo-bar/Input:getVersionRequest
	/Operation:Foo-ba.r2
	/Operation:Foo-ba.r2/Input:getVersionRequest
	/Message:getVersionFault
	/Message:getVersionFault/ExceptionFault
	/Message:getVersionFault/ExceptionFault/Exception?:anyType
	/Message:getVersionRequest
	/Message:getVersionRequest
	/Message:getVersionResponse
	/Message:getVersionResponse/getVersionResponse
	/Message:getVersionResponse/getVersionResponse/return?:string
	
Download
----------------
here[]