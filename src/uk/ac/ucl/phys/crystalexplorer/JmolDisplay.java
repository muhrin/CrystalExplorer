package uk.ac.ucl.phys.crystalexplorer;

import org.jmol.api.Event;
import org.jmol.api.JmolViewer;

import uk.ac.ucl.phys.crystalexplorer.jmolplatform.UpdateListener;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceView;

public class JmolDisplay extends SurfaceView implements UpdateListener {
	
	private class PinchZoomScaleListener extends
			ScaleGestureDetector.SimpleOnScaleGestureListener {

		private JmolViewer myViewer;

		PinchZoomScaleListener(JmolViewer viewer) {
			myViewer = viewer;
		}

		@Override
		public boolean onScale(final ScaleGestureDetector detector) {
			float zoomFactor = detector.getScaleFactor();

			myViewer.syncScript("Mouse: zoomByFactor " + zoomFactor, "~", 0);
			Log.w("Jmol", "changing zoom factor=" + zoomFactor);

			return true;
		}
	}

	private class JmolSpinDetector implements SensorEventListener {

		JmolSpinDetector(JmolViewer viewer) {
			myViewer = viewer;
			myPanLock = false;
		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		}

		@Override
		public void onSensorChanged(SensorEvent event) {
			if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
				updateOrientation(event.values[0], event.values[1],
						event.values[2], event.timestamp);
			}
		}

		public void setPanLock(boolean panLock) {
			myPanLock = panLock;
		}

		private void updateOrientation(float x, float y, float z, long when) {
			float dxyz2 = x * x + y * y;
			float speed = (float) (Math.sqrt(dxyz2) * SPIN_FACTOR);
			if (myPanLock) {
				myViewer.syncScript("Mouse: rotateXYBy " + (int) (-y * speed)
						+ " " + (int) (-x * speed), "=", 0);
				return;
			}
			if (dxyz2 < GYRO_THRESHOLD)
				return;

			if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
				myViewer.syncScript("Mouse: spinXYBy " + (int) -y + " " + (int) -x
						+ " " + speed, "=", 0);
			} else {
				// landscape mode, so x --> y and y --> -x
				myViewer.syncScript("Mouse: spinXYBy " + (int) x + " " + (int) y
						+ " " + speed, "=", 0);
			}
		}

		private final static float GYRO_THRESHOLD = 0.7f;
		private final static float SPIN_FACTOR = 4.5f;

		private JmolViewer myViewer;
		private boolean myPanLock;
	}

	private JmolViewer myViewer;
	private final SensorManager mySensorManager;
	private ScaleGestureDetector myScaleDetector;
	private JmolSpinDetector mySpinDetector;

	public JmolDisplay(Context context) {
		super(context);
		mySensorManager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
		
		Log.w("Jmol", "JmolImageView " + this);
	}

	@Override
	protected void onAttachedToWindow() {
		if (myViewer == null)
			return;

		// Set up the sensors
		if (myScaleDetector == null) {
			myScaleDetector = new ScaleGestureDetector(getContext(),
					new PinchZoomScaleListener(myViewer));
		}

		if (mySpinDetector == null) {
			final boolean gyroExists = (mySensorManager
					.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null);
			if (gyroExists) {
				mySpinDetector = new JmolSpinDetector(myViewer);
			}
		}

		if (mySpinDetector != null) {
			mySensorManager.registerListener(mySpinDetector,
					mySensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
					SensorManager.SENSOR_DELAY_UI);
		}
	}

	@Override
	protected void onDetachedFromWindow() {
		if (mySpinDetector != null) {
			mySensorManager.unregisterListener(mySpinDetector);
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		final int screenWidth = canvas.getWidth();
		final int screenHeight = canvas.getHeight();
		Log.w("Jmol", "JmolActivity onDraw");

		myViewer.renderScreenImage(canvas, screenWidth, screenHeight);
	}

	@Override
	public void setViewer(JmolViewer viewer) {
		myViewer = viewer;
	}

	@Override
	public void getScreenDimensions(int[] widthHeight) {
		widthHeight[0] = getWidth();
		widthHeight[1] = getHeight();
	}

	@Override
	public void setScreenDimension() {
		int width = getWidth();
		int height = getHeight();
		if (myViewer.getScreenWidth() != width
				|| myViewer.getScreenHeight() != height)
			myViewer.setScreenDimension(width, height);
	}

	@Override
	public void repaint() {
		// from Viewer
		postInvalidate();
	}

	@Override
	public void mouseEvent(int id, int x, int y, int modifiers, long when) {
		myViewer.mouseEvent(id, x, y, modifiers, when);
	}

	@Override
	public boolean onTouchEvent(final MotionEvent event) {
		switch (event.getPointerCount()) {
		case 1:
			break;
		case 2:
			if (mySpinDetector != null && isTwoFingerGyroPan(event)) {
				mySpinDetector.setPanLock(true);
				return true;
			}
			// fall through
		default:
			myScaleDetector.onTouchEvent(event);
			// no multitouch needed here.
			return true;
		}
		if(mySpinDetector != null)
			mySpinDetector.setPanLock(false);
		final int index = event.findPointerIndex(0);
		if (index < 0)
			return true;
		Log.w("Jmol", "onTouchEvent " + index + " " + event);
		int e = Integer.MIN_VALUE;
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			e = Event.MOUSE_DOWN;
			break;
		case MotionEvent.ACTION_MOVE:
			e = Event.MOUSE_DRAG;
			break;
		case MotionEvent.ACTION_UP:
			e = Event.MOUSE_UP;
			break;
		}

		if (e != Integer.MIN_VALUE)
			myViewer.mouseEvent(e, (int) event.getX(index),
					(int) event.getY(index), Event.MOUSE_LEFT,
					event.getEventTime());
		
		postInvalidate();
		return true;
	};

	private boolean isTwoFingerGyroPan(MotionEvent event) {
		final float PAN_THRESHOLD = 0.9f;

		if (event.getAction() != MotionEvent.ACTION_POINTER_2_DOWN)
			return false;
		float x1 = event.getX(0);
		float x2 = event.getX(1);
		return (Math.abs(Math.abs(x2 - x1) / getWidth()) > PAN_THRESHOLD);
	}
}
