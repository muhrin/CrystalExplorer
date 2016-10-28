package uk.ac.ucl.phys.crystalexplorer;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.Runtime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.ReentrantLock;

import android.os.AsyncTask;
import android.os.CountDownTimer;

public class StructurePredictionTask extends AsyncTask<StructureProperties, Integer, StructurePredictionTask.PredictionOutcome> {
	
	public enum OutcomeCode {
		SUCCESS, ERROR_FAILED_TO_PREDICT
	}
	
	public interface StructurePredictionListener {
		public void onStructurePredicted(String path);
		public void onStructurePredictionFailed(OutcomeCode err, String msg);
		public void onStructurePredictionProgress(int percentage, boolean takingLong);
	}

	public class PredictionOutcome {
		public OutcomeCode code;
		public String message;
		public String structurePath;
	}
	
	enum PredictionTimerType {
		PREDICTION, GRACE
	}

	private class PredictionTimer extends CountDownTimer implements Runnable {

		private final StructurePredictionTask mRun;
		private final PredictionTimerType mType;
		private final long mTotalCountdownTime;
		private boolean mKilled;

		PredictionTimer(StructurePredictionTask run, PredictionTimerType type,
				long millisInFuture) {
			super(millisInFuture, Math.max(millisInFuture / 100, 1));
			mTotalCountdownTime = millisInFuture;
			mRun = run;
			mType = type;
			mKilled = false;
		}
		
		@Override
		public void run() {
			start();
		}
		
		@Override
		public void onTick(long millisUntilFinished) {
			float fractionalProgress = (float)(mTotalCountdownTime - millisUntilFinished) / (float)mTotalCountdownTime;
			mRun.onPredictTick(fractionalProgress * 100f);
		}

		@Override
		public void onFinish() {
			if(!mKilled) {
				if (mType == PredictionTimerType.PREDICTION)
					mRun.onPredictionTimeout();
				else
					mRun.onGraceTimeout();
			}
		}
		
		public void kill() {
			if(!mKilled) {
				mKilled = true;
				cancel();
			}
		}
	}

	public class PredictionRun {
		private final StructureProperties mStructure;
		private final float mPrecision;
		private final File mSaveDir;
		private final int mMaxTimesFound;

		private boolean mStop;
		private boolean mStopAfterNext;
		private File mLowestPath;
		private boolean mBeenUpdated;;
		private float mLowestEnergy;
		private int mTimesFound;
		private String mErrorMessage;
		ExecutorService mPool;
		ReentrantLock mPoolLock = new ReentrantLock();
		
		class Optimiser implements Runnable {
			private final PredictionRun mRun;
			private final StructureProperties mStructure;
			private final File mStructureFile;
			
			Optimiser(final PredictionRun run, final File structureFile, final StructureProperties structureProperties) {
				mRun = run;
				mStructureFile = new File(structureFile.getPath());
				mStructure = structureProperties;
			}

			@Override
			public void run() {
				final String result = NdkCrystalExplorer.generateStructure(mStructureFile.getPath(),
						mStructure.numAtoms, mStructure.atomNumbers, mStructure.atomSizes, mStructure.atomStrengths, mStructure.isCluster);
				mRun.updateWith(result, mStructureFile);
				
			}
		}

		PredictionRun(StructureProperties structure, float precision, File saveDir, int maxTimesFound) {
			mStructure = structure;
			mPrecision = precision;
			mSaveDir = saveDir;
			mMaxTimesFound = maxTimesFound;

			mStop = false;
			mLowestEnergy = 0f;
			mTimesFound = 0;
			mBeenUpdated = false;
			mErrorMessage = new String();
		}

		boolean updateWith(String outcome, File structureFile) {
			mBeenUpdated = true;

			if (!outcome.equals("success")) {
				if (mLowestEnergy == 0f)
					mErrorMessage = outcome;
				return false;
			}

			InputStream file = null;
			try {
				file = new BufferedInputStream(new FileInputStream(
						structureFile));
			} catch (FileNotFoundException e1) {
				if (!hasStructure())
					setError(structureFile.toString() + " not found");
				return false;
			}

			BufferedReader input = new BufferedReader(new InputStreamReader(
					file));
			String energyToken;
			try {
				energyToken = input.readLine();

				String[] tokens = energyToken.split("\\s");
				if (tokens.length < 5) {
					setError("Couldn't find energy token");
					input.close();
					structureFile.delete();
					return false;
				}
				energyToken = tokens[4];

				input.close();

			} catch (IOException e) {
				return false;
			}

			float energy;
			try {
				energy = Float.parseFloat(energyToken);
			} catch (NumberFormatException e) {
				setError("Couldn't parse energy token");
				structureFile.delete();
				return false;
			}

			setStructure(structureFile, energy);
			return true;
		}

		public boolean hasBeenUpdated() {
			return mBeenUpdated;
		}

		public boolean hasStructure() {
			return mLowestPath != null && mLowestPath.exists();
		}

		public String getLowestPath() {
			if(mLowestPath == null) return "";
			return mLowestPath.getPath();
		}

		public String getErrorMessage() {
			return mErrorMessage;
		}

		public int getTimesFound() {
			return mTimesFound;
		}
		
		public boolean willStopAfterNext() {
			return mStopAfterNext;
		}

