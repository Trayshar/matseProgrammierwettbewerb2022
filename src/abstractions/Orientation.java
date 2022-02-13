package abstractions;

import java.util.Arrays;
import java.util.stream.Stream;

/**
 * Orientation based on Quaternions. All orientations are relative to the identity orientation
 */
public enum Orientation{
    /**
     * Identity orientation.
     * <br>
     * Quaternion: 1.0 + 0.0i + 0.0j + 0.0k
     */
    Alpha(new byte[]{0, 1, 2, 3, 4, 5}, new byte[]{0, 0, 0, 0, 0, 0}),
    /** Quaternion: 0.0 + 1.0i + 0.0j + 0.0k */
    Beta(new byte[]{5, 1, 4, 3, 2, 0}, new byte[]{0, 2, 2, 2, 2, 0}),
    /** Quaternion: 0.0 + 0.0i + 1.0j + 0.0k */
    Gamma(new byte[]{5, 3, 2, 1, 4, 0}, new byte[]{2, 2, 2, 2, 2, 2}),
    /** Quaternion: 0.0 + 0.0i + 0.0j + 1.0k */
    Delta(new byte[]{0, 3, 4, 1, 2, 5}, new byte[]{2, 0, 0, 0, 0, 2}),
    /** Quaternion: 0.71 - 0.71i + 0.0j + 0.0k */
    Epsilon(new byte[]{4, 1, 0, 3, 5, 2}, new byte[]{2, 3, 0, 1, 2, 0}),
    /** Quaternion: 0.71 + 0.71i + 0.0j + 0.0k */
    Zeta(new byte[]{2, 1, 5, 3, 0, 4}, new byte[]{0, 1, 0, 3, 2, 2}),
    /** Quaternion: 0.0 + 0.0i + 0.71j + 0.71k */
    Eta(new byte[]{4, 3, 5, 1, 0, 2}, new byte[]{0, 1, 2, 3, 0, 2}),
    /** Quaternion: 0.0 + 0.0i - 0.71j + 0.71k */
    Theta(new byte[]{2, 3, 0, 1, 5, 4}, new byte[]{2, 3, 2, 1, 0, 0}),
    /** Quaternion: 0.0 + 0.71i + 0.71j + 0.0k */
    Iota(new byte[]{5, 2, 1, 4, 3, 0}, new byte[]{1, 2, 2, 2, 2, 3}),
    /** Quaternion: 0.71 + 0.0i + 0.0j - 0.71k */
    Kappa(new byte[]{0, 4, 1, 2, 3, 5}, new byte[]{1, 0, 0, 0, 0, 3}),
    /** Quaternion: 0.71 + 0.0i + 0.0j + 0.71k */
    Lambda(new byte[]{0, 2, 3, 4, 1, 5}, new byte[]{3, 0, 0, 0, 0, 1}),
    /** Quaternion: 0.0 - 0.71i + 0.71j + 0.0k */
    Mu(new byte[]{5, 4, 3, 2, 1, 0}, new byte[]{3, 2, 2, 2, 2, 1}),
    /** Quaternion: 0.5 - 0.5i - 0.5j - 0.5k */
    Nu(new byte[]{4, 5, 1, 0, 3, 2}, new byte[]{3, 2, 3, 0, 1, 3}),
    /** Quaternion: 0.5 + 0.5i + 0.5j - 0.5k */
    Xi(new byte[]{2, 0, 1, 5, 3, 4}, new byte[]{1, 2, 1, 0, 3, 1}),
    /** Quaternion: 0.5 - 0.5i + 0.5j + 0.5k */
    Omicron(new byte[]{4, 0, 3, 5, 1, 2}, new byte[]{1, 0, 1, 2, 3, 1}),
    /** Quaternion: 0.5 + 0.5i - 0.5j + 0.5k */
    Pi(new byte[]{2, 5, 3, 0, 1, 4}, new byte[]{3, 0, 3, 2, 1, 3}),
    /** Quaternion: 0.5 + 0.5i + 0.5j + 0.5k */
    Rho(new byte[]{3, 2, 5, 4, 0, 1}, new byte[]{0, 1, 1, 3, 1, 2}),
    /** Quaternion: 0.5 - 0.5i + 0.5j - 0.5k */
    Sigma(new byte[]{3, 4, 0, 2, 5, 1}, new byte[]{2, 3, 1, 1, 1, 0}),
    /** Quaternion: 0.5 - 0.5i - 0.5j + 0.5k */
    Tau(new byte[]{1, 2, 0, 4, 5, 3}, new byte[]{2, 3, 3, 1, 3, 0}),
    /** Quaternion: 0.5 + 0.5i - 0.5j - 0.5k */
    Upsilon(new byte[]{1, 4, 5, 2, 0, 3}, new byte[]{0, 1, 3, 3, 3, 2}),
    /** Quaternion: 0.71 + 0.0i + 0.71j + 0.0k */
    Phi(new byte[]{3, 0, 2, 5, 4, 1}, new byte[]{1, 1, 1, 1, 3, 1}),
    /** Quaternion: 0.0 + 0.71i + 0.0j + 0.71k */
    Chi(new byte[]{3, 5, 4, 0, 2, 1}, new byte[]{3, 1, 3, 1, 1, 3}),
    /** Quaternion: 0.71 + 0.0i - 0.71j + 0.0k */
    Psi(new byte[]{1, 5, 2, 0, 4, 3}, new byte[]{3, 3, 3, 3, 1, 3}),
    /** Quaternion: 0.0 - 0.71i + 0.0j + 0.71k */
    Omega(new byte[]{1, 0, 4, 5, 2, 3}, new byte[]{1, 3, 1, 3, 3, 1});

    /** Where each side of the cube is after applying this orientation */
    public final byte[] side;

    /** How the triangles on each side are affected. Just add this */
    public final byte[] triangleOffset;

    Orientation(byte[] side, byte[] triangleOffset) {
        this.side = side;
        this.triangleOffset = triangleOffset;
    }

    public static Stream<Orientation> stream() {
        return Arrays.stream(values());
    }

    /**
     * Enum.values() clones the array to stop modification of the enum (God, I sometimes hate this langauge).
     * We need performance, so we don't care about safety.
     */
    private static final Orientation[] values = values();

    /**
     * Faster brother of Enum.values(). DO NOT MODIFY THE VALUES RETURNED
     */
    public static Orientation[] getValues() {
        return values;
    }

    /**
     * Returns the orientation for the given ordinal. DO NOT MODIFY THE VALUE RETURNED
     */
    public static Orientation get(int ordinal) {
        assert ordinal > 0 && ordinal < 24;

        return values[ordinal];
    }

    //TODO: Implement rotation
}
