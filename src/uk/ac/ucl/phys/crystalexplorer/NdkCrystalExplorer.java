package uk.ac.ucl.phys.crystalexplorer;

public class NdkCrystalExplorer {

	static {
		System.loadLibrary("gnustl_shared");
		System.loadLibrary("sslib-jni");
	}

	public static native String generateStructure(String outFile, int numAtoms,
			int numbers[], float[] sizes, float[] strengths);

}
