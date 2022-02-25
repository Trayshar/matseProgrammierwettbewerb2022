package tooling;

import javax.swing.*;
import java.io.FileWriter;
import java.io.IOException;

public class Benchmark {
    public static void main(String[] args) {
        if(args.length > 0) {
            int x = 0, y = 0, z = 0, runs = 1, maxX = 0, maxY = 0, maxZ = 0;
            boolean shuffled = false, rotated = false;
            for (String s : args) {
                if(s.startsWith("-x")) x = Integer.parseInt(s.substring(2));
                else if(s.startsWith("-y")) y = Integer.parseInt(s.substring(2));
                else if(s.startsWith("-z")) z = Integer.parseInt(s.substring(2));
                else if(s.startsWith("-maxX")) maxX = Integer.parseInt(s.substring(5));
                else if(s.startsWith("-maxY")) maxY = Integer.parseInt(s.substring(5));
                else if(s.startsWith("-maxZ")) maxZ = Integer.parseInt(s.substring(5));
                else if(s.startsWith("-shuffled")) shuffled = true;
                else if(s.startsWith("-rotated")) rotated = true;
                else if(s.startsWith("-runs")) runs = Integer.parseInt(s.substring(5));
                else {
                    System.err.printf("Unknown argument \"%s\"\n", s);
                }
            }

            double t = 0d;
            if(maxX != 0) {
                try (FileWriter fw = new FileWriter("result_files/benchmark_" + System.currentTimeMillis() + ".csv")) {
                    fw.write("x,y,z,run,time,cubes\n");
                    for (x = 1; x <= maxX; x++) {
                        for (y = 1; y <= maxY; y++) {
                            for (z = 1; z <= maxZ; z++) {
                                double deltaT = 0d;
                                for (int i = 0; i < runs; i++) {
                                    double tmp = Generator.doTesting(x, y, z, shuffled, rotated);
                                    deltaT += tmp;
                                    fw.write(x + "," + y + "," + z + "," + i + "," + tmp + "," + x*y*z + "\n");
                                }
                                t += deltaT;
                                deltaT /= runs;
                                System.out.printf("Average runtime of [%d,%d,%d] was %f seconds!\n", x, y, z, deltaT);
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else {
                for (int i = 0; i < runs; i++) {
                    t += Generator.doTesting(x, y, z, shuffled, rotated);
                }
            }

            t /= runs;
            System.out.printf("Average runtime was %f seconds!\n", t);
            return;
        }


        JTextField xField = new JTextField(5);
        JTextField yField = new JTextField(5);
        JTextField zField = new JTextField(5);
        JCheckBox shuffleField = new JCheckBox();
        JCheckBox rotateField = new JCheckBox();

        JPanel myPanel = new JPanel();
        myPanel.add(new JLabel("Dimensions:"));
        myPanel.add(xField);
        myPanel.add(yField);
        myPanel.add(zField);
        myPanel.add(new JLabel("Shuffle:"));
        myPanel.add(shuffleField);
        myPanel.add(new JLabel("Rotate:"));
        myPanel.add(rotateField);
        xField.requestFocusInWindow();

        int result = JOptionPane.showConfirmDialog(null, myPanel,
                "Please configure run options...", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {

            Generator.doTesting(Integer.parseInt(xField.getText()),
                    Integer.parseInt(yField.getText()),
                    Integer.parseInt(zField.getText()), shuffleField.isSelected(), rotateField.isSelected());
        } else {
            System.out.println("Canceled");
        }
    }
}
