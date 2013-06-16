package uk.ac.ucl.phys.crystalexplorer;

import java.util.Random;

import android.content.Context;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class AtomChooser extends RelativeLayout implements SeekBar.OnSeekBarChangeListener {
	
	private static int SEEK_BAR_MAX = 99;
	private static float MAX_ATOM_DRAW_STROKE = 10f;
	private static int MAX_ATOMS = 5;
	
	public static class MinMax {
		
		private static final Random mRandom = new Random();
		
		public MinMax(final float first, final float second) {
			this.min = Math.min(first, second);
			this.max = Math.max(first, second);
		}
		
		public float range() {
			return max - min;
		}
		
		public float bracket(final float x) {
			if(x < min)
				return min;
			else if(x > max)
				return max;
			return x;
		}
		
		public float fractionInRange(float x) {
			x = bracket(x);
			return (x - min) / range();
		}
		
		public float randomInRange() {
			return mRandom.nextFloat() * range() + min;
		}
		
		public final float min;
		public final float max;
	}
	
	public static class SavedState extends BaseSavedState {
		
		private SavedState(
				Parcelable superState,
				final int maxAtoms,
				final MinMax sizes,
				final MinMax strengths,
				final int numAtoms,
				final int size,
				final int strength) {
			super(superState);
			myMaxAtoms = maxAtoms;
			mySizes = sizes;
			myStrengths = strengths;
			myNumAtoms = numAtoms;
			mySize = size;
			myStrength = strength;
		}
		
		private SavedState(Parcel in) {
			super(in);
			myMaxAtoms = in.readInt();
			mySizes = new MinMax(in.readFloat(), in.readFloat());
			myStrengths = new MinMax(in.readFloat(), in.readFloat());	
			myNumAtoms = in.readInt();
			mySize = in.readInt();
			myStrength = in.readInt();
		}
		
		public int getNumAtoms() {
			return myNumAtoms;
		}
		
		public int getSize() {
			return mySize;
		}
		
		public int getStrength() {
			return myStrength;
		}
		
		public int getMaxAtoms() {
			return myMaxAtoms;
		}
		
		public MinMax getSizes() {
			return mySizes;
		}
		
		public MinMax getStrengths() {
			return myStrengths;
		}
		
		@Override
		public void writeToParcel(Parcel destination, int flags) {
			super.writeToParcel(destination, flags);
			destination.writeInt(myMaxAtoms);
			destination.writeFloat(mySizes.min);
			destination.writeFloat(mySizes.max);
			destination.writeFloat(myStrengths.min);
			destination.writeFloat(myStrengths.max);
			destination.writeInt(myNumAtoms);
			destination.writeInt(mySize);
			destination.writeInt(myStrength);
		}
		
        public static final Parcelable.Creator<SavedState> CREATOR = new Creator<SavedState>() {

            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }

        };
		
        private final int myMaxAtoms;
        private final MinMax mySizes;
        private final MinMax myStrengths;
		private final int myNumAtoms;
		private final int mySize;
		private final int myStrength;
	}
	
	public AtomChooser(Context context) {
		super(context);
		myMaxAtoms = MAX_ATOMS;
		mySizes = new MinMax(0.2f, 2.0f);
		myStrengths = new MinMax(0.2f, 2.0f);
		setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		init(context, 0xFF0000);
	}
	
	public AtomChooser(Context context, final int atomColour) {
		super(context);
		myMaxAtoms = MAX_ATOMS;
		mySizes = new MinMax(0.2f, 2.0f);
		myStrengths = new MinMax(0.2f, 2.0f);
		init(context, atomColour);
	}	

	public AtomChooser(
			Context context,
			int maxAtoms,
			float minSize,
			float maxSize,
			float minStrength,
			float maxStrength,
			int atomColour) {
		super(context);
		myMaxAtoms = MAX_ATOMS;
		mySizes = new MinMax(0.2f, 2.0f);
		myStrengths = new MinMax(0.2f, 2.0f);
		init(context, atomColour);
	}
	
	public int getNumAtoms() {
		return Integer.parseInt(numAtomsPicker.getSelectedItem().toString());
	}
	public void setNumAtoms(final int numAtoms) {
		if(numAtoms >= 1 && numAtoms <= myMaxAtoms) {
			numAtomsPicker.setSelection(numAtoms - 1);
		}
	}
	
	public float getSize() {
		return (float)atomSizeSlider.getProgress() / (float)atomSizeSlider.getMax() * mySizes.range() + mySizes.min;
	}
	public void setSize(final float size) {
		atomSizeSlider.setProgress((int)(mySizes.fractionInRange(size) * (float)atomSizeSlider.getMax()));
	}
	
	public float getStrength() {
		return (float)atomStrengthSlider.getProgress() / (float)atomStrengthSlider.getMax() * myStrengths.range() + myStrengths.min;
	}
	public void setStrength(final float strength) {
		atomStrengthSlider.setProgress((int)(myStrengths.fractionInRange(strength) * (float)atomStrengthSlider.getMax())); 
	}
	
	@Override
	protected Parcelable onSaveInstanceState() {
		Parcelable superState = super.onSaveInstanceState();
		return new SavedState(
				superState,
				myMaxAtoms,
				mySizes,
				myStrengths,
				getNumAtoms(),
				atomSizeSlider.getProgress(),
				atomStrengthSlider.getProgress());
	}
	
	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		if(state == null)
			return;
		
		SavedState savedState = null;
		try {
			savedState = (SavedState)state;
		} catch (ClassCastException e) {
		}
		
		if(savedState != null) {
			super.onRestoreInstanceState(savedState.getSuperState());
			
			atomSizeSlider.setMax(SEEK_BAR_MAX);
			atomStrengthSlider.setMax(SEEK_BAR_MAX);
			setNumAtoms(savedState.getNumAtoms());
			atomSizeSlider.setProgress(savedState.getSize());
			atomStrengthSlider.setProgress(savedState.getStrength());
		} else
			super.onRestoreInstanceState(state);
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		updateAtomDrawSettings();
	}
	
	@Override
	protected void dispatchDraw(Canvas canvas) {
		super.dispatchDraw(canvas);
		
		final float height = (float)getHeight();
		final float width = (float)getWidth();
		

		if (Build.VERSION.SDK_INT >= 11) {
			if (canvas.isHardwareAccelerated()) {
				setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		    }
		}
		
		canvas.drawCircle(myAtomDrawX, myAtomDrawY, myAtomDrawStrokeRadius, myAtomStrokePaint);
		canvas.drawCircle(myAtomDrawX, myAtomDrawY, myAtomDrawRadius, myPaint);
		canvas.drawLine(0f, height - 1f, width, height - 1f, mFooterLinePaint);
	}
	
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		updateAtomDrawSettings();
	}
	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
	}
	
	public Parcelable saveInstanceState() {
		return onSaveInstanceState();
	}
	
	public void restoreInstanceState(Parcelable state) {
		onRestoreInstanceState(state);
	}
	
	private void init(Context context, final int atomColour) {

		// Drawing stuff
		myPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		myPaint.setStyle(Paint.Style.FILL);
		myPaint.setColor(atomColour);
		myPaint.setAlpha(150);
		
		myAtomStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		myAtomStrokePaint.setStyle(Paint.Style.STROKE);
		myAtomStrokePaint.setARGB(170, 40, 40, 40);
		myAtomStrokePaint.setMaskFilter(new BlurMaskFilter(5f, BlurMaskFilter.Blur.NORMAL));
		
		mFooterLinePaint = new Paint();
		mFooterLinePaint.setStyle(Paint.Style.STROKE);
		mFooterLinePaint.setARGB(255, 170, 170, 170);
		
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		setPadding(16, 16, 16, 16);
		setLayoutParams(lp);
		
		atomSizeSlider = new SeekBar(context);
		atomSizeSlider.setMax(SEEK_BAR_MAX);
		atomStrengthSlider = new SeekBar(context);
		atomStrengthSlider.setMax(SEEK_BAR_MAX);
		numAtomsPicker = new Spinner(context);
		
		RelativeLayout.LayoutParams numLayout = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);
		numLayout.addRule(CENTER_VERTICAL);
		numAtomsPicker.setLayoutParams(numLayout);
		numAtomsPicker.setId(1);
		Integer[] possibleNumAtoms = new Integer[myMaxAtoms];
		for(int i = 1; i <= myMaxAtoms; ++i)
			possibleNumAtoms[i - 1] = i;
		ArrayAdapter<Integer> numAtomsAdapter =
				new ArrayAdapter<Integer>(context, android.R.layout.simple_spinner_dropdown_item, possibleNumAtoms);
		numAtomsPicker.setAdapter(numAtomsAdapter);
		addView(numAtomsPicker);
		
		TableLayout atomOptions = new TableLayout(context);
		TableRow sizeRow = new TableRow(context);
		sizeRow.setGravity(Gravity.CENTER_VERTICAL);
		TableRow strengthRow = new TableRow(context);
		strengthRow.setGravity(Gravity.CENTER_VERTICAL);
		
		TextView tvAtomSize = new TextView(context);
		tvAtomSize.setText("Size");
		tvAtomSize.setBackgroundColor(Color.TRANSPARENT);
		tvAtomSize.setId(10);
		sizeRow.addView(tvAtomSize);
		
		TextView tvAtomStrength = new TextView(context);
		tvAtomStrength.setText("Strength");
		tvAtomStrength.setBackgroundColor(Color.TRANSPARENT);
		tvAtomStrength.setId(11);
		strengthRow.addView(tvAtomStrength);
		
		atomSizeSlider.setId(2);
		atomSizeSlider.setOnSeekBarChangeListener(this);
		sizeRow.addView(atomSizeSlider);

		atomStrengthSlider.setId(3);
		atomStrengthSlider.setOnSeekBarChangeListener(this);
		strengthRow.addView(atomStrengthSlider);

		atomOptions.addView(sizeRow);
		atomOptions.addView(strengthRow);
		atomOptions.setColumnStretchable(1, true);
		atomOptions.setGravity(Gravity.CENTER_VERTICAL);
		
		RelativeLayout.LayoutParams atomOptionsLayoutParams = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.MATCH_PARENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);
		atomOptionsLayoutParams.addRule(RelativeLayout.RIGHT_OF, numAtomsPicker.getId());
		atomOptions.setLayoutParams(atomOptionsLayoutParams);
		addView(atomOptions);
		
		setSize(mySizes.randomInRange());
		setStrength(myStrengths.randomInRange());
		
		updateAtomDrawSettings();
		invalidate();
	}
	
	private void updateAtomDrawSettings() {
		final float width = (float)getWidth();
		final float height = (float)getHeight();
			
		myAtomDrawRadius = 0.5f * (Math.min(width, height) - 2f * MAX_ATOM_DRAW_STROKE) * (float)getSize() / (float)mySizes.max;
		
		final float atomDrawStrokeWidth = (float)getStrength() / (float)myStrengths.max * MAX_ATOM_DRAW_STROKE;
		myAtomDrawStrokeRadius = myAtomDrawRadius + 0.5f * atomDrawStrokeWidth;
		myAtomStrokePaint.setStrokeWidth(atomDrawStrokeWidth + 0.001f);
		
		// Account for padding
		final float padLeft = getPaddingLeft();
		final float padTop = getPaddingTop();
		final float xpad = (float)(padLeft + getPaddingRight());
		final float ypad = (float)(padTop + getPaddingBottom());
		
		myAtomDrawX = padLeft + 0.5f * (width - xpad);
		myAtomDrawY = padTop + 0.5f * (height - ypad);
		invalidate();
	}
	
	public int getColour() {
		return myPaint.getColor();
	}
	
	private Paint mFooterLinePaint;
	private Paint myPaint;
	private Paint myAtomStrokePaint;
	private float myAtomDrawX;
	private float myAtomDrawY;
	private float myAtomDrawRadius;
	private float myAtomDrawStrokeRadius;
	
	private final int myMaxAtoms;
	private final MinMax mySizes;
	private final MinMax myStrengths;
	private SeekBar atomSizeSlider;
	private SeekBar atomStrengthSlider;
	private Spinner numAtomsPicker;

}
