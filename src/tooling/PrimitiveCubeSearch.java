package tooling;

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

public class PrimitiveCubeSearch {

    public static void main(String[] args) {
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
                    if(s.unique(f) != 1) { // Check that everyone does have exactly one result
                        System.out.printf("Got more than one candidates for filter %s\n", f);
                    }
                });
            }

            ICube[] cubes = get(s);
            System.out.println("CubeType " + t + " with " + s.getNumCachedQueries() + " permutations needs " + cubes.length + " unique cubes:");
            for (ICube c : cubes) {
                Orientation orientation = null;
                int id = getUniqueID(c);
                for (Orientation o : Orientation.getValues()) {
                    c.setOrientation(o);
                    int nid = getUniqueID(c);
                    if(nid < id) {
                        id = nid;
                        orientation = o;
                        //System.out.printf("I'm feeling violated! %s with %d didnt match in %s (%d)\n", c, id, o, nid);
                        //System.exit(-1);
                    }
                }
                System.out.printf("[%d][%s] %s\n", id, orientation, c.serialize());
                if(ids.contains(id)) {
                    System.out.println("Violation! ID " + id + "already used!");
                    System.exit(-1);
                }
                ids.add(id);
            }
            System.out.println("----------");
        }
        System.out.println(ids.stream().max(Integer::compareTo).get());
    }

    private static int getUniqueID(ICube cube) {
        byte[] sides = cube.getTriangles();
        return sides[0] +
                sides[1] * 5 +
                sides[2] * 25 +
                sides[3] * 125 +
                sides[4] * 625 +
                sides[5] * 3125;
    }

    private static int getUniqueIDOld(ICube cube) {
        byte[] tmp = cube.getTriangles();
        int[] tmp2 = {
                (getDifference(tmp[0] - tmp[1]) + getDifference(tmp[0] - tmp[2]) + getDifference(tmp[1] - tmp[2])),
                (getDifference(tmp[0] - tmp[2]) + getDifference(tmp[0] - tmp[3]) + getDifference(tmp[2] - tmp[3])),
                (getDifference(tmp[0] - tmp[3]) + getDifference(tmp[0] - tmp[4]) + getDifference(tmp[3] - tmp[4])),
                (getDifference(tmp[0] - tmp[4]) + getDifference(tmp[0] - tmp[1]) + getDifference(tmp[4] - tmp[1])),
                (getDifference(tmp[5] - tmp[1]) + getDifference(tmp[5] - tmp[2]) + getDifference(tmp[1] - tmp[2])),
                (getDifference(tmp[5] - tmp[2]) + getDifference(tmp[5] - tmp[3]) + getDifference(tmp[2] - tmp[3])),
                (getDifference(tmp[5] - tmp[3]) + getDifference(tmp[5] - tmp[4]) + getDifference(tmp[3] - tmp[4])),
                (getDifference(tmp[5] - tmp[4]) + getDifference(tmp[5] - tmp[1]) + getDifference(tmp[4] - tmp[1]))
        };
        Arrays.sort(tmp2);
        //System.out.print(Arrays.toString(tmp2));
        int result = 0;
        for (int i = 0; i < 8; i++) {
            result += tmp2[i] << i*3;
        }
        return result;
    }

    private static int getDifference(int ab) {
        if(ab < 0) {
            return 4 + ab;
        }else if(ab > 4) {
            return ab - 4;
        }
        return ab;
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
