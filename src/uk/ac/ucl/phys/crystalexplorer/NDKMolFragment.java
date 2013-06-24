package uk.ac.ucl.phys.crystalexplorer;

import android.view.Menu;
import android.view.MenuInflater;

public class NDKMolFragment extends jp.sfjp.webglmol.NDKmol.NDKMolFragment {
	
	public NDKMolFragment() {
		super(false);
	}
	
	@Override
	public void onCreateOptionsMenu (Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		
		inflater.inflate(R.menu.ndkmol, menu);
	}
}
