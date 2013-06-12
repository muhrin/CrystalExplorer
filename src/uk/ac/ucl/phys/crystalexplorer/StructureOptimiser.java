package uk.ac.ucl.phys.crystalexplorer;

public class StructureOptimiser implements Runnable {
	
	public StructureOptimiser(
			String outFile,
			int numAtoms,
			int atomNumbers[],
			double[] atomSizes,
			double[] atomStrengths) {
		myOutput = new String();
		myOutFile = outFile;
		myNumAtoms = numAtoms;
		myAtomNumbers = atomNumbers.clone();
		myAtomSizes = atomSizes.clone();
		myAtomStrengths = atomStrengths.clone();
	}

	@Override
	public void run() {
		myOutput = NdkCrystalExplorer.generateStructure(myOutFile, myNumAtoms, myAtomNumbers, myAtomSizes, myAtomStrengths);
	}
	
	public String getOutput() {
		return myOutput;
	}

	private final String myOutFile;
	private final int myNumAtoms;
	private final int[] myAtomNumbers;
	private final double[] myAtomSizes;
	private final double[] myAtomStrengths;
	private String myOutput;
}
