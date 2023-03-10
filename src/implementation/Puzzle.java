package implementation;

import abstractions.IPuzzleSolution;
import abstractions.IPuzzleSolver;
import abstractions.PuzzleNotSolvableException;
import abstractions.cube.ICube;
import abstractions.cube.Triangle;
import implementation.cube.CachedCube;
import implementation.solver.SolverFactory;
import mdw2021.IPuzzle;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;

public class Puzzle implements IPuzzle {
	/** flag to control if this is run in debug mode. */
	public static final boolean DEBUG = false;
	public static final boolean LOG = false;

	public int dimensionX, dimensionY, dimensionZ;
	public ICube[] cubes;
	public IPuzzleSolution solution;

	public void readInput(String filename) {
		ArrayList<ICube> cubes = new ArrayList<>();
		try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
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
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		this.cubes = cubes.stream().filter(Objects::nonNull).toArray(ICube[]::new);
	}

	public void solve() {
		try {
			var s = SolverFactory.of(dimensionX, dimensionY, dimensionZ, cubes);
			if(Puzzle.LOG) {
				this.solution = s.solveWithLogging();
			}else {
				this.solution = s.solve();
			}
		} catch (PuzzleNotSolvableException e) {
			e.printStackTrace();
			this.solution = null;
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
		//System.out.println("Read cube: " + id + ", " + Arrays.toString(tri));
		return new CachedCube(id, tri);
	}

}
