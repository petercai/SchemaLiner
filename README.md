SchemaDistiller
===========
Intro
-----------
SchemaDistiller is a handy tool to transform one or more XML Schema(XSD) and WSDL files to XPath-style text files, which are stripped most of XML Schema details and focus on the data, their type information and the layout.

Features
-----------
###1. Transform XSD file to plain text file

XML Schema file:

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

XPath-style text file:

	/AddRequest
	/AddRequest/ParameterOne:int
	/AddRequest/Parameter2:int[8.2]
	/AddRequest/ParameterTwo:string
	/AddRequest/Parameter3?:string
	/AddRequest/Parameter4:string[2.5]
	
	
###2. Transform WSDL file to plain text file

