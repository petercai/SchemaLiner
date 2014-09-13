/***********************************************
 * Copyright (c) 2013 Peter Cai                *
 * All rights reserved.                        *
 ***********************************************/
package cai.peter.sd;

import java.io.File;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class Launcher
{
	public static void main(String[] args) 
	{
		Options options = new Options();
		options.addOption("t",false, "show elements' Type information");
		options.addOption("c",false, "show elements' Cardinality information");
		options.addOption("m",false, "show root elements in Multiple files");
		Option option = new Option("f",true, "File name(.xsd|.wsdl) or Folder to transfrom");
		option.setRequired(true);
		options.addOption(option);
		CommandLineParser parser = new BasicParser();
		try
		{
			CommandLine cl = parser.parse(options, args);
			String filename = cl.getOptionValue("f");
			SchemaTransformer.transform(new File(filename));
			if( cl.hasOption("t"))
			{
				XdOption.getInstance().opt = XdOption.Option.SHOW_TYPEINFO;
			}
			else if( cl.hasOption("c"))
			{
				XdOption.getInstance().opt = XdOption.Option.SHOW_CARDINALITY;
			}
			
			if( cl.hasOption("m"))
			{
				XdOption.getInstance().showMultiFile = true;
			}
			else
			{
				help(options);
			}
		}
		catch (ParseException e)
		{
			help(options);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	static void help(Options options)
	{
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("java -jar xd.jar -f <arg> [options]", options);
	}
}