		public void start() {
			
			mPool = new BlockingFixedThreadPool(Runtime.getRuntime().availableProcessors());
			
			int i = 0;
			mStop = false;
			mStopAfterNext = false;
			while (!mStop) {
				// File to save the structure to
				File structureFile = new File(mSaveDir, generateStructureName()
						+ "-" + Integer.toString(i++) + ".res");
				mPool.execute(new Optimiser(this, structureFile, mStructure));
				
				if(mStopAfterNext && hasStructure())
					break;

				if (getTimesFound() >= mMaxTimesFound)
					break;
			}
			
			mPoolLock.lock();
			mPool = null;
			mPoolLock.unlock();
		}

		public void stopNow() {
			if(mStop)
				return;
			
			mPoolLock.lock();
			mStop = true;
			if(mPool != null) {
				mPool.shutdownNow();
			}
			mPoolLock.unlock();
		}
		
		public void stopAfterNext() {
			mStopAfterNext = true;
		}

		private void setError(String msg) {
			mErrorMessage = msg;
		}

		synchronized private void setStructure(File structureFile, float energy) {
			mErrorMessage = "";
			if (hasStructure()) {
				// Is the energy the same?
				if (equals(energy, mLowestEnergy)) {
					structureFile.delete();
					++mTimesFound;
					return;
				} else if (energy > mLowestEnergy) {
					structureFile.delete();
					return;
				}
				else
					mTimesFound = 0;
			}

			if(hasStructure())
				mLowestPath.delete();
			
			mLowestPath = structureFile;
			mLowestEnergy = energy;
		}

		private boolean equals(float v1, float v2) {
			return (!((v1 < v2 - mPrecision) || (v2 < v1 - mPrecision)));
		}

		public int getMaxTimesFound() {
			return mMaxTimesFound;
		}
		
		private String generateStructureName() {
			return Long.toString(System.currentTimeMillis());
		}

	}

	private final static float ENERGY_TOLERANCE = 5e-4f;
	public final static long DEFAULT_PREDICTION_TIME = 3000;
	public final static long DEFAULT_PREDICTION_TIME_GRACE = 2000;

	private final File mFilesDir;
	private PredictionRun mCurrentRun;
	private final StructurePredictionListener mListener;
	private boolean mTakingLong;
	private PredictionTimer mPredictionTimer;
	private boolean mCancelled;

	public StructurePredictionTask(File filesDir, StructurePredictionListener listener) {
		mFilesDir = filesDir;
		mListener = listener;
	}
	
	private void onGraceTimeout() {
		if(mCurrentRun != null)
			mCurrentRun.stopNow();
	}

	private void onPredictionTimeout() {
		if(mCurrentRun != null) {
			if(mCurrentRun.getTimesFound() >= mCurrentRun.getMaxTimesFound())
				mCurrentRun.stopNow();
			else {
				// Give it a grace period to find some structures
				mCurrentRun.stopAfterNext();
				mTakingLong = true;
				(new Thread(new PredictionTimer(this, PredictionTimerType.GRACE,
						DEFAULT_PREDICTION_TIME_GRACE))).start();
			}
		}
	}
	
	private void onPredictTick(float fractionalProgress) {
		publishProgress((int)fractionalProgress);
	}
	
	public void stopAndCancel() {
		if(mCurrentRun != null) {
			mCurrentRun.stopNow();
		}
		mCancelled = true;
	}

	@Override
	protected void onPreExecute() {
		mCancelled = false;
		if(mPredictionTimer != null) {
			mPredictionTimer.kill();
			mPredictionTimer = null;
		}
		
		mPredictionTimer = new PredictionTimer(this, PredictionTimerType.PREDICTION,
				DEFAULT_PREDICTION_TIME);
		(new Thread(mPredictionTimer)).start();
	}

	@Override
	protected PredictionOutcome doInBackground(StructureProperties... args) {
		PredictionOutcome outcome = new PredictionOutcome();

		final StructureProperties structureProperties = args[0];
		int totalAtoms = 0;
		for (int n : structureProperties.atomNumbers) {
			totalAtoms += n;
		}
		final int maxTimesFound = 2 * totalAtoms;

		mTakingLong = false;
		mCurrentRun = new PredictionRun(structureProperties, ENERGY_TOLERANCE, mFilesDir, maxTimesFound);
		mCurrentRun.start();
		mPredictionTimer.kill();

		if (mCurrentRun.hasStructure()) {
			outcome.code = OutcomeCode.SUCCESS;
			outcome.structurePath = mCurrentRun.getLowestPath();
		} else {
			outcome.code = OutcomeCode.ERROR_FAILED_TO_PREDICT;
			outcome.message = mCurrentRun.getErrorMessage();
		}
		mCurrentRun = null;

		return outcome;
	}
	
	@Override
	protected final void  onProgressUpdate (Integer... values) {
		if(mCurrentRun != null && mListener != null && !mCancelled) {
			mListener.onStructurePredictionProgress(values[0], mTakingLong);
		}
	}
	
	@Override
	protected void onPostExecute(PredictionOutcome outcome) {
		if(mPredictionTimer != null) {
			mPredictionTimer = null;
		}
		
		if(mListener != null && !mCancelled) {
			if(outcome.code == OutcomeCode.SUCCESS)
				mListener.onStructurePredicted(outcome.structurePath);
			else
				mListener.onStructurePredictionFailed(outcome.code, outcome.message);
		}
	}

}
