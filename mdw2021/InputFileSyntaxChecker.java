package mdw2021;

public class InputFileSyntaxChecker {
	static void checkSyntax(String inputFile, boolean verboseOuptput) throws Exception {
		String[] lines = inputFile.split("\n");
		boolean dimensionFound = false;
		int d1, d2, d3;
		int numberPartsExpected = -1;
		int partDefinitions = 0;

		for (String line : lines) {
			// Throw away lines starting with comment signs
			if (line.startsWith("//")) {
				continue;
			}
			// Check whether first none-comment line contains a correct dimension definition
			if (!dimensionFound && line.startsWith("Dimension ")) {
				String dimensions[] = line.substring(10).split(",");
				if (dimensions.length == 3) {
					d1 = Integer.parseInt(dimensions[0]);
					d2 = Integer.parseInt(dimensions[1]);
					d3 = Integer.parseInt(dimensions[2]);
					dimensionFound = true;
					numberPartsExpected = d1 * d2 * d3;
					if (verboseOuptput) {
						System.out.println("Read dimensions: " + d1 + "," + d2 + "," + d3);
					}
					continue;
				} else {
					throw new Exception("Dimension definition incorrect: " + line);
				}
			}

			// Check whether all lines following the dimension line, contain the correct
			// part definitions
			String tmpPartDesc[] = line.substring(5).split(":\\ ");
			if (tmpPartDesc.length == 2) {
				if (partDefinitions <= numberPartsExpected) {
					String cubeSides[] = tmpPartDesc[1].split(" ");
					int cubeSidesInt[] = { -1, -1, -1, -1, -1, -1 };
					if (cubeSides.length == 6) {
						for (int i = 0; i < cubeSides.length; i++) {
							cubeSidesInt[i] = Integer.parseInt(cubeSides[i]);
						}
						partDefinitions++;
						if (verboseOuptput) {
							System.out.print("Read in part definition of \"" + tmpPartDesc[0] + "\": ");
							for (int side : cubeSidesInt) {
								System.out.print(side + " ");
							}
							System.out.println();
						}
					} else {
						throw new Exception("Expected six integers. Got: " + cubeSides.length);
					}
				} else {
					throw new Exception("Got part definition " + partDefinitions + ". Expected " + numberPartsExpected
							+ " or less.");
				}
			} else {
				throw new Exception(
						"Expected part denominator seperated by a colon from six integers seperated by spaces. Got:"
								+ line.substring(5));
			}
		}
		// Check, whether the number of parts match the dimension definition
		if (partDefinitions != numberPartsExpected) {
			throw new Exception("Got " + partDefinitions + " definitions. Expected " + numberPartsExpected + ".");
		}
		if (verboseOuptput) {
			System.out.println("Successfully finished reading input file without finding errors.");
		}
	}
}
