package mdw2021;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class CompetitionMainClass {

	private static Options options;
	private static final String helpOptionString = "help";
	private static final String test1OptionString = "test1";
	private static final String test1Output = "Test geändert.";
	private static final String test2OptionString = "test2";
	private static final String test3OptionString = "test3";

	public static void main(String[] args) {
		initOptions();
		parseOptions(args);
	}

	private static void initOptions() {
		CompetitionMainClass.options = new Options();
		options.addOption(helpOptionString, "h", false, "Displays this help text.");
		OptionGroup testoptionsGroup = new OptionGroup();
		testoptionsGroup.addOption(new Option(test1OptionString, "t1", false, "Output following text: " + test1Output));
		testoptionsGroup.addOption(new Option(test2OptionString, "t2", true, "Output the given option argument"));
		testoptionsGroup
				.addOption(new Option(test3OptionString, "t3", false, "Output the current time stamp in seconds"));
		options.addOptionGroup(testoptionsGroup);

	}

	private static void parseOptions(String[] args) {
		// create the parser
		CommandLineParser parser = new DefaultParser();
		try {
			// parse the command line arguments
			CommandLine line = parser.parse(options, args);

			if (args.length == 0 || line.hasOption(helpOptionString)) {
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp("CopmetitionMainClass", options);
			} else if (line.hasOption(test1OptionString)) {
				System.out.println(test1Output);
			} else if (line.hasOption(test2OptionString)) {
				System.out.println(line.getOptionValue(test2OptionString));
			} else if (line.hasOption(test3OptionString)) {
				System.out.println(System.currentTimeMillis() / 1000);
			}

		} catch (ParseException exp) {
			// oops, something went wrong
			System.err.println("Parsing failed. Reason: " + exp.getMessage());
		}
	}
}
