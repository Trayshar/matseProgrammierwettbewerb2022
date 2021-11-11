package mdw2021;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

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
	private static final String test1Output = "Test";
	private static final String test2OptionString = "test2";
	private static final String test3OptionString = "test3";
	private static final String inputfileOptionString = "inputfile";
	private static final String resultfileOptionString = "resultfile";
	private static final String verboseOutputOptionString = "verbose";
	private static boolean verboseOutput = false;

	public static void main(String[] args) {
		initOptions();
		parseOptions(args);
	}

	private static void initOptions() {
		CompetitionMainClass.options = new Options();
		options.addOption("h", helpOptionString, false, "Displays this help text.");
		// Generate group of test options
		OptionGroup testoptionsGroup = new OptionGroup();
		testoptionsGroup.addOption(new Option(test1OptionString, "t1", false, "Output following text: " + test1Output));
		testoptionsGroup.addOption(new Option(test2OptionString, "t2", true, "Output the given option argument"));
		testoptionsGroup
				.addOption(new Option(test3OptionString, "t3", false, "Output the current time stamp in seconds"));
		options.addOptionGroup(testoptionsGroup);
		// Generate input file option
		options.addOption("i", inputfileOptionString, true, "Path to competition input file");
		// Generate result file option
		options.addOption("r", resultfileOptionString, true, "Path to competition result file");
		// Generate option for verbose output
		options.addOption("v", verboseOutputOptionString, false, "Output verbose information, if available");
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
			if (line.hasOption(verboseOutputOptionString)) {
				verboseOutput = true;
			}
			if (line.hasOption(inputfileOptionString) && line.hasOption(resultfileOptionString)) {
				// load files
				String inputFile = loadFileContent(line.getOptionValue(inputfileOptionString));
				String resultFile = loadFileContent(line.getOptionValue(resultfileOptionString));
				// check syntax of both files
				InputFileSyntaxChecker.CheckSyntax(inputFile);
				ResultFileSyntaxChecker.CheckSyntax(resultFile);
				// compare input and output files
				SemanticsChecker.checkInputOutpuConsistency(inputFile, resultFile);

			} else if (line.hasOption(inputfileOptionString)) {
				// load file
				String inputFile = loadFileContent(line.getOptionValue(inputfileOptionString));
				// check syntax of input file
				InputFileSyntaxChecker.CheckSyntax(inputFile);
			} else if (line.hasOption(resultfileOptionString)) {
				// load files
				String resultFile = loadFileContent(line.getOptionValue(resultfileOptionString));
				// check syntax of output file
				ResultFileSyntaxChecker.CheckSyntax(resultFile);
			}

		} catch (ParseException exp) {
			System.err.println("Parsing failed. Reason: " + exp.getMessage());
		} catch (IOException exp) {
			System.err.println("Loading file failed: " + exp.getMessage());
		} catch (Exception e) {
			System.err.println("Failed. Reason: " + e.getMessage());
		}
	}

	private static String loadFileContent(String path) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded);
	}
}
