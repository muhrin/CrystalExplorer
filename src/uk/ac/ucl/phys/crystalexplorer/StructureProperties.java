package uk.ac.ucl.phys.crystalexplorer;

public class StructureProperties {
	StructureProperties(final int numAtoms) {
		this.numAtoms = numAtoms;
		atomNumbers = new int[numAtoms];
		atomSizes = new float[numAtoms];
		atomStrengths = new float[numAtoms];
		isCluster = false;
	}
	public final int numAtoms;
	public final int[] atomNumbers;
	public final float[] atomSizes;
	public final float[] atomStrengths;
	public boolean isCluster;
}