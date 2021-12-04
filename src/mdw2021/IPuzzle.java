package mdw2021;

public interface IPuzzle {

	// Implement this function in a way that reads the input data with the given path/filename in the
	// official input data format and imports it to your internal data structures
	public void readInput(String filename);

	// solve the puzzle defined by the input data
	public void solve();

	// This function shall return false, before a call of solve function.
	// It shall return true, if solve was called and there actually is a solution.
	// Otherwise false.
	public boolean hasSolution();

	// This function only is called if hasSolution() returns true.
	// When called it shall write a solution to the output file with the given path/filename
	// in the official output data format.
	public void writeResult(String filename);
}
