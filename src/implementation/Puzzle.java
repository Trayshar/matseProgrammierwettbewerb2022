package implementation;

import abstractions.IPuzzelSolution;
import abstractions.cube.ICube;
import implementation.cube.StaticCubeSet;
import mdw2021.IPuzzle;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

public class Puzzle implements IPuzzle {

	private int dimensionX, dimensionY, dimensionZ;
	private StaticCubeSet cubes;
	private IPuzzelSolution solution;

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
					cubes.add(GigaFactory.readFromRaw(s));
				}
			});
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		}

		this.cubes = new StaticCubeSet(cubes.toArray(new ICube[0]));
	}

	public void solve() {
		this.solution = GigaFactory.constructSolver().solve(dimensionX, dimensionY, dimensionZ, cubes);
	}

	public boolean hasSolution() {
		return this.solution != null;
	}

	public void writeResult(String filename) {
		try {
			FileWriter fw = new FileWriter(filename);
			fw.write(solution.serialize());
			fw.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
