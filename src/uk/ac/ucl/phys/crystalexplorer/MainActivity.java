package uk.ac.ucl.phys.crystalexplorer;

import jp.sfjp.webglmol.NDKmol.HetAtomMode;
import uk.ac.ucl.phys.crystalexplorer.R;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends FragmentActivity implements
		AtomsSelectionFragment.AtomSelectionListener {

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

	private boolean mPaused = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Set this action otherwise NDKMol will load it's default molecule
		getIntent().setAction(NDKMolFragment.ACTION_WAIT);

		setContentView(R.layout.activity_main);

		// Check whether the activity is using the layout version with
		// the fragment_container FrameLayout. If so, we must add the first
		// fragment
		if (findViewById(R.id.fragment_container) != null) {

			// However, if we're being restored from a previous state,
			// then we don't need to do anything and should return or else
			// we could end up with overlapping fragments.
			if (savedInstanceState != null) {
				return;
			}

			// Create an instance of ExampleFragment
			AtomsSelectionFragment atomsSelection = new AtomsSelectionFragment();

			// In case this activity was started with special instructions from
			// an Intent,
			// pass the Intent's extras to the fragment as arguments
			atomsSelection.setArguments(getIntent().getExtras());

			// Add the fragment to the 'fragment_container' FrameLayout
			getSupportFragmentManager().beginTransaction()
					.add(R.id.fragment_container, atomsSelection).commit();
		} else {
			initNDKMolFragment((NDKMolFragment) getSupportFragmentManager()
					.findFragmentById(R.id.ndkmol_fragment));
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onStructurePredicted(String structurePath, Bundle atomInfoBundle) {
		if (mPaused)
			return;

		NDKMolFragment ndkMol = (NDKMolFragment) getSupportFragmentManager()
				.findFragmentById(R.id.ndkmol_fragment);
		boolean dualPaneLayout = true;

		if (ndkMol == null) {
			ndkMol = new NDKMolFragment();
			dualPaneLayout = false;
		}
		initNDKMolFragment(ndkMol);

		if (dualPaneLayout) {

			ndkMol.openFile(structurePath, atomInfoBundle);
		} else {
			// One-pane layout so swap fragments

			Bundle args = new Bundle();
			args.putString(AtomInfoKeys.STRUCTURE_PATH, structurePath);
			args.putBundle(AtomInfoKeys.STRUCTURE_INFO, atomInfoBundle);
			ndkMol.setArguments(args);

			FragmentTransaction transaction = getSupportFragmentManager()
					.beginTransaction();

			// Replace whatever is in the fragment_container view with this
			// fragment,
			// and add the transaction to the back stack so the user can
			// navigate back
			transaction.replace(R.id.fragment_container, ndkMol);
			transaction.addToBackStack(null);

			// Commit the transaction
			transaction.commit();
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		mPaused = true;
	}

	@Override
	public void onResume() {
		super.onResume();
		mPaused = false;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.menu_about:
			showAbout();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void initNDKMolFragment(NDKMolFragment ndkMol) {
		//ndkMol.setMenuVisibility(false);
		ndkMol.setShowUnitcell(true);
		ndkMol.setHetAtomMode(HetAtomMode.SPHERE);
		ndkMol.setLineWidth(2f);
	}

	private void showAbout() {
		// Should show about dialog
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		LayoutInflater inflater = getLayoutInflater();

		// Inflate and set the layout for the dialog
		// Pass null as the parent view because its going in the dialog layout
		builder.setView(inflater.inflate(R.layout.dialog_about, null));

		builder.create().show();
	}
}
