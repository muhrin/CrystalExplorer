package uk.ac.ucl.phys.crystalexplorer;

public class NdkCrystalExplorer {
	
	static {
		System.loadLibrary("gnustl_shared");
		System.loadLibrary("sslib-jni");
	}
	
	public static native String generateStructure(String outFile, int numAtoms, int numbers[], double[] sizes, double[] strengths);
	
	public String doTest(String outFile, int numAtoms, int[] numbers, double[] sizes, double[] strengths) {
		return generateStructure(outFile, numAtoms, numbers, sizes, strengths);
	}

}
