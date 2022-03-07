package tooling.analysis;

import abstractions.Coordinate;
import abstractions.PuzzleNotSolvableException;
import abstractions.cube.CubeType;
import abstractions.cube.ICube;
import abstractions.cube.ICubeFilter;
import abstractions.cube.Triangle;
import implementation.cube.filter.CubeFilterFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class TreeAnalysis {
    public static final int[] upperLimit = new int[36];
    public static final int[] lowerLimit = new int[36];

    static {
        Arrays.fill(upperLimit, Integer.MIN_VALUE);
        Arrays.fill(lowerLimit, Integer.MAX_VALUE);
    }

    public static Coordinate[] coords = null;

    public static void main(String[] args) throws PuzzleNotSolvableException, IOException {
        final int dimX = 5, dimY = 4, dimZ = 2;



//        FileWriter fw = new FileWriter("result_files/pathAnalysis.txt");
//        int length = dimX * dimY * dimZ;
//        Coordinate[] bestCoords = null;
//        double runtime = 20;
//        Coordinate[] t = new LinearCoordinateGenerator(dimX, dimY, dimZ).generate();
//        for(var perms = new PermIterator(length); perms.hasNext();) {
//            int[] e = perms.next();
//            double time = 0d;
//
//            Coordinate[] newCoords = new Coordinate[length];
//            for (int i = 0; i < length; i++) {
//                newCoords[i] = t[e[i]];
//            }
//
//            coords = newCoords;
//            System.out.println("Coords: " + Arrays.toString(newCoords));
//            for(File f : new File("4x2x5").listFiles((file, s) -> s.endsWith(".txt"))) {
//                Generator.clearArrayCubeSorterCache();
//                Puzzle p = new Puzzle();
//                System.out.println("Running test " + f.getName());
//                p.readInput(f.getAbsolutePath());
//
//                double deltaT = Generator.doTesting(p.dimensionX, p.dimensionY, p.dimensionZ, p.cubes, (int) Math.ceil(runtime/10), f.getAbsolutePath());
//
//                time += deltaT;
//            }
//
//            if(time < runtime) {
//                runtime = time;
//                bestCoords = newCoords;
//                String s = "New best (" + runtime + "): " + Arrays.toString(bestCoords) + "\n";
//                fw.write(s);
//                System.out.println(s);
//                fw.flush();
//            }
//        }
//        fw.close();
//
//        System.out.print("Best (" + runtime + "): " + Arrays.toString(bestCoords));

//        while(true) {
//            Generator.clearArrayCubeSorterCache();
//            var cubes = Generator.generate(dimX, dimY, dimZ, true, false).toArray(ICube[]::new);
//            var solver = new TreeBuilder(dimX, dimY, dimZ,
//                    Arrays.stream(cubes).filter((cube) -> CubeType.get(cube.getTriangles()) == CubeType.ThreeEdge).toArray(ICube[]::new),
//                    Arrays.stream(cubes).filter((cube) -> CubeType.get(cube.getTriangles()) == CubeType.FourConnected).toArray(ICube[]::new),
//                    Arrays.stream(cubes).filter((cube) -> CubeType.get(cube.getTriangles()) == CubeType.Five).toArray(ICube[]::new),
//                    Arrays.stream(cubes).filter((cube) -> CubeType.get(cube.getTriangles()) == CubeType.Six).toArray(ICube[]::new),
//                    new LinearCoordinateGenerator(dimX, dimY, dimZ));
//
//            var root = solver.build();
//
//            System.out.println("Upper: "+Arrays.toString(upperLimit));
//            System.out.println("Lower: "+Arrays.toString(lowerLimit));
//        }

        //System.out.println("Got root: " + root);
//
//        TreeRow[] rows = new TreeRow[dimX * dimY * dimZ];
//        for (int i = 0; i < rows.length; i++) {
//            rows[i] = new TreeRow(i);
//        }
//        analyzeRow(root, rows, rows.length - 1);
//
//        for (int i = 0; i < rows.length; i++) {
//            System.out.println(rows[i]);
//        }


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
        var t = new CustomCoordinateGenerator(dimX, dimY, dimZ).generate();
        var b = new boolean[dimX][dimY][dimZ];

        int i = 0;
        for (Coordinate c : t) {
            ICubeFilter f = CubeFilterFactory.defaultFilter();
            if(c.x() == 0) f.setSide(ICube.Side.Back, Triangle.None);
            if(c.x() == dimX - 1) f.setSide(ICube.Side.Front, Triangle.None);
            if(c.y() == 0) f.setSide(ICube.Side.Left, Triangle.None);
            if(c.y() == dimY - 1) f.setSide(ICube.Side.Right, Triangle.None);
            if(c.z() == 0) f.setSide(ICube.Side.Down, Triangle.None);
            if(c.z() == dimZ - 1) f.setSide(ICube.Side.Up, Triangle.None);

            CubeType type = CubeType.get(f.getTriangles());
            System.out.println(c + ": " + type);

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
