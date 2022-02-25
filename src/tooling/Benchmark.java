package tooling;

import java.io.FileWriter;
import java.io.IOException;

public class Benchmark {
    public static void main(String[] args) {
        if(args.length > 0) {
            int x = 0, y = 0, z = 0, runs = 1, maxX = 0, maxY = 0, maxZ = 0, timeout = 30;
            String name = "";
            boolean shuffled = false, rotated = false;
            for (String s : args) {
                if(s.startsWith("-x=")) x = Integer.parseInt(s.substring(3));
                else if(s.startsWith("-y=")) y = Integer.parseInt(s.substring(3));
                else if(s.startsWith("-z=")) z = Integer.parseInt(s.substring(3));
                else if(s.startsWith("-maxX=")) maxX = Integer.parseInt(s.substring(6));
                else if(s.startsWith("-maxY=")) maxY = Integer.parseInt(s.substring(6));
                else if(s.startsWith("-maxZ=")) maxZ = Integer.parseInt(s.substring(6));
                else if(s.startsWith("-shuffled")) shuffled = true;
                else if(s.startsWith("-rotated")) rotated = true;
                else if(s.startsWith("-runs=")) runs = Integer.parseInt(s.substring(6));
                else if(s.startsWith("-name=")) name = s.substring(6);
                else if(s.startsWith("-timeout=")) timeout = Integer.parseInt(s.substring(9));
                else {
                    System.err.printf("Unknown argument \"%s\"\n", s);
                }
            }

            double t = 0d;
            if(maxX != 0) {
                try (FileWriter fw = new FileWriter("result_files/benchmark_" + name + "_" + System.currentTimeMillis() + ".csv")) {
                    fw.write("x,y,z,run,time,cubes\n");
                    for (x = 1; x <= maxX; x++) {
                        for (y = 1; y <= maxY; y++) {
                            for (z = 1; z <= maxZ; z++) {
                                double deltaT = 0d;
                                for (int i = 0; i < runs; i++) {
                                    if(i > runs/2 && deltaT*i == timeout) { // These keep timing out, so we skip them
                                        deltaT += (runs-i) * timeout; // Adding skipped time
                                        System.out.println("Skipping " + (runs-i) + " runs due to timeout!");
                                        break;
                                    }
                                    double tmp = Generator.doTesting(x, y, z, shuffled, rotated, timeout);
                                    deltaT += tmp;
                                    fw.write(x + "," + y + "," + z + "," + i + "," + tmp + "," + x*y*z + "\n");
                                }
                                t += deltaT;
                                deltaT /= runs;
                                System.out.printf("Average runtime of [%d,%d,%d] was %f seconds!\n", x, y, z, deltaT);
                                fw.flush();
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else {
                for (int i = 0; i < runs; i++) {
                    t += Generator.doTesting(x, y, z, shuffled, rotated, timeout);
                }
            }

            t /= runs;
            System.out.printf("Average runtime was %f seconds!\n", t);
            Generator.shutdown(); // Apparently executors without any task still keep the programm alive after main() returned. Because ... reasons.
        }
    }
}
