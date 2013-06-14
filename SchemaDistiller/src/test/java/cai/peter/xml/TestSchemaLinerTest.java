package cai.peter.xml;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestSchemaLinerTest
{
	private SchemaDistiller	schemaLiner;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{}

	@Before
	public void setUp() throws Exception
	{
		schemaLiner = new SchemaDistiller();
	}

	@Test
	public void testParseElements()
	{
		schemaLiner.processElements(new File("F:\\workspace\\github\\SchemaLiner\\SchemaLiner\\schema\\Services\\External\\ArrangementReportingServiceIntf.xsd"));
	}

	@Test
	public void testParseServiceRequestHeaderType()
	{
		schemaLiner.parseServiceRequestHeader(new File("F:\\workspace\\github\\SchemaLiner\\SchemaLiner\\schema\\CDM\\ServiceHeader_V2_2.xsd"));
	}
	@Test
	public void testParseServiceHeaderType()
	{
		schemaLiner.parseServiceHeader(new File("F:\\workspace\\github\\SchemaLiner\\SchemaLiner\\schema\\CDM\\ServiceHeader_V2_2.xsd"));
	}
}
