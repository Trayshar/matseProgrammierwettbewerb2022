package tooling;

import abstractions.IPuzzleSolution;
import abstractions.IPuzzleSolver;
import abstractions.Orientation;
import abstractions.PuzzleNotSolvableException;
import abstractions.cube.ICube;
import abstractions.cube.ICubeFilter;
import abstractions.cube.Triangle;
import implementation.cube.CachedCube;
import implementation.cube.filter.CubeFilterFactory;
import implementation.cube.sorter.ArrayCubeSorter;
import implementation.solver.SolverFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Generator {

    public static void main(String[] args) throws IOException {
        final int x = 4;
        final int y = 2;
        final int z = 5;

        for (int i = 0; i < 20; i++) {
            generate(x, y, z, true, true);
            File f = new File("result_files/selfcheck.in.txt");
            Files.copy(f.toPath(), Path.of("input_files/" + x + "x" + y + "x" + z + "/test_" + i + ".txt"));
        }
    }

    private final static Random rand = new Random();
    private final static ICubeFilter defaultFilter = CubeFilterFactory.from(Triangle.AnyNotNone, Triangle.AnyNotNone, Triangle.AnyNotNone, Triangle.AnyNotNone, Triangle.AnyNotNone, Triangle.AnyNotNone);

    public static Stream<ICube> generate(int dimensionX, int dimensionY, int dimensionZ, boolean shuffle, boolean rotate) {
        var cubes = new ICube[dimensionX][dimensionY][dimensionZ];
        ICubeFilter[][][] filters = new ICubeFilter[dimensionX][dimensionY][dimensionZ];
        for (int x = 0; x < dimensionX; x++) {
            for (int y = 0; y < dimensionY; y++) {
                for (int z = 0; z < dimensionZ; z++) {
                    ICubeFilter f = defaultFilter.cloneFilter(); // Match filter against bounds
                    if(x == 0) f.setSide(ICube.Side.Back, Triangle.None);
                    if(x == dimensionX - 1) f.setSide(ICube.Side.Front, Triangle.None);
                    if(y == 0) f.setSide(ICube.Side.Left, Triangle.None);
                    if(y == dimensionY - 1) f.setSide(ICube.Side.Right, Triangle.None);
                    if(z == 0) f.setSide(ICube.Side.Down, Triangle.None);
                    if(z == dimensionZ - 1) f.setSide(ICube.Side.Up, Triangle.None);
                    filters[x][y][z] = f;
        }}}

        int id = 1;
        for (int x = 0; x < dimensionX; x++) {
            for (int y = 0; y < dimensionY; y++) {
                for (int z = 0; z < dimensionZ; z++) {
                    // go over all sides of the filter and construct a cube fitting inside
                    Triangle[] triangles = new Triangle[6];
                    int i = 0;
                    for (ICube.Side s : ICube.Side.values()) {
                        Triangle req = filters[x][y][z].getSide(s);
                        Triangle ret;
                        if(req != Triangle.AnyNotNone) { // Triangle is already determined or is "None"
                            ret = req;
                        }else{ // Triangle can be chosen freely
                            ret = Triangle.valueOf( rand.nextInt(1, 5) );
                            // Update nearby filter
                            if(valid(x + s.x, dimensionX) && valid(y + s.y, dimensionY) && valid(z + s.z, dimensionZ)) {
                                filters[x + s.x][y + s.y][z + s.z].setSide(s.getOpposite(), ret.getMatching(s.z != 0));
                            }
                        }
                        filters[x][y][z].setSide(s, ret);
                        triangles[i++] = ret;
                    }

                    cubes[x][y][z] = new CachedCube(id++, triangles);
                }
            }
        }

        //Self-check
        try{
            // Check if puzzle has a solution...
            File f = new File("result_files/selfcheck.out.txt");
            f.createNewFile();
            FileWriter fw = new FileWriter(f);
            fw.write(String.format("Dimension %d,%d,%d\n", dimensionX, dimensionY, dimensionZ));
            for (int x = 0; x < dimensionX; x++) {
                for (int y = 0; y < dimensionY; y++) {
                    for (int z = 0; z < dimensionZ; z++) {
                        fw.write(String.format("[%d,%d,%d] %s\n", x + 1, y + 1, z + 1, cubes[x][y][z].serialize()));
            }}}
            fw.close();
            String result = executeExternalJar("library/RaetselTester.jar", new String[]{"result_files/selfcheck.out.txt"});
            if(Pattern.compile(".*Puzzle enth??lt \\d+ Fehler!.*").matcher(result).find()) {
                System.out.print(result);
                System.exit(1);
            }

            f = new File("result_files/selfcheck.in.txt");
            f.createNewFile();
            fw = new FileWriter(f);
            fw.write(String.format("Dimension %d,%d,%d\n", dimensionX, dimensionY, dimensionZ));
            for (int x = 0; x < dimensionX; x++) {
                for (int y = 0; y < dimensionY; y++) {
                    for (int z = 0; z < dimensionZ; z++) {
                        if(rotate) cubes[x][y][z].setOrientation(getRandomOrientation());
                        fw.write(String.format("%s\n",cubes[x][y][z].serialize()));
            }}}
            fw.close();
            result = executeExternalJar("library/validator.jar", new String[]{"result_files/selfcheck.in.txt", "result_files/selfcheck.out.txt"});
            if(Pattern.compile(".* insgesamt \\d+ Fehler auf!.*").matcher(result).find()) {
                System.out.print(result);
                System.exit(1);
            }

            System.out.println("Self-test successful!");
        }catch (IOException e) {
            System.out.printf("Self-check failed with IO error: %s\n", e.getMessage());
            e.printStackTrace();
            System.exit(1);
        } catch (IllegalStateException e) {
            System.out.printf("Self-check failed with other error: %s\n", e.getCause().getMessage());
            e.printStackTrace();
            System.exit(1);
        }

        var c = Arrays.stream(cubes).flatMap(Arrays::stream).flatMap(Arrays::stream);
        if(shuffle) {
            var l = c.collect(Collectors.toList());
            Collections.shuffle(l);
            return l.stream();
        }
        return c;
    }

    private static boolean valid(int val, int max) {
        return val >= 0 && val < max;
    }

    private static Orientation getRandomOrientation() {
        return Orientation.get( rand.nextInt(0, 24));
    }

    protected static String executeExternalJar(String path, String[] arguments) throws IOException, IllegalStateException {
        try{
            // Redirect output into local buffer.
            java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
            PrintStream previous = System.out;
            System.setOut(new java.io.PrintStream(out));

            // Execute jar file
            // https://stackoverflow.com/a/16246002
            File lib = new File(path);
            String mainClass;
            try (var jarFile = new JarFile(lib)) {
                final Manifest manifest = jarFile.getManifest();
                // Skip JarInJarLoader; As expected, eclipse libraries crash. So we are going straight to the source.
                mainClass = manifest.getMainAttributes().getValue("Rsrc-Main-Class");
                if (mainClass == null) mainClass = manifest.getMainAttributes().getValue("Main-Class");
            }
            final URLClassLoader child = new URLClassLoader(new URL[]{lib.toURI().toURL()}, Generator.class.getClassLoader());
            final Class<?> classToLoad = Class.forName(mainClass, true, child);
            final Method method = classToLoad.getDeclaredMethod("main", String[].class);
            method.invoke(null, (Object) arguments);
            child.close();

            // Fetch output and reset System.out
            String result = out.toString();
            System.setOut(previous);
            return result;
        }catch (IllegalArgumentException | ClassNotFoundException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new IllegalStateException(e);
        }

    }

    public static void shutdown() {
        observerExecutor.shutdownNow();
        solverExecutor.shutdownNow();
    }

    private static final ScheduledExecutorService observerExecutor = Executors.newSingleThreadScheduledExecutor();
    private static final ExecutorService solverExecutor = Executors.newFixedThreadPool(4);

    public static double doTesting(int dimX, int dimY, int dimZ, boolean shuffle, boolean rotate, int timeout) {
        System.out.printf("Generating puzzle: [%d, %d, %d] %s %s\n", dimX, dimY, dimZ, shuffle ? "shuffled" : "", rotate ? "rotated" : "");
        var cubes = generate(dimX, dimY, dimZ, shuffle, rotate).toArray(ICube[]::new);
        return doTesting(dimX, dimY, dimZ, cubes, timeout, "result_files/selfcheck.in.txt");
    }

    public static double doTesting(int dimX, int dimY, int dimZ, ICube[] cubes, int timeout, String fileIn) {
        System.out.printf("Got %d cubes...\n", cubes.length);
        IPuzzleSolution solution = null;
        try{
            //printMemoryStats();
            clearArrayCubeSorterCache();
            System.out.println("--- Starting solver ---");

            var s = SolverFactory.of(dimX, dimY, dimZ, cubes);

            long var3 = System.currentTimeMillis();
            solution = s.solveWithTimeout(solverExecutor, observerExecutor, timeout);
            long var5 = System.currentTimeMillis();

            double time = (double)(var5 - var3) / 1000.0D;
            System.out.println("--- Solver finished ---");
            System.out.printf("Took %f seconds.\n", time);
            System.out.println("-----------------------");
            if(solution == null) throw new PuzzleNotSolvableException();

            File f = new File("result_files/selfcheck.out.txt");
            f.createNewFile();
            FileWriter fw = new FileWriter(f);
            fw.write(solution.serialize());
            fw.close();

            String result = executeExternalJar("library/RaetselTester.jar", new String[]{"result_files/selfcheck.out.txt"});
            if(Pattern.compile(".*Puzzle enth??lt \\d+ Fehler!.*").matcher(result).find()) {
                System.err.println("Failed to solve puzzle!");
                System.err.print(result);
                throw new PuzzleNotSolvableException();
            }

            result = executeExternalJar("library/validator.jar", new String[]{fileIn, "result_files/selfcheck.out.txt"});
            if(Pattern.compile(".* insgesamt \\d+ Fehler auf!.*").matcher(result).find()) {
                System.err.println("Failed to solve puzzle!");
                System.err.print(result);
                throw new PuzzleNotSolvableException();
            }

            return time;
        }catch (PuzzleNotSolvableException  e) {
            System.err.println("Failed to solve puzzle: " + e.getMessage());
            System.err.printf("Solution: %s\n", solution == null ? "null" : solution.serialize());
            System.err.printf("Cubes: %s\n", Arrays.toString(Arrays.stream(cubes).map(ICube::serialize).toArray()));
            System.exit(-1);
            return -1;
        } catch (IOException | IllegalStateException e) {
            e.printStackTrace();
            System.exit(-1);
            return -1;
        } catch (TimeoutException e) {
            System.out.println("Timeout!");
            return timeout;
        }
    }

    private static void printMemoryStats() {
        Runtime instance = Runtime.getRuntime();
        System.out.print ("--- Memory Stats in MB ---\n");
        System.out.printf("Allocated: %04d Max:  %04d\n", instance.totalMemory() / 1048576L, instance.maxMemory() / 1048576L);
        System.out.printf("Used:      %04d Free: %04d\n", (instance.totalMemory() - instance.freeMemory()) / 1048576L, instance.freeMemory() / 1048576L);
        System.out.print ("--------------------------\n");
    }

    protected static void clearArrayCubeSorterCache() {
        for(int i = 0; i < 46656; i++) {
            ArrayCubeSorter.queries[i] = null;
        }
    }
}
