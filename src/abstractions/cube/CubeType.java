package abstractions.cube;

import implementation.cube.filter.CubeFilterFactory;

/**
 * An enum over all possible types a cube can have, in terms of "triangle/no triangle"
 */
public enum CubeType {
    /**
     *      + -- +
     *      |    |
     * + -- + -- + -- + -- +
     * |    |    |    |    |
     * + -- + -- + -- + -- +
     *      |    |
     *      + -- +
     */
    Zero(Triangle.None, Triangle.None, Triangle.None, Triangle.None, Triangle.None, Triangle.None),
    /**
     *      + -- +
     *      | ## |
     * + -- + -- + -- + -- +
     * |    |    |    |    |
     * + -- + -- + -- + -- +
     *      |    |
     *      + -- +
     */
    One(Triangle.AnyNotNone, Triangle.None, Triangle.None, Triangle.None, Triangle.None, Triangle.None),
    /**
     *      + -- +
     *      | ## |
     * + -- + -- + -- + -- +
     * | ## |    |    |    |
     * + -- + -- + -- + -- +
     *      |    |
     *      + -- +
     */
    TwoConnected(Triangle.AnyNotNone, Triangle.AnyNotNone, Triangle.None, Triangle.None, Triangle.None, Triangle.None),
    /**
     *      + -- +
     *      |    |
     * + -- + -- + -- + -- +
     * | ## |    | ## |    |
     * + -- + -- + -- + -- +
     *      |    |
     *      + -- +
     */
    TwoOpposite(Triangle.None, Triangle.AnyNotNone, Triangle.None, Triangle.AnyNotNone, Triangle.None, Triangle.None),
    /**
     *      + -- +
     *      | ## |
     * + -- + -- + -- + -- +
     * | ## | ## |    |    |
     * + -- + -- + -- + -- +
     *      |    |
     *      + -- +
     */
    ThreeEdge(Triangle.AnyNotNone, Triangle.AnyNotNone, Triangle.AnyNotNone, Triangle.None, Triangle.None, Triangle.None),
    /**
     *      + -- +
     *      |    |
     * + -- + -- + -- + -- +
     * | ## | ## | ## |    |
     * + -- + -- + -- + -- +
     *      |    |
     *      + -- +
     */
    ThreeConnected(Triangle.None, Triangle.AnyNotNone, Triangle.AnyNotNone, Triangle.AnyNotNone, Triangle.None, Triangle.None),
    /**
     *      + -- +
     *      |    |
     * + -- + -- + -- + -- +
     * | ## | ## | ## | ## |
     * + -- + -- + -- + -- +
     *      |    |
     *      + -- +
     */
    FourRound(Triangle.None, Triangle.AnyNotNone, Triangle.AnyNotNone, Triangle.AnyNotNone, Triangle.AnyNotNone, Triangle.None),
    /**
     *      + -- +
     *      | ## |
     * + -- + -- + -- + -- +
     * | ## | ## | ## |    |
     * + -- + -- + -- + -- +
     *      |    |
     *      + -- +
     */
    FourConnected(Triangle.AnyNotNone, Triangle.AnyNotNone, Triangle.AnyNotNone, Triangle.AnyNotNone, Triangle.None, Triangle.None),
    /**
     *      + -- +
     *      | ## |
     * + -- + -- + -- + -- +
     * | ## | ## | ## |    |
     * + -- + -- + -- + -- +
     *      | ## |
     *      + -- +
     */
    Five(Triangle.AnyNotNone, Triangle.AnyNotNone, Triangle.AnyNotNone, Triangle.AnyNotNone, Triangle.AnyNotNone, Triangle.None),
    /**
     *      + -- +
     *      | ## |
     * + -- + -- + -- + -- +
     * | ## | ## | ## | ## |
     * + -- + -- + -- + -- +
     *      | ## |
     *      + -- +
     */
    Six(Triangle.AnyNotNone, Triangle.AnyNotNone, Triangle.AnyNotNone, Triangle.AnyNotNone, Triangle.AnyNotNone, Triangle.AnyNotNone);

    public final ICubeFilter predicate;
    public final int triangles;

    CubeType(Triangle... filter) {
        this.predicate = CubeFilterFactory.from(filter);
        this.triangles = this.predicate.getNumTriangle();
    }

    /** See {@literal  tooling.PrimitiveCubeSearch.generateFilterToTypeMapping()} */
    private static final CubeType[][][][][][] types = {{{{{{Zero, One,}, {One, TwoConnected,},}, {{One, TwoConnected,}, {TwoConnected, ThreeEdge,},},}, {{{One, TwoConnected,}, {TwoOpposite, ThreeConnected,},}, {{TwoConnected, ThreeEdge,}, {ThreeConnected, FourConnected,},},},}, {{{{One, TwoConnected,}, {TwoConnected, ThreeEdge,},}, {{TwoOpposite, ThreeConnected,}, {ThreeConnected, FourConnected,},},}, {{{TwoConnected, ThreeEdge,}, {ThreeConnected, FourConnected,},}, {{ThreeConnected, FourConnected,}, {FourRound, Five,},},},},}, {{{{{One, TwoOpposite,}, {TwoConnected, ThreeConnected,},}, {{TwoConnected, ThreeConnected,}, {ThreeEdge, FourConnected,},},}, {{{TwoConnected, ThreeConnected,}, {ThreeConnected, FourRound,},}, {{ThreeEdge, FourConnected,}, {FourConnected, Five,},},},}, {{{{TwoConnected, ThreeConnected,}, {ThreeEdge, FourConnected,},}, {{ThreeConnected, FourRound,}, {FourConnected, Five,},},}, {{{ThreeEdge, FourConnected,}, {FourConnected, Five,},}, {{FourConnected, Five,}, {Five, Six,},},},},},};
    /**
     * Returns the CubeType that all cubes matching the given filter belong into.
     */
    public static CubeType get(byte... filter) {
        // use (filter[i]+2)%(filter[i]+1) instead of (filter[i] == 0 ? 0 : 1) ?
        return types[filter[0] == 0 ? 0 : 1][filter[1] == 0 ? 0 : 1][filter[2] == 0 ? 0 : 1][filter[3] == 0 ? 0 : 1][filter[4] == 0 ? 0 : 1][filter[5] == 0 ? 0 : 1];
    }
}