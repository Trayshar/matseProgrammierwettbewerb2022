package tooling;

import javax.swing.*;

public class Benchmark {
    public static void main(String[] args) {
        if(args.length > 0) {
            int x = 0, y = 0, z = 0, runs = 1;
            boolean shuffled = false, rotated = false;
            for (String s : args) {
                if(s.startsWith("-x")) x = Integer.parseInt(s.substring(2));
                else if(s.startsWith("-y")) y = Integer.parseInt(s.substring(2));
                else if(s.startsWith("-z")) z = Integer.parseInt(s.substring(2));
                else if(s.startsWith("-shuffled")) shuffled = true;
                else if(s.startsWith("-rotated")) rotated = true;
                else if(s.startsWith("-runs")) runs = Integer.parseInt(s.substring(5));
                else {
                    System.err.printf("Unknown argument \"%s\"\n", s);
                }
            }

            double t = 0d;
            for (int i = 0; i < runs; i++) {
                t += Generator.doTesting(x, y, z, shuffled, rotated);
            }
            if(runs > 1) {
                t /= runs;
                System.out.printf("Average runtime was %f seconds!", t);
            }

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
