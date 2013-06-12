package uk.ac.ucl.phys.crystalexplorer;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import uk.ac.ucl.phys.crystalexplorer.R;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
	
	public final static String EXTRA_MESSAGE = "uk.ac.ucl.phys.crystalexplorer.MESSAGE";
	
	public MainActivity() {
		atomChoosers = new ArrayList<AtomChooser>();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		content = (ViewGroup)findViewById(R.id.main_content);
		
		if(savedInstanceState == null) {
			// Initial atom chooser
			addAtom();	
		}
		else {
			
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case R.id.menu_add_atom:
			addAtom();
			return true;
		}
		return false;
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("numAtomChoosers", atomChoosers.size());
		for (AtomChooser atomChooser : atomChoosers) {
			outState.putParcelable("atomChooser" + Integer.toString(atomChooser.getId()), atomChooser.saveInstanceState());
		}
	}
	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		final int numAtomChoosers = savedInstanceState.getInt("numAtomChoosers");
		for(int i = 0; i < numAtomChoosers; ++i) {
			addAtom().restoreInstanceState(savedInstanceState.getParcelable("atomChooser" + Integer.toString(i + 1)));
		}
	}
	
	private AtomChooser addAtom() {
		AtomChooser atomChooser = new AtomChooser(this);
		RelativeLayout.LayoutParams layout = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.MATCH_PARENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);
		atomChooser.setLayoutParams(layout);
		atomChooser.setId(atomChoosers.size() + 1);
		content.addView(atomChooser);
		atomChoosers.add(atomChooser);
		content.invalidate();
		
		return atomChooser;
	}
	
	private void removeAtom() {
		if(atomChoosers.isEmpty())
			return;
		atomChoosers.remove(atomChoosers.size() - 1);
	}
	
	/** Called when the Predict button is pressed */
	public void predictStructure(View view) {
		
		TextView predictOutput = (TextView)findViewById(R.id.predict_output);
		predictOutput.setText("Generating structures...");
		
		final int numAtoms = atomChoosers.size();
		int[] numbers = new int[numAtoms];
		double[] sizes = new double[numAtoms];
		double[] strengths = new double[numAtoms];
		for(int i = 0; i < numAtoms; ++i) {
			numbers[i] = atomChoosers.get(i).getNumAtoms();
			sizes[i] = atomChoosers.get(i).getSize();
			strengths[i] = atomChoosers.get(i).getStrength();
		}
		
		// File to save the structure to
		File structureFile = new File(getFilesDir(), "out.res");
		
		NdkCrystalExplorer explorer = new NdkCrystalExplorer();
		String result = explorer.doTest(structureFile.toString(), numAtoms, numbers, sizes, strengths);
		
		predictOutput.setText(result);
		
		if(!result.equals("success"))
			return;
	    
        InputStream file = null;
		try {
			file = new BufferedInputStream(new FileInputStream(structureFile));
		} catch (FileNotFoundException e1) {
			predictOutput.setText(structureFile.toString() + " not found");
			e1.printStackTrace();
			return;
		}
        BufferedReader input = new BufferedReader(new InputStreamReader(file));
        String line;
        try {
            line = input.readLine();
            predictOutput.setText(line);
            input.close();
            
            // Start the jmol activity
            Intent startJmolIntent = new Intent(this, JmolActivity.class);
            startJmolIntent.putExtra(EXTRA_MESSAGE, structureFile.toString());
            startActivity(startJmolIntent);
            
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	
	private ViewGroup content;
	private ArrayList<AtomChooser> atomChoosers;
}
