package implementation;

import abstractions.IPuzzleSolution;
import abstractions.PuzzleNotSolvableException;
import abstractions.cube.ICube;
import abstractions.cube.Triangle;
import implementation.cube.CachedCube;
import implementation.solution.NoSolution;
import implementation.solver.StagedSolver;
import mdw2021.IPuzzle;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class Puzzle implements IPuzzle {
	/** flag to control if this is run in debug mode. */
	public static final boolean DEBUG = false;

	private int dimensionX, dimensionY, dimensionZ;
	private ICube[] cubes;
	private IPuzzleSolution solution;

	public void readInput(String filename) {
		ArrayList<ICube> cubes = new ArrayList<>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(filename));
			reader.lines().filter(s -> !s.startsWith("//")).forEachOrdered(s -> {
				if(s.startsWith("Dimension")) {
					var tmp = s.substring(10).split(",");
					System.out.println(Arrays.toString(tmp));
					this.dimensionX = Integer.parseInt(tmp[0]);
					this.dimensionY = Integer.parseInt(tmp[1]);
					this.dimensionZ = Integer.parseInt(tmp[2]);
				}else {
					cubes.add(readFromRaw(s));
				}
			});
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		}

		this.cubes = cubes.stream().filter(Objects::nonNull).toArray(ICube[]::new);
	}

	public void solve() {
		try {
			this.solution = new StagedSolver(dimensionX, dimensionY, dimensionZ, cubes).solve(0, 0, 0, null);
		} catch (PuzzleNotSolvableException e) {
			e.printStackTrace();
			this.solution = new NoSolution(dimensionX, dimensionY, dimensionZ);
		}
	}

	public boolean hasSolution() {
		return this.solution != null;
	}

	public void writeResult(String filename) {
		String s = "ERROR!";
		try {
			FileWriter fw = new FileWriter(filename);
			s = solution.serialize();
			fw.write(s);
			fw.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Got solution: \n" + s);
	}

	public static ICube readFromRaw(String s) {
		int col = s.indexOf(':');
		int id = Integer.parseInt(s.substring(5, col));
		Triangle[] tri = Arrays.stream(s.substring(col + 2).split(" ")).mapToInt(Integer::parseInt).mapToObj(Triangle::valueOf).toArray(Triangle[]::new);
		System.out.println("Read cube: " + id + ", " + Arrays.toString(tri));
		return new CachedCube(id, tri);
	}

}
