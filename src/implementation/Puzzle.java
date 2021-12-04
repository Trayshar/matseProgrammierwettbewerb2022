package implementation;

// see ../mdw2021/IPuzzle.java for comments about implementation of functions below
import mdw2021.IPuzzle;

public class Puzzle implements IPuzzle {
	
	public Puzzle() {		
	}

	public void readInput(String filename) {
	}

	public void solve() {
		// just simulating some compute time
		// can be removed after implementation 
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
		}
	}

	public boolean hasSolution() {
		return false;
	}

	public void writeResult(String filename) {
	}

}
