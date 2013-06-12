package uk.ac.ucl.phys.crystalexplorer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Parcel;
import android.os.Parcelable;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

public class AtomChooser extends RelativeLayout implements SeekBar.OnSeekBarChangeListener {
	
	private static int SEEK_BAR_MAX = 99;
	private static float MAX_ATOM_DRAW_STROKE = 10f;
	
	public static class MinMax {
		public MinMax(final double first, final double second) {
			this.min = Math.min(first, second);
			this.max = Math.max(first, second);
		}
		
		public double range() {
			return max - min;
		}
		
		public double bracket(final double x) {
			if(x < min)
				return min;
			else if(x > max)
				return max;
			return x;
		}
		
		public double fractionInRange(double x) {
			x = bracket(x);
			return (x - min) * range();
		}
		
		public final double min;
		public final double max;
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
			mySizes = new MinMax(in.readDouble(), in.readDouble());
			myStrengths = new MinMax(in.readDouble(), in.readDouble());	
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
			destination.writeDouble(mySizes.min);
			destination.writeDouble(mySizes.max);
			destination.writeDouble(myStrengths.min);
			destination.writeDouble(myStrengths.max);
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
		myMaxAtoms = 10;
		mySizes = new MinMax(0.2, 2.0);
		myStrengths = new MinMax(0.2, 2.0);
		init(context);
	}

	public AtomChooser(
			Context context,
			int maxAtoms,
			double minSize,
			double maxSize,
			double minStrength,
			double maxStrength) {
		super(context);
		myMaxAtoms = 10;
		mySizes = new MinMax(0.2, 2.0);
		myStrengths = new MinMax(0.2, 2.0);
		init(context);
	}
	
	public int getNumAtoms() {
		return Integer.parseInt(numAtomsPicker.getSelectedItem().toString());
	}
	public void setNumAtoms(final int numAtoms) {
		if(numAtoms >= 1 && numAtoms <= myMaxAtoms) {
			numAtomsPicker.setSelection(numAtoms - 1);
		}
	}
	
	public double getSize() {
		return (double)atomSizeSlider.getProgress() / (double)atomSizeSlider.getMax() * mySizes.range() + mySizes.min;
	}
	public void setSize(final double size) {
		atomSizeSlider.setProgress((int)(mySizes.fractionInRange(size) * (double)atomSizeSlider.getMax()));
	}
	
	public double getStrength() {
		return (double)atomStrengthSlider.getProgress() / (double)atomStrengthSlider.getMax() * myStrengths.range() + myStrengths.min;
	}
	public void setStrength(final double strength) {
		atomStrengthSlider.setProgress((int)(myStrengths.fractionInRange(strength) * (double)atomStrengthSlider.getMax())); 
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
		SavedState savedState = (SavedState)state;
		super.onRestoreInstanceState(savedState.getSuperState());
		
		atomSizeSlider.setMax(SEEK_BAR_MAX);
		atomStrengthSlider.setMax(SEEK_BAR_MAX);
		setNumAtoms(savedState.getNumAtoms());
		atomSizeSlider.setProgress(savedState.getSize());
		atomStrengthSlider.setProgress(savedState.getStrength());
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		updateAtomDrawSettings();
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.dispatchDraw(canvas);
		
		canvas.drawCircle(myAtomDrawX, myAtomDrawY, myAtomDrawStrokeRadius, myAtomStrokePaint);
		canvas.drawCircle(myAtomDrawX, myAtomDrawY, myAtomDrawRadius, myPaint);
	}
	
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		updateAtomDrawSettings();
	}
	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
	}
	
	public Parcelable saveInstanceState() {
		return onSaveInstanceState();
	}
	
	public void restoreInstanceState(Parcelable state) {
		onRestoreInstanceState(state);
	}
	
	private void init(Context context) {
		
		setWillNotDraw(false);
		
		myPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		myPaint.setStyle(Paint.Style.FILL);
		myPaint.setARGB(180, 255, 0, 0);
		
		myAtomStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		myAtomStrokePaint.setStyle(Paint.Style.STROKE);
		myAtomStrokePaint.setARGB(200, 0, 0, 0);
		
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
		
		LinearLayout atomOptions = new LinearLayout(context);
		atomOptions.setOrientation(LinearLayout.VERTICAL);
		
		TextView tvAtomSize = new TextView(context);
		tvAtomSize.setText("Size");
		tvAtomSize.setBackgroundColor(Color.TRANSPARENT);
		atomOptions.addView(tvAtomSize);
		
		RelativeLayout.LayoutParams sizeLayout = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.MATCH_PARENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);
		atomSizeSlider.setLayoutParams(sizeLayout);
		atomSizeSlider.setId(2);
		atomSizeSlider.setOnSeekBarChangeListener(this);
		atomOptions.addView(atomSizeSlider);
		
		TextView tvAtomStrength = new TextView(context);
		tvAtomStrength.setText("Strength");
		tvAtomStrength.setBackgroundColor(Color.TRANSPARENT);
		atomOptions.addView(tvAtomStrength);
		
		RelativeLayout.LayoutParams strengthLayout = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.MATCH_PARENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);
		atomStrengthSlider.setLayoutParams(strengthLayout);
		atomStrengthSlider.setId(3);
		atomStrengthSlider.setOnSeekBarChangeListener(this);
		atomOptions.addView(atomStrengthSlider);
		
		RelativeLayout.LayoutParams atomOptionsLayoutParams = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.MATCH_PARENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);
		atomOptionsLayoutParams.addRule(RelativeLayout.RIGHT_OF, numAtomsPicker.getId());
		atomOptions.setLayoutParams(atomOptionsLayoutParams);
		addView(atomOptions);
		
		updateAtomDrawSettings();
		invalidate();
	}
	
	private void updateAtomDrawSettings() {
		final float width = (float)getWidth();
		final float height = (float)getHeight();
			
		myAtomDrawRadius = 0.5f * (Math.min(width, height) - 2f * MAX_ATOM_DRAW_STROKE) * (float)getSize() / (float)mySizes.max;
		
		final float atomDrawStrokeWidth = (float)getStrength() / (float)myStrengths.max * MAX_ATOM_DRAW_STROKE;
		myAtomDrawStrokeRadius = myAtomDrawRadius + 0.5f * atomDrawStrokeWidth;
		myAtomStrokePaint.setStrokeWidth(atomDrawStrokeWidth);
		
		// Account for padding
		final float padLeft = getPaddingLeft();
		final float padTop = getPaddingTop();
		final float xpad = (float)(padLeft + getPaddingRight());
		final float ypad = (float)(padTop + getPaddingBottom());
		
		myAtomDrawX = padLeft + 0.5f * (width - xpad);
		myAtomDrawY = padTop + 0.5f * (height - ypad);
		invalidate();
	}
	
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
