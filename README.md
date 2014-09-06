SchemaDistiller
===========
Intro
-----------
SchemaDistiller is a handy tool to transform XML Schema(XSD) and WSDL to an XPath-style plain text file.

Features
-----------
###1. XSD -> text

	<xs:schema ...>
		<xs:element name="AddRequest">
			<xs:complexType>
				<xs:sequence>
					<xs:element maxOccurs="1" minOccurs="1" name="ParameterOne" type="xs:int"/>
					<xs:element maxOccurs="1" minOccurs="1" name="ParameterTwo" type="xs:string"/>
				</xs:sequence>
			</xs:complexType>
		</xs:element>
	</xs:schema>

For example, XML schema above will be transformed to a plain text below:

	/AddRequest
	/AddRequest/ParameterOne:int
	/AddRequest/ParameterTwo:string

	
	
###2. WSDL -> text

###3. Process single or multiple XML Schema (.xsd) and WSDL (.wsdl) files