package mdw2021;

public class Cube {
	String sides;

	public Cube(int[] inputSides) {
		sides = " " + inputSides[0] + inputSides[1] + inputSides[2] + inputSides[3] + inputSides[4] + inputSides[5];
	}

	public void rotate(int x, int y, int z) {

	}

	public boolean equals(Cube cubeToCompare) {
		return sides.equals(cubeToCompare.sides);
	}
}
