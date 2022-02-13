import numpy as np
import itertools
from pyquaternion import Quaternion

# https://k-l-lambda.github.io/2020/12/14/rubik-cube-notation/
greekLetters = ["Alpha", "Beta", "Gamma", "Delta", "Epsilon", "Zeta", "Eta", "Theta", "Iota", "Kappa", "Lambda", "Mu",
                "Nu", "Xi", "Omicron", "Pi", "Rho", "Sigma", "Tau", "Upsilon", "Phi", "Chi", "Psi", "Omega"]

# Z    Y
# ^   /
# |  /         The cube (side length 1) is in the center of this coordinate system
# | /
# |/
# +---------------> X

# Triangle number: 1: ◣                2: ◤                3: ◥               4: ◢                 # Face direction
cubeTriangles = [[(-0.25, -0.25, 1),  (-0.25, 0.25, 1),   (0.25, 0.25, 1),   (0.25, -0.25, 1)],    # Up
                 [(-1, 0.25, -0.25),  (-1, 0.25, 0.25),   (-1, -0.25, 0.25), (-1, -0.25, -0.25)],  # Left
                 [(-0.25, -1, -0.25), (-0.25, -1, 0.25),  (0.25, -1, 0.25),  (0.25, -1, -0.25)],   # Front
                 [(1, -0.25, -0.25),  (1, -0.25, 0.25),   (1, 0.25, 0.25),   (1, 0.25, -0.25)],    # Right
                 [(0.25, 1, -0.25),   (0.25, 1, 0.25),    (-0.25, 1, 0.25),  (-0.25, 1, -0.25)],   # Back
                 [(-0.25, 0.25, -1),  (-0.25, -0.25, -1), (0.25, -0.25, -1), (0.25, 0.25, -1)]]    # Down


# Gets a list of all possible rotations
# https://stackoverflow.com/a/70755389
def rotations():
    for x, y, z in itertools.permutations([0, 1, 2]):
        for sx, sy, sz in itertools.product([1, -1], repeat=3):
            rotation_matrix = np.zeros((3, 3))
            rotation_matrix[0, x] = sx
            rotation_matrix[1, y] = sy
            rotation_matrix[2, z] = sz
            if np.linalg.det(rotation_matrix) == 1:
                quat = Quaternion(matrix=rotation_matrix)
                yield quat


# Returns where the given rotation puts the sides of our cube and how the triangle on each side gets affected.
# The idea is to rotate the default triangle for the given rotation on all 6 sides
# and then look where the triangle is pointing to calculate the offset.
# Also, we're gonna hardcode that instead of messing with quaternions in java ourselves. Also saves performance
def get_rotation_data(quat):
    new_side_positions = [None] * 6
    new_triangle_offset = [None] * 6
    raw = [None] * 6

    for i in range(0, 6):
        original = cubeTriangles[i][0]
        rotated = round_vec(quat.rotate(original))

        if rotated[2] == 1:
            index = 0
        elif rotated[0] == -1:
            index = 1
        elif rotated[1] == -1:
            index = 2
        elif rotated[0] == 1:
            index = 3
        elif rotated[1] == 1:
            index = 4
        elif rotated[2] == -1:
            index = 5
        new_side_positions[i] = index
        new_triangle_offset[i] = cubeTriangles[index].index(rotated)
        raw[i] = rotated
    return new_side_positions, new_triangle_offset, raw


# Small helper to round a tuple
def round_vec(vec):
    return tuple(map(lambda x: isinstance(x, float) and round(x, 2) or x, vec))


if __name__ == '__main__':
    all_rotations = list(rotations())  # Indexed list of rotations
    rotation_data = list()  # Holds the data we care about, indexed by the above
    rotation_multiplication = [[-1] * 24 for _ in range(24)]  # Hold how two given rotations multiply

    print("Raw data: ")
    letterIndex = 0
    for quat in all_rotations:
        letter = greekLetters[letterIndex]
        data = get_rotation_data(quat)
        print(round_vec(quat.elements), data[2])
        rotation_data.append((letter, data[0], data[1], round_vec(quat.elements)))
        letterIndex += 1

    print("Calc rotations: ")
    for a in range(0, 24):
        for b in range(0, 24):
            data = all_rotations[a] * all_rotations[b]
            if all_rotations.__contains__(data):
                tmp = all_rotations.index(data)
                rotation_multiplication[a][b] = tmp
            else:
                print(str(round_vec(data.elements)) + " = " + str(round_vec(all_rotations[a].elements)) + " * " + str(round_vec(all_rotations[b].elements)))
                rotation_multiplication[a][b] = all_rotations.index(-data)

    print("Enum definitions: ")
    for enum in rotation_data:
        quat = enum[3]
        print(f'/** Quaternion: {quat[0]} {"-" if quat[1] < 0 else "+"} {abs(quat[1])}i {"-" if quat[2] < 0 else "+"} {abs(quat[2])}j {"-" if quat[3] < 0 else "+"} {abs(quat[3])}k */')
        print(f'{enum[0]}(new byte[]{"{" + ", ".join(map(str, enum[1])) + "}"}, new byte[]{"{" + ", ".join(map(str, enum[2])) + "}"}),')

    print("Enum multiplication, From \ To: ")
    for rot in rotation_multiplication:
        print(str(rot)[1:-1] + ",")

# Other sources:
# https://math.stackexchange.com/questions/4379507/how-to-get-all-possible-rotations-of-the-cube-represented-by-matrix-with-data
# https://danceswithcode.net/engineeringnotes/quaternions/quaternions.html
# https://www.cs.utexas.edu/~theshark/courses/cs354/lectures/cs354-14.pdf
