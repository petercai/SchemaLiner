SchemaDistiller
===========
Intro
-----------
SchemaDistiller is a handy tool to transform XML Schema(XSD) and WSDL to an XPath-style plain text file.

Features
-----------
###1. XSD -> text

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

For example, XML schema above will be transformed to a plain text below:

	/AddRequest
	/AddRequest/ParameterOne:int
	/AddRequest/Parameter2:int[8.2]
	/AddRequest/ParameterTwo:string
	/AddRequest/Parameter3?:string
	/AddRequest/Parameter4:string[2.5]
	
	
###2. WSDL -> text

###3. Process single or multiple XML Schema (.xsd) and WSDL (.wsdl) files