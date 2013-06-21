package uk.ac.ucl.phys.crystalexplorer;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class BlockingFixedThreadPool extends ThreadPoolExecutor {
	
	
	BlockingFixedThreadPool(final int nThreads) {
		super(nThreads, nThreads, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
	}
	
	@Override
	public void execute(Runnable command) {
		if(getQueue().size() >= getCorePoolSize()) {
			try {
				FutureTask< Void > ftask = new FutureTask<Void>(new Runnable() { public void run() {} }, null);
		        super.execute(ftask);
				ftask.get(); // Block
			} catch (InterruptedException e) {
			} catch (ExecutionException e) {
			}
		}
		super.execute(command);
	}

}
