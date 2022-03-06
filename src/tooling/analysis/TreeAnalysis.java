package tooling.analysis;

import abstractions.Coordinate;
import abstractions.PuzzleNotSolvableException;
import abstractions.cube.CubeType;
import abstractions.cube.ICube;
import implementation.EdgeCoordinateGenerator;
import implementation.LinearCoordinateGenerator;
import tooling.Generator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class TreeAnalysis {
    public static void main(String[] args) throws PuzzleNotSolvableException, IOException {
        final int dimX = 3, dimY = 3, dimZ = 3;

        var cubes = Generator.generate(dimX, dimY, dimZ, true, false).toArray(ICube[]::new);
        var solver = new TreeBuilder(dimX, dimY, dimZ,
                Arrays.stream(cubes).filter((cube) -> CubeType.get(cube.getTriangles()) == CubeType.ThreeEdge).toArray(ICube[]::new),
                Arrays.stream(cubes).filter((cube) -> CubeType.get(cube.getTriangles()) == CubeType.FourConnected).toArray(ICube[]::new),
                Arrays.stream(cubes).filter((cube) -> CubeType.get(cube.getTriangles()) == CubeType.Five).toArray(ICube[]::new),
                Arrays.stream(cubes).filter((cube) -> CubeType.get(cube.getTriangles()) == CubeType.Six).toArray(ICube[]::new),
                new EdgeCoordinateGenerator(dimX, dimY, dimZ));

        var root = solver.build();
        //System.out.println("Got root: " + root);

        TreeRow[] rows = new TreeRow[dimX * dimY * dimZ];
        for (int i = 0; i < rows.length; i++) {
            rows[i] = new TreeRow(i);
        }
        analyzeRow(root, rows, rows.length - 1);

        for (int i = 0; i < rows.length; i++) {
            System.out.println(rows[i]);
        }


//        validateCoordinates(4, 3, 3);
    }

    private static boolean analyzeRow(TreeBuilder.TreeNode parent, TreeRow[] rows, int maxHeight) {
        TreeRow row = rows[parent.getHeight() + 1];
        row.nodes += parent.children.length;

        if(parent.getHeight() >= 0) {
            rows[parent.getHeight()].childrenOfNodes += parent.children.length;
        }

        if(parent.getHeight() + 1 == maxHeight) {
            row.solutionNodes++;
            return true;
        }

        boolean hasSolutionSomewhereUp = false;
        for (int i = 0; i < parent.children.length; i++) {
            hasSolutionSomewhereUp |= analyzeRow(parent.children[i], rows, maxHeight);
        }
        if(hasSolutionSomewhereUp) {
            row.solutionNodes++;
            row.childrenPerSolutionNode.add(parent.children.length);
        }
        return hasSolutionSomewhereUp;
    }

    private static class TreeRow {
        private final int index;
        private int nodes = 0;
        private int solutionNodes = 0;
        private final ArrayList<Integer> childrenPerSolutionNode = new ArrayList<>();
        private int childrenOfNodes = 0;

        private TreeRow(int index) {
            this.index = index;
        }

        @Override
        public String toString() {
            return String.format("TreeRow %d Nodes: %d SolutionNodes: %d Percentage: %f AvgChildren: %f AvgChildrenOfSolution: %f",
                    index, nodes, solutionNodes, ((double)solutionNodes / (double)nodes), ((double)childrenOfNodes / (double)nodes), childrenPerSolutionNode.stream().mapToInt(Integer::intValue).average().orElse(-1)
                    );
        }
    }


    private static void validateCoordinates(int dimX, int dimY, int dimZ) throws IllegalStateException{
        var t = new EdgeCoordinateGenerator(dimX, dimY, dimZ).generate();
        var b = new boolean[dimX][dimY][dimZ];

        int i = 0;
        for (Coordinate c : t) {
            System.out.println(c);
            if(b[c.x()][c.y()][c.z()]) throw new IllegalStateException(c + " already set");
            b[c.x()][c.y()][c.z()] = true;
            boolean tmp = false;
            tmp |= saveArrayGet(b, c.x() + 1, c.y(), c.z());
            tmp |= saveArrayGet(b, c.x() - 1, c.y(), c.z());
            tmp |= saveArrayGet(b, c.x(), c.y() + 1, c.z());
            tmp |= saveArrayGet(b, c.x(), c.y() - 1, c.z());
            tmp |= saveArrayGet(b, c.x(), c.y(), c.z() + 1);
            tmp |= saveArrayGet(b, c.x(), c.y(), c.z() - 1);
            if(i++ != 0 && !tmp) throw new IllegalStateException(c + " has no set neighbor");
        }

        if(i != dimX * dimY * dimZ) throw new IllegalStateException("Not all cubes traversed: " + i + "/" + dimX * dimY * dimZ);
    }

    private static boolean saveArrayGet(boolean[][][] b, int x, int y, int z) {
        if(x >= 0 && x < b.length && y >= 0 && y < b[0].length && z >= 0 && z < b[0][0].length) return b[x][y][z];
        return false;
    }
}
