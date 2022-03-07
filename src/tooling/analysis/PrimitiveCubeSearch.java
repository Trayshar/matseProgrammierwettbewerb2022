package tooling.analysis;

import abstractions.Orientation;
import abstractions.cube.CubeType;
import abstractions.cube.ICube;
import abstractions.cube.ICubeFilter;
import abstractions.cube.Triangle;
import implementation.cube.CachedCube;
import implementation.cube.filter.ByteCubeFilter;
import implementation.cube.filter.CubeFilterFactory;
import implementation.cube.sorter.CubeSorterFactory;
import implementation.cube.sorter.HashCubeSorter;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class PrimitiveCubeSearch {

    public static void main(String[] args) {
//        generateFilterToTypeMapping();
//        System.exit(0);
        HashSet<Integer> ids = new HashSet<>();

        // For all cube types
        for (CubeType t : CubeType.values()) {
            HashCubeSorter s = CubeSorterFactory.makeHashCubeSorter(new ICube[]{});

            // For all possible cubes of that type:
            forAllPermutations(t.predicate, f -> {
                if(s.unique(f) == 0) { // No cube for this.
                    addCube(s, f);
                }
            });

            for (Orientation o : Orientation.getValues()) {
                ICubeFilter c = new ByteCubeFilter((ByteCubeFilter) t.predicate, o);
                forAllPermutations(c, f -> {
                    if(s.unique(f) != 1) { // Check that everything does have exactly one result
                        System.out.printf("Got more than one candidates for filter %s\n", f);
                    }
                });
            }

            ICube[] cubes = get(s);
            System.out.println("CubeType " + t + " with " + s.getNumCachedQueries() + " permutations needs " + cubes.length + " unique cubes:");
            for (ICube c : cubes) {
                int id = c.getUniqueCubeId();
                System.out.printf("[%d] %s\n", id, c.serialize());
                if(ids.contains(id)) {
                    System.out.println("Violation! ID " + id + "already used!");
                    System.exit(-1);
                }
                ids.add(id);
            }
            System.out.println("----------");
            System.out.println(ids.stream().min(Integer::compareTo).get() + "," + ids.stream().max(Integer::compareTo).get());
            System.out.println("----------");
            ids.clear();
        }
    }

    public static CubeType[][][][][][] generateFilterToTypeMapping() {
        CubeType[][][][][][] types = new CubeType[2][2][2][2][2][2];
        for (CubeType t : CubeType.values()) {
            //System.out.println("Type " + t);
            Orientation.stream().map(orientation -> {
                Byte[] x = new Byte[6];
                for (int i = 0; i < 6; i++) {
                    x[orientation.side[i]] = (byte) t.predicate.getSide(ICube.Side.valueOf(i)).ordinal();
                }
                return Arrays.stream(x).collect(Collectors.toList());
            }).distinct().forEach(bytes -> {
                if(types[bytes.get(0) == 0 ? 0 : 1]
                        [bytes.get(1) == 0 ? 0 : 1]
                        [bytes.get(2) == 0 ? 0 : 1]
                        [bytes.get(3) == 0 ? 0 : 1]
                        [bytes.get(4) == 0 ? 0 : 1]
                        [bytes.get(5) == 0 ? 0 : 1] != null) System.out.println("AHHHHHHHHH");

                types[bytes.get(0) == 0 ? 0 : 1]
                        [bytes.get(1) == 0 ? 0 : 1]
                        [bytes.get(2) == 0 ? 0 : 1]
                        [bytes.get(3) == 0 ? 0 : 1]
                        [bytes.get(4) == 0 ? 0 : 1]
                        [bytes.get(5) == 0 ? 0 : 1] = t;
            });
        }

        System.out.println("{");
        for (int i = 0; i < 2; i++) {
            System.out.println(" {");
            for (int j = 0; j < 2; j++) {
                System.out.println("  {");
                for (int k = 0; k < 2; k++) {
                    System.out.println("   {");
                    for (int l = 0; l < 2; l++) {
                        System.out.println("    {");
                        for (int m = 0; m < 2; m++) {
                            System.out.println("     {");
                            for (int n = 0; n < 2; n++) {
                                System.out.println("      " + types[i][j][k][l][m][n] + ",");
                            }
                            System.out.println("     },");
                        }
                        System.out.println("    },");
                    }
                    System.out.println("   },");
                }
                System.out.println("  },");
            }
            System.out.println(" },");
        }
        System.out.println("};");

        return types;
    }

    public static Double[][][] generateData() {
        HashCubeSorter s = CubeSorterFactory.makeHashCubeSorter(new ICube[]{});

        // For all cube types
        for (CubeType t : CubeType.values()) {
            // For all possible cubes of that type:
            forAllPermutations(t.predicate, f -> {
                if(s.unique(f) == 0) { // No cube for this.
                    addCube(s, f);
                }
            });

            for (Orientation o : Orientation.getValues()) {
                ICubeFilter c = new ByteCubeFilter((ByteCubeFilter) t.predicate, o);
                forAllPermutations(c, f -> {
                    if(s.unique(f) != 1) { // Check that everyone does have exactly one result
                        System.out.printf("Got more than one candidates for filter %s\n", f);
                    }
                });
            }
        }

        ICube[] cubes = get(s);
        int size = cubes.length;
        Double[][][] data = new Double[size][24][6];
        //System.out.println("CubeType " + t + " with " + s.getNumCachedQueries() + " permutations needs " + cubes.length + " unique cubes:");
        for (int c = 0; c < size; c++) {
            for (int o = 0; o < 24; o++) {
                var tmp = cubes[c].getTriangles(Orientation.get(o));
                for (int j = 0; j < 6; j++) {
                    data[c][o][j] = (double) tmp[j];
                }

            }
        }

        return data;
    }

    private static final Triangle[] none = {Triangle.None};
    private static final Triangle[] all = {Triangle.BottomLeft, Triangle.TopLeft, Triangle.TopRight, Triangle.BottomRight};
    private static Triangle[] getPossibilities(ICubeFilter f, ICube.Side s) {
         return f.getSide(s) == Triangle.None ? none : all;
    }


    private static Field fieldCS_given, fieldCS_queries;
    static {
        try {
            fieldCS_given = HashCubeSorter.class.getDeclaredField("given");
            fieldCS_given.setAccessible(true);
            fieldCS_queries = HashCubeSorter.class.getDeclaredField("queries");
            fieldCS_queries.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    private static ICube[] get(HashCubeSorter s) {
        try {
            return (ICube[]) fieldCS_given.get(s);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    // Dear god why do I do this
    private static void addCube(HashCubeSorter s, ICubeFilter mold) {
        try {
            ICube[] newCubes = new ICube[s.getNumCubes()+1];
            ICube newCube = new CachedCube(s.getNumCubes()+1,
                    mold.getSide(ICube.Side.Up), mold.getSide(ICube.Side.Left),
                    mold.getSide(ICube.Side.Front), mold.getSide(ICube.Side.Right),
                    mold.getSide(ICube.Side.Back), mold.getSide(ICube.Side.Down));
            ICube[] oldCubes = (ICube[]) fieldCS_given.get(s);
            System.arraycopy(oldCubes, 0, newCubes, 0, s.getNumCubes());
            newCubes[s.getNumCubes()] = newCube;
            fieldCS_given.set(s, newCubes);

            ((HashMap<?, ?>)fieldCS_queries.get(s)).clear();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private static void forAllPermutations(ICubeFilter f, Consumer<ICubeFilter> consoomer) {
        for(Triangle up : getPossibilities(f, ICube.Side.Up)) {
            for(Triangle left : getPossibilities(f, ICube.Side.Left)) {
                for(Triangle front : getPossibilities(f, ICube.Side.Front)) {
                    for(Triangle right : getPossibilities(f, ICube.Side.Right)) {
                        for(Triangle back : getPossibilities(f, ICube.Side.Back)) {
                            for(Triangle down : getPossibilities(f, ICube.Side.Down)) {
                                consoomer.accept(CubeFilterFactory.from(up, left, front, right, back, down));
                            }
                        }
                    }
                }
            }
        }
    }
}
