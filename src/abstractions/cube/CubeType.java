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
}