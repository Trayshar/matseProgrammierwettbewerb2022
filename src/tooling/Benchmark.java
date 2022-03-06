package tooling;

import implementation.Puzzle;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

public class Benchmark {
    public static void main(String[] args) throws FileNotFoundException {
        if(args.length > 0) {
            int x = 0, y = 0, z = 0, runs = 1, maxX = 0, maxY = 0, maxZ = 0, timeout = 30;
            String name = "";
            boolean shuffled = false, rotated = false;
            File inputFolder = null;
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
                else if(s.startsWith("-folder=")) inputFolder = new File(s.substring(8));
                else {
                    System.err.printf("Unknown argument \"%s\"\n", s);
                }
            }

            double t = 0d;
            if(inputFolder != null) {
                if(!inputFolder.exists() || !inputFolder.isDirectory()) throw new FileNotFoundException();

                runs = 1;
                try (FileWriter fw = new FileWriter("result_files/" + inputFolder.getName() + "_" + name + "_" + System.currentTimeMillis() + ".csv")) {
                    fw.write("time,file\n");

                    for(File f : inputFolder.listFiles((file, s) -> s.endsWith(".txt"))) {
                        Generator.clearArrayCubeSorterCache();
                        Puzzle p = new Puzzle();
                        p.readInput(f.getAbsolutePath());

                        double deltaT = Generator.doTesting(p.dimensionX, p.dimensionY, p.dimensionZ, p.cubes, timeout, f.getAbsolutePath());

                        t += deltaT;
                        System.out.println("Took " + deltaT + " seconds!");
                        fw.write(deltaT + "," + f.getName() + "\n");
                        fw.flush();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if(maxX != 0) {
                try (FileWriter fw = new FileWriter("result_files/benchmark_" + name + "_" + System.currentTimeMillis() + ".csv")) {
                    fw.write("x,y,z,run,time,cubes\n");
                    for (x = 1; x <= maxX; x++) {
                        for (y = 1; y <= maxY; y++) {
                            for (z = 1; z <= maxZ; z++) {
                                double deltaT = 0d;
                                for (int i = 0; i < runs; i++) {
                                    if(i > runs/2 && Math.round(deltaT/i) >= timeout) { // These keep timing out, so we skip them
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
