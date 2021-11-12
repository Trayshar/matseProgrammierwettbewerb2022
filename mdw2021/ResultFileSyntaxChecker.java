package mdw2021;

public class ResultFileSyntaxChecker {
	static void checkSyntax(String resultFile, boolean verboseOutput) throws Exception {
		String[] lines = resultFile.split("\n");
		boolean dimensionFound = false;
		int dimensionsInt[] = { -1, -1, -1 };
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
					dimensionsInt[0] = Integer.parseInt(dimensions[0]);
					dimensionsInt[1] = Integer.parseInt(dimensions[1]);
					dimensionsInt[2] = Integer.parseInt(dimensions[2]);
					dimensionFound = true;
					numberPartsExpected = dimensionsInt[0] * dimensionsInt[1] * dimensionsInt[2];
					if (verboseOutput) {
						System.out.println("Read dimensions: " + dimensionsInt[0] + "," + dimensionsInt[1] + ","
								+ dimensionsInt[2]);
					}
					continue;
				} else {
					throw new Exception("Dimension definition incorrect: " + line);
				}
			}

			// Check whether all lines following the dimension line, contain the correct
			// part definitions
			if (line.startsWith("[")) {
				line = line.substring(1);
			} else {
				throw new Exception("Expected cube position defintion starting with '['. Got: " + line);
			}
			String tmpPartDesc[] = line.split("\\]\\ |:\\ ");
			if (tmpPartDesc.length == 3) {
				if (partDefinitions <= numberPartsExpected) {
					String positions[] = tmpPartDesc[0].split(",");
					String cubeSides[] = tmpPartDesc[2].split(" ");
					int positionInt[] = { 0, 0, 0 };
					int cubeSidesInt[] = { -1, -1, -1, -1, -1, -1 };
					// check position definition
					if (positions.length == 3) {
						for (int i = 0; i < positions.length; i++) {
							positionInt[i] = Integer.parseInt(positions[i]);
							if (positionInt[i] < 1 || positionInt[i] > dimensionsInt[i]) {
								throw new Exception(
										"Expected integer values 1-" + dimensionsInt[i] + ". Got: " + positionInt[i]);
							}
						}
					} else {
						throw new Exception("Expected three integers. Got: " + positions.length);
					}

					// check cube position and side definition
					if (cubeSides.length == 6) {
						for (int i = 0; i < cubeSides.length; i++) {
							cubeSidesInt[i] = Integer.parseInt(cubeSides[i]);
						}
						partDefinitions++;
						if (verboseOutput) {
							System.out.print("Read in part definition of \"" + tmpPartDesc[1] + "\" at position [");
							boolean first = true;
							for (int pos : positionInt) {
								if (first) {
									first = false;
									System.out.print(pos);
								} else {
									System.out.print("," + pos);
								}
							}
							System.out.print("]: ");
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
		if (verboseOutput) {
			System.out.println("Successfully finished reading result file without finding errors.");
		}
	}
}
