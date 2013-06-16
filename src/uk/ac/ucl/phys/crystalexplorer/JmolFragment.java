package uk.ac.ucl.phys.crystalexplorer;

import org.jmol.adapter.smarter.SmarterJmolAdapter;
import org.jmol.api.JmolCallbackListener;
import org.jmol.api.JmolViewer;
import org.jmol.constant.EnumCallback;

import uk.ac.ucl.phys.crystalexplorer.jmolplatform.Platform;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

public class JmolFragment extends Fragment implements JmolCallbackListener {
	/*
	 * General signal flow -- two cases:
	 * 
	 * I. System calls onDraw(Canvas) for whatever reason. Jmol draws onto it
	 * 
	 * II. Jmol gets a user event or script completion or DELAY command and
	 * needs an update Jmol triggers imageView.postInvalidate() System calls
	 * onDraw(Canvas)
	 */

	// labels on; background labels white;spacefill on";
	
	private final static String DEFAULT_SPACEFILL = "50%";

	private final static String STARTUP_SCRIPT = "set zoomLarge false; set allowGestures TRUE; unbind ALL _slidezoom; spacefill 50%;";
	private final static String SCRIPT_BALL_AND_STICK = "unitcell off; draw unitcell mesh nofill; spacefill " + DEFAULT_SPACEFILL + ";";

	private final static String STRUCTURE_PATH = "uk.ac.ucl.phys.crystalexplorer.STRUCTURE_PATH";

	private JmolViewer viewer;
	private JmolDisplay imageView;
	private String styleSettings = new String();
	private Bundle mSaveSettings = new Bundle();
	private String mCurrentStructure = new String();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Would like to participate in options menu
		setHasOptionsMenu(true);

		Platform.scaleFactor = getResources().getDisplayMetrics().density + 0.5f;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		imageView = new JmolDisplay(getActivity());
		imageView.setWillNotDraw(false);
		
		if (viewer == null) {
			// bit of a chicken and an egg here, but
			// we pass the updateListener to viewer, where it will be called
			// the "display" and then Platform will get a call asking for an
			// update.
			// not sure about the rest of it!
			viewer = JmolViewer
					.allocateViewer(
							imageView,
							new SmarterJmolAdapter(),
							null,
							null,
							null,
							"-NOTmultitouch-tab platform=" + Platform.class.getName(),
							null);
			viewer.setJmolCallbackListener(this);

			script(STARTUP_SCRIPT);
			
		}
		
		return imageView;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		Bundle args = getArguments();
		if(savedInstanceState != null) {
			processAtomInfo(savedInstanceState);
			mCurrentStructure = savedInstanceState.getString(STRUCTURE_PATH);
			if(!mCurrentStructure.isEmpty()) {
				loadStructure(mCurrentStructure, true);
			}
		} else if(args != null) {
			loadStructure(args.getString(AtomInfoKeys.STRUCTURE_PATH), args.getBundle(AtomInfoKeys.ATOMS_INFO));
		}
	}
	
	@Override
	public void onDestroyView() {
		super.onDestroyView();
		
		imageView.setViewer(null);
		viewer.setJmolCallbackListener(null);
		viewer = null;
		imageView = null;
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if(!mCurrentStructure.isEmpty()) {
			outState.putString(STRUCTURE_PATH, mCurrentStructure);
		}
		outState.putAll(mSaveSettings);
	}

	@Override
	public void onPause() {
		Log.w("Jmol", "onPause");
		super.onPause();
		setPaused(true);
	}

	@Override
	public void onStop() {
		Log.w("Jmol", "onStop");
		super.onStop();
		setPaused(true);
	}

	@Override
	public void onResume() {
		super.onResume();
		setPaused(false);
	};

	private void processAtomInfo(final Bundle atomInfo) {
		// Go through atom species setting radii and colours
		final int numAtoms = atomInfo.getInt(AtomInfoKeys.ATOM_INFO_NUM_ATOMS);
		final String[] atomSpecies = atomInfo
				.getStringArray(AtomInfoKeys.ATOM_INFO_ATOM_SPECIES);
		final int[] atomColours = atomInfo
				.getIntArray(AtomInfoKeys.ATOM_INFO_ATOM_COLOURS);
		final float[] atomSizes = atomInfo
				.getFloatArray(AtomInfoKeys.ATOM_INFO_ATOM_SIZES);
		for (int i = 0; i < numAtoms; ++i) {
			styleSettings += "{_" + atomSpecies[i] + "}.vanderwaals="
					+ Float.toString(atomSizes[i]) + "; ";
			styleSettings += "color {_" + atomSpecies[i] + "} [x"
					+ Integer.toHexString(0xFFFFFF & atomColours[i]) + "]; ";
		}
		mSaveSettings.putAll(atomInfo);
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		MenuItem styleMenu = menu.findItem(R.id.style);

		if (styleMenu != null)
			styleMenu.setEnabled(viewer.getAtomCount() > 0);
	}

	@Override
	public void onCreateOptionsMenu (Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.jmol, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.cpkspacefill:
			script("spacefill only; " + styleSettings);
			break;
		case R.id.ballandstick:
			String script = "wireframe -0.15; ";
			if (!styleSettings.isEmpty()) {
				script += styleSettings;
			}
			script += SCRIPT_BALL_AND_STICK;
			script(script);
			
			break;
		case R.id.sticks:
			script("wireframe -0.3; " + styleSettings);
			break;
		case R.id.wireframe:
			script("wireframe only; " + styleSettings);
			break;
		case R.id.cartoon:
			script("cartoons only;color cartoons structure");
			break;
		case R.id.trace:
			script("trace only;color trace structure");
			break;
		default:
			return super.onOptionsItemSelected(item);
		}

		return true;
	}

	@Override
	public void notifyCallback(EnumCallback message, Object[] data) {
		// probably ignore
		// TODO Auto-generated method stub

	}

	@Override
	public boolean notifyEnabled(EnumCallback type) {
		// probably ignore
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setCallbackFunction(String callbackType, String callbackFunction) {
		// ignore -- applet only
		// TODO Auto-generated method stub

	}

	private void script(final String script) {
		viewer.script(script);
	}

	public void repaint() {
		Log.w("Jmol", "JmolActivity repaint " + imageView);
		if (paused || viewer == null)
			return;
		if (imageView != null)
			imageView.postInvalidate();
	}

	private boolean paused;

	protected void setPaused(boolean TF) {
		paused = TF;
		if (paused && viewer != null) {
			viewer.syncScript("Mouse: spinXYBy 0 0 0", "+", 0);
		}
		Log.w("Jmol", "setPaused " + paused);
	}

	public void loadStructure(final String path, final Bundle atomInfoBundle) {
		processAtomInfo(atomInfoBundle);
		loadStructure(path, true);
	}
	
	private void loadStructure(final String path, final boolean doSupercell) {
		String loadString = "load " + path;
		if (doSupercell)
			loadString += " {2 2 2}; ";
		else
			loadString += "; ";
		if (!styleSettings.isEmpty()) {
			loadString += styleSettings + "; ";
		}
		loadString += SCRIPT_BALL_AND_STICK;
		script(loadString);
		mCurrentStructure = path;
	}

}
