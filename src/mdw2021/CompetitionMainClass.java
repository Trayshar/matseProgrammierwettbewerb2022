package mdw2021;

import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import implementation.Puzzle;

public class CompetitionMainClass {

	private static Options options;
	private static final String helpOptionString = "help";
	private static final String test1OptionString = "test1";
	private static final String test1Output = "Test";
	private static final String test2OptionString = "test2";
	private static final String test3OptionString = "test3";
    private static final String inputfileOptionString = "inputfile";
    private static final String resultfileOptionString = "resultfile";
	private static String inputFilename;
	private static String resultFilename;
	private static boolean runAlgorithm = false;	

	public static void main(String[] args) {
		initOptions();
		parseOptions(args);
		if (runAlgorithm) {			
			final long timeStart = System.currentTimeMillis();			
										
			IPuzzle p = new Puzzle();
			p.readInput(inputFilename);
			p.solve();
			if (p.hasSolution())
				p.writeResult(resultFilename);
			else
				noSolution(resultFilename);
			
			final long timeEnd = System.currentTimeMillis();
			System.out.println("Processing of file " + inputFilename + " took " + (timeEnd - timeStart) / 1000.0 + " seconds."); 		
		}
	}
	
	private static void noSolution(String filename) {
	    try {
	        FileWriter myWriter = new FileWriter(filename);
	        myWriter.write("No Solution was found!\n");
	        myWriter.close();
	      } catch (IOException e) {
	        System.err.println("Could not open file for writing!");
	        e.printStackTrace();
	      }		
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
			if (line.hasOption(inputfileOptionString) && line.hasOption(resultfileOptionString)) {
				// load files
				inputFilename = line.getOptionValue(inputfileOptionString);
				resultFilename = line.getOptionValue(resultfileOptionString);
				runAlgorithm = true;

			} else if (line.hasOption(inputfileOptionString)) {
				throw new ParseException("Input File given, but no Result File was defined!");
			} else if (line.hasOption(resultfileOptionString)) {
				throw new ParseException("Result File given, but no Input File was defined!");
			}

		} catch (ParseException exp) {
			System.err.println("Parsing failed. Reason: " + exp.getMessage());
		} catch (Exception e) {
			System.err.println("Failed. Reason: " + e.getMessage());
		}
	}
}
