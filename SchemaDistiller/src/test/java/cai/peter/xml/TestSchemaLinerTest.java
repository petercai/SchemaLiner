package cai.peter.xml;

import java.io.File;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestSchemaLinerTest
{
	private SchemaDistiller	distiller;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{}

	@Before
	public void setUp() throws Exception
	{
	}

	@Test
	public void testParseElements()
	{
		distiller =  new SchemaDistiller(new File("F:\\workspace\\github\\SchemaLiner\\SchemaDistiller\\schema\\Services\\External\\ArrangementReportingServiceIntf.xsd"));
		distiller.process();
	}

	@Test
	public void testParseServiceRequestHeaderType()
	{
		distiller=  new SchemaDistiller(new File("F:\\workspace\\github\\SchemaLiner\\SchemaDistiller\\schema\\CDM\\ServiceHeader_V2_2.xsd"));
		distiller.parseServiceRequestHeader(null);
	}
	@Test
	public void testParseServiceHeaderType()
	{
		distiller=  new SchemaDistiller(new File("F:\\workspace\\github\\SchemaLiner\\SchemaDistiller\\schema\\CDM\\ServiceHeader_V2_2.xsd"));
		distiller.parseServiceHeader(null);
	}
}
