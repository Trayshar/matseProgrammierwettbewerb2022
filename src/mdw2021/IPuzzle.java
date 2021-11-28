package mdw2021;

public interface IPuzzle {
	public void readInput(String filename);
	public void solve();
	public boolean hasSolution();
	public void writeResult(String filename);
}
