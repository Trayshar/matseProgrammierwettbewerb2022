package tooling;

import abstractions.Orientation;
import abstractions.cube.CubeType;
import abstractions.cube.ICube;
import abstractions.cube.ICubeFilter;
import abstractions.cube.Triangle;
import implementation.cube.CachedCube;
import implementation.cube.CubeSorter;
import implementation.cube.filter.ByteCubeFilter;
import implementation.cube.filter.CubeFilterFactory;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.function.Consumer;

public class PrimitiveCubeSearch {

    public static void main(String[] args) {
        // For all cube types
        for (CubeType t : CubeType.values()) {
            CubeSorter s = new CubeSorter(new ICube[]{});

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
            System.out.println("CubeType " + t + " with " + s.getSize() + " permutations needs " + cubes.length + " unique cubes:");
            for (ICube c : cubes) {
                System.out.println(c.serialize());
            }
            System.out.println("----------");
        }
    }

    private static final Triangle[] none = {Triangle.None};
    private static final Triangle[] all = {Triangle.BottomLeft, Triangle.TopLeft, Triangle.TopRight, Triangle.BottomRight};
    private static Triangle[] getPossibilities(ICubeFilter f, ICube.Side s) {
         return f.getSide(s) == Triangle.None ? none : all;
    }


    private static Field fieldCS_given, fieldCS_queries;
    static {
        try {
            fieldCS_given = CubeSorter.class.getDeclaredField("given");
            fieldCS_given.setAccessible(true);
            fieldCS_queries = CubeSorter.class.getDeclaredField("queries");
            fieldCS_queries.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    private static ICube[] get(CubeSorter s) {
        try {
            return (ICube[]) fieldCS_given.get(s);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    // Dear god why do I do this
    private static void addCube(CubeSorter s, ICubeFilter mold) {
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
