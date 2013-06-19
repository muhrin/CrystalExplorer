package uk.ac.ucl.phys.crystalexplorer;

import java.io.File;
import java.util.ArrayList;

import uk.ac.ucl.phys.crystalexplorer.StructurePredictionTask.OutcomeCode;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class AtomsSelectionFragment extends Fragment implements OnClickListener, StructurePredictionTask.StructurePredictionListener, OnCancelListener {

	private static final int CHOOSER_ID_OFFSET = 10;
	private static final String NUM_ATOMS = "uk.ac.ucl.phys.crystalexplorer.NUM_ATOMS";

	public final static String[] ELEMENTS = { "H", "He", "Li", "Be", "B", "C",
			"N", "O", "F", "Ne", "Na", "Mg", "Al", "Si", "P", "S", "Cl", "Ar",
			"K", "Ca", "Sc", "Ti", "V", "Cr", "Mn", "Fe", "Co", "Ni", "Cu",
			"Zn", "Ga", "Ge", "As", "Se", "Br", "Kr", "Rb", "Sr", "Y", "Zr",
			"Nb", "Mo", "Tc", "Ru", "Rh", "Pd", "Ag", "Cd", "In", "Sn", "Sb",
			"Te", "I", "Xe", "Cs", "Ba", "La", "Ce", "Pr", "Nd", "Pm", "Sm",
			"Eu", "Gd", "Tb", "Dy", "Ho", "Er", "Tm", "Yb", "Lu", "Hf", "Ta",
			"W", "Re", "Os", "Ir", "Pt", "Au", "Hg", "Tl", "Pb", "Bi", "Po",
			"At", "Rn", "Fr", "Ra", "Ac", "Th", "Pa", "U", "Np", "Pu", "Am",
			"Cm", "Bk", "Cf", "Es", "Fm", "Md", "No", "Lr", "Rf", "Db", "Sg",
			"Bh", "Hs", "Mt" };
	public final static int[] ELEMENT_COLOURS = {/* 0xFFFFFF, 0xD9FFFF, */
			0xCC80FF, 0xC2FF00, 0xFFB5B5, 0x909090, 0x3050F8, 0xFF0D0D,
			0x90E050, 0xB3E3F5, 0xAB5CF2, 0x8AFF00, 0xBFA6A6, 0xF0C8A0,
			0xFF8000, 0xFFFF30, 0x1FF01F, 0x80D1E3, 0x8F40D4, 0x3DFF00,
			0xE6E6E6, 0xBFC2C7, 0xA6A6AB, 0x8A99C7, 0x9C7AC7, 0xE06633,
			0xF090A0, 0x50D050, 0xC88033, 0x7D80B0, 0xC28F8F, 0x668F8F,
			0xBD80E3, 0xFFA100, 0xA62929, 0x5CB8D1, 0x702EB0, 0x00FF00,
			0x94FFFF, 0x94E0E0, 0x73C2C9, 0x54B5B5, 0x3B9E9E, 0x248F8F,
			0x0A7D8C, 0x006985, 0xC0C0C0, 0xFFD98F, 0xA67573, 0x668080,
			0x9E63B5, 0xD47A00, 0x940094, 0x429EB0, 0x57178F, 0x00C900,
			0x70D4FF, 0xFFFFC7, 0xD9FFC7, 0xC7FFC7, 0xA3FFC7, 0x8FFFC7,
			0x61FFC7, 0x45FFC7, 0x30FFC7, 0x1FFFC7, 0x00FF9C, 0x00E675,
			0x00D452, 0x00BF38, 0x00AB24, 0x4DC2FF, 0x4DA6FF, 0x2194D6,
			0x267DAB, 0x266696, 0x175487, 0xD0D0E0, 0xFFD123, 0xB8B8D0,
			0xA6544D, 0x575961, 0x9E4FB5, 0xAB5C00, 0x754F45, 0x428296,
			0x420066, 0x007D00, 0x70ABFA, 0x00BAFF, 0x00A1FF, 0x008FFF,
			0x0080FF, 0x006BFF, 0x545CF2, 0x785CE3, 0x8A4FE3, 0xA136D4,
			0xB31FD4, 0xB31FBA, 0xB30DA6, 0xBD0D87, 0xC70066, 0xCC0059,
			0xD1004F, 0xD90045, 0xE00038, 0xE6002E, 0xEB0026 };

	private final static int MAX_ATOMS = ELEMENTS.length;

	public interface AtomSelectionListener {
		public void onStructurePredicted(String structureFile,
				Bundle atomInfoBundle);
	}

	private ProgressDialog mProgressDialog;
	private StructurePredictionTask mStructurePredictionTask;

	public AtomsSelectionFragment() {
		atomChoosers = new ArrayList<AtomChooser>();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Would like to participate in options menu
		setHasOptionsMenu(true);
		
		resetProgressDialog();
	}
	
	@Override
	public void onCreateOptionsMenu (Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.atoms_list, menu);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.atoms_list, container, false);

		content = (ViewGroup) view.findViewById(R.id.atoms_list);

		view.findViewById(R.id.button_predict).setOnClickListener(this);

		return view;
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		for (AtomChooser chooser : atomChoosers) {
			content.removeView(chooser);
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		if (savedInstanceState == null && atomChoosers.isEmpty()) {
			// Initial atom chooser
			addAtom();
		}
	}

	@Override
	public void onViewStateRestored(Bundle savedInstanceState) {
		super.onViewStateRestored(savedInstanceState);

		if (savedInstanceState != null) {
			final int numAtomChoosers = savedInstanceState.getInt(NUM_ATOMS);
			for (int i = 0; i < numAtomChoosers; ++i) {
				AtomChooser atom = addAtom();

				if (atom != null)
					atom.restoreInstanceState(savedInstanceState
							.getParcelable("atomChooser"
									+ Integer.toString(CHOOSER_ID_OFFSET + i)));
			}
		} else if (!atomChoosers.isEmpty()) {
			for (AtomChooser chooser : atomChoosers) {
				if (chooser.getParent() == null)
					content.addView(chooser);
			}
		}
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		try {
			myCallback = (AtomSelectionListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ "must implement AtomSelectionListener");
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_add_atom:
			addAtom();
			return true;
		case R.id.menu_remove_atom:
			removeAtom();
			return true;
		}
		return false;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putInt(NUM_ATOMS, atomChoosers.size());
		for (AtomChooser atomChooser : atomChoosers) {
			outState.putParcelable(
					"atomChooser" + Integer.toString(atomChooser.getId()),
					atomChooser.saveInstanceState());
		}
	}
	
	@Override
	public void onPause() {
		super.onPause();
		
		if(mStructurePredictionTask != null) {
			mStructurePredictionTask.cancel(true);
		}
		if(mProgressDialog != null && mProgressDialog.isShowing()) {
			mProgressDialog.dismiss();
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.button_predict:
			predictStructure();
			break;
		}
	}

	private AtomChooser addAtom() {
		if (atomChoosers.size() < MAX_ATOMS) {
			AtomChooser atomChooser = new AtomChooser(getActivity(),
					ELEMENT_COLOURS[atomChoosers.size()]);
			RelativeLayout.LayoutParams layout = new RelativeLayout.LayoutParams(
					RelativeLayout.LayoutParams.MATCH_PARENT,
					RelativeLayout.LayoutParams.WRAP_CONTENT);
			atomChooser.setLayoutParams(layout);
			atomChooser.setId(CHOOSER_ID_OFFSET + atomChoosers.size());

			content.addView(atomChooser);
			atomChoosers.add(atomChooser);
			content.invalidate();

			return atomChooser;
		}
		return null;
	}

	private void removeAtom() {
		if (atomChoosers.size() <= 1)
			return;

		((LinearLayout) content).removeView(atomChoosers.get(atomChoosers
				.size() - 1));
		atomChoosers.remove(atomChoosers.size() - 1);
		content.invalidate();
	}

	private void predictStructure() {

		final StructureProperties structure = new StructureProperties(atomChoosers.size());
		for (int i = 0; i < atomChoosers.size(); ++i) {
			structure.atomNumbers[i] = atomChoosers.get(i).getNumAtoms();
			structure.atomSizes[i] = atomChoosers.get(i).getSize();
			structure.atomStrengths[i] = atomChoosers.get(i).getStrength();
		}

		mProgressDialog.show();
		
		mStructurePredictionTask = new StructurePredictionTask(getStructureSaveDir(), this);
		mStructurePredictionTask.execute(structure);
	}

	private Bundle createAtomInfoBundle() {
		Bundle atomInfo = new Bundle();

		final int numAtoms = atomChoosers.size();
		atomInfo.putInt(AtomInfoKeys.ATOM_INFO_NUM_ATOMS, numAtoms);
		float[] atomSizes = new float[numAtoms];
		int[] atomColours = new int[numAtoms];
		String[] atomSpecies = new String[numAtoms];
		for (int i = 0; i < numAtoms; ++i) {
			atomSizes[i] = (float) atomChoosers.get(i).getSize();
			atomColours[i] = atomChoosers.get(i).getColour();
			atomSpecies[i] = ELEMENTS[i];
		}
		atomInfo.putFloatArray(AtomInfoKeys.ATOM_INFO_ATOM_SIZES, atomSizes);
		atomInfo.putIntArray(AtomInfoKeys.ATOM_INFO_ATOM_COLOURS, atomColours);
		atomInfo.putStringArray(AtomInfoKeys.ATOM_INFO_ATOM_SPECIES,
				atomSpecies);

		return atomInfo;
	}

	private AtomSelectionListener myCallback;
	private ViewGroup content;
	private ArrayList<AtomChooser> atomChoosers;
	
	@Override
	public void onStructurePredicted(String path) {
		resetProgressDialog();
		
		myCallback.onStructurePredicted(path, createAtomInfoBundle());
	}

	@Override
	public void onStructurePredictionFailed(OutcomeCode err, String msg) {
		resetProgressDialog();
		
		// 1. Instantiate an AlertDialog.Builder with its constructor
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		// 2. Chain together various setter methods to set the dialog characteristics
		builder.setMessage(R.string.prediction_failed);
		       //.setTitle(R.string.dialog_title);

		// 3. Get the AlertDialog from create()
		AlertDialog dialog = builder.create();
		dialog.show();
	}

	@Override
	public void onStructurePredictionProgress(int percentage, boolean takingLong) {
		if(mProgressDialog != null && mProgressDialog.isShowing()) {
			if(!takingLong)
				mProgressDialog.setProgress(percentage);
			else {
				if(percentage != mProgressDialog.getMax())
					mProgressDialog.setProgress(mProgressDialog.getMax());
				mProgressDialog.setMessage(getString(R.string.predicting_structure_taking_long));
			}
		}
	}

	@Override
	public void onCancel(DialogInterface dialog) {
		if(mStructurePredictionTask != null) {
			mStructurePredictionTask.cancel(true);
		}
		resetProgressDialog();
	}
	
	private void resetProgressDialog() {
		if(mProgressDialog == null) {
			mProgressDialog = new ProgressDialog(getActivity());
			mProgressDialog.setOnCancelListener(this);
		}
		
		if(mProgressDialog.isShowing()) {
			mProgressDialog.dismiss();
		}
		
		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		mProgressDialog.setMessage(getString(R.string.predicting_structure));
		mProgressDialog.setProgress(0);
		mProgressDialog.setMax(100);
	}
	
	private File getStructureSaveDir() {
		String state = Environment.getExternalStorageState();
	    if (Environment.MEDIA_MOUNTED.equals(state) && !Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
	        return getActivity().getExternalFilesDir(null);
	    } else {
	    	return getActivity().getFilesDir();
	    }
	}
}
