package uk.ac.ucl.phys.crystalexplorer;

import java.util.ArrayList;
import java.util.concurrent.Executor;

public class MultiThreadExecutor implements Executor {
	private ArrayList<Thread> myThreads;
	
	MultiThreadExecutor(final int numThreads) {
		myThreads = new ArrayList<Thread>(2);
		for (Thread thread : myThreads) {
			//thread.
		}
	}
	
	@Override
	public void execute(Runnable command) {
		// TODO Auto-generated method stub
	}

}
