package uk.ac.ucl.phys.crystalexplorer;

import java.io.File;

import org.jmol.adapter.smarter.SmarterJmolAdapter;
import org.jmol.api.Event;
import org.jmol.api.JmolCallbackListener;
import org.jmol.api.JmolViewer;
import org.jmol.constant.EnumCallback; //import org.jmol.i18n.GT;
import org.openscience.jmolandroid.FileDialog;
import org.openscience.jmolandroid.search.Downloader;
import org.openscience.jmolandroid.search.PDBSearchActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceView;
import android.widget.EditText;

public class JmolActivity extends Activity implements JmolCallbackListener {

  /*
   * General signal flow -- two cases:
   * 
   * I. System calls onDraw(Canvas) for whatever reason.
   *        Jmol draws onto it
   * 
   * II. Jmol gets a user event or script completion or DELAY command
   *     and needs an update
   *        Jmol triggers imageView.postInvalidate()
   *        System calls onDraw(Canvas)
   * 
   * 
   */

  private final static String TEST_SCRIPT = ";load http://chemapps.stolaf.edu/jmol/docs/examples-12/data/caffeine.xyz;";
  //labels on; background labels white;spacefill on";

  private final static String STARTUP_SCRIPT = "set zoomLarge false; set allowGestures TRUE; unbind ALL _slidezoom;";

  public static float SCALE_FACTOR;
  
  private int screenWidth, screenHeight;

  class JmolImageView extends SurfaceView {

    public JmolImageView(Context context) {
      super(context);
      // TODO Auto-generated constructor stub
      Log.w("Jmol", "JmolImageView " + this);
    }

    @Override
    protected void onDraw(Canvas canvas) {
      super.onDraw(canvas);
      
      screenWidth = canvas.getWidth();
      screenHeight = canvas.getHeight();
      Log.w("Jmol", "JmolActivity onDraw");
      
      viewer.renderScreenImage(canvas, screenWidth, screenHeight);
      //      drawTrigger = false;
    }
  }

  //  protected boolean drawTrigger;
  private JmolViewer viewer;
  private JmolImageView imageView;
  private JmolUpdateListener updateListener;
  private boolean opening;
  private ScaleGestureDetector scaleDetector;
  private JmolSpinDetector spinDetector;

  private boolean resumeComplete;

  public SurfaceView getImageView() {
    return imageView;
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    // Ignore orientation change that restarts activity
    super.onConfigurationChanged(newConfig);
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    
    imageView = new JmolImageView(this);
    imageView.setWillNotDraw(false);
    setContentView(imageView);
    
    updateListener = new JmolUpdateListener(this);

    SCALE_FACTOR = getResources().getDisplayMetrics().density + 0.5f;
    scaleDetector = new ScaleGestureDetector(this, new PinchZoomScaleListener());
    SensorManager sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
    boolean gyroExists = (sm.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null);
    if (gyroExists) {
      spinDetector = new JmolSpinDetector();
      sm.registerListener(spinDetector, sm
          .getDefaultSensor(Sensor.TYPE_GYROSCOPE),
          SensorManager.SENSOR_DELAY_UI);
    }

  }

  /*
   * this is what took me ALL day to discover --
   * -- sometimes onPause and onDestroy are sent
   *    at the BEGINNING of a process. Don't know why...
   *    anyway, the System.exit(0) was not good.
   *    
  @Override
  protected void onDestroy() {
    super.onDestroy();
    Log.w("Jmol", "onDestroy");
    if (viewer != null)
       viewer.releaseScreenImage();

    viewer = null;
    imageView = null;
    updateListener = null;

    //System.exit(0);
  };

  */

  @Override
  protected void onPause() {
    Log.w("Jmol", "onPause");
    super.onPause();
    setPaused(true);
  }

  @Override
  protected void onStop() {
    Log.w("Jmol", "onStop");
    super.onStop();
    setPaused(true);
  }

  @Override
  protected void onResume() {
    super.onResume();
    setPaused(false);

    //   setDialog("Loading...");

    Log.w("Jmol", "onResume..." + viewer);
    // am I correct that imageView is null if and only if viewer is null?
    // otherwise we could have a different imageView in updateListener than here

    //    if (imageView == null) {
    //      imageView = (SurfaceView) findViewById(R.id.imageMolecule);
    //    }

    if (viewer == null) {
      // bit of a chicken and an egg here, but 
      // we pass the updateListener to viewer, where it will be called
      // the "display" and then Platform will get a call asking for an update.
      // not sure about the rest of it!
      viewer = JmolViewer
          .allocateViewer(
              updateListener,
              new SmarterJmolAdapter(),
              null,
              null,
              null,
              "-NOTmultitouch-tab platform=uk.ac.ucl.phys.crystalexplorer.JmolPlatform",
              null);
      viewer.setJmolCallbackListener(this);
      
      String loadScript = STARTUP_SCRIPT;
      
      // Get the message from the intent
      Intent intent = getIntent();
      if(intent != null) {
    	  loadScript += "load " + intent.getStringExtra(MainActivity.EXTRA_MESSAGE) + ";"; 
      }
      
      script(loadScript);
    } else {

      Log.w("Jmol", "onResume... viewer=" + viewer + " opening=" + opening);
      if (viewer.getAtomCount() > 0) {// && !updateListener.isShowingDialog()) {
        imageView.invalidate();
      } else {
        //          setTitle(R.string.app_name);
        if (!opening) {
          imageView.post(new Runnable() {
            @Override
            public void run() {
              openOptionsMenu();
            }
          });
        }
        opening = false;
      }

    }
    Log.w("Jmol", "onResume... done");
    resumeComplete = true;
  };

  // TODO Auto-generated method stub

  @Override
  public void onBackPressed() {
    finish();
  };

  private boolean panLock;

  @Override
  public boolean onTouchEvent(final MotionEvent event) {
    switch (event.getPointerCount()) {
    case 1:
      break;
    case 2:
      if (spinDetector != null && isTwoFingerGyroPan(event))
        return panLock = true;
      //fall through
    default:
      scaleDetector.onTouchEvent(event);
      // no multitouch needed here.
      return true;
    }
    panLock = false;
    final int index = event.findPointerIndex(0);
    if (index < 0 || updateListener == null)
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
      updateListener.mouseEvent(e, (int) event.getX(index), (int) event
          .getY(index), Event.MOUSE_LEFT, event.getEventTime());
    return true;
  };

  private final static float PAN_THRESHOLD = 0.9f;

  private boolean isTwoFingerGyroPan(MotionEvent event) {
    if (event.getAction() != MotionEvent.ACTION_POINTER_2_DOWN)
      return false;
    float x1 = event.getX(0);
    float x2 = event.getX(1);
    return (Math.abs(Math.abs(x2 - x1) / screenWidth) > PAN_THRESHOLD);
  }

  private class JmolSpinDetector implements SensorEventListener {

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
      // huh?
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
      if (paused || updating || viewer == null)
        return;
      if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
        updateOrientation(event.values[0], event.values[1], event.values[2],
            event.timestamp);
      }
    }
  }

  private class PinchZoomScaleListener extends
      ScaleGestureDetector.SimpleOnScaleGestureListener {

    @Override
    public boolean onScale(final ScaleGestureDetector detector) {
      float zoomFactor = detector.getScaleFactor();
      //float viewerZoom = viewer.getZoomPercentFloat();
      //Log.w("Jmol", "old zoom=" + viewerZoom + " with detector.getScaleFactor=" + zoomFactor);

      //zoomFactor /= (viewerZoom / 100.0f);
      viewer.syncScript("Mouse: zoomByFactor " + zoomFactor, "~", 0);
      Log.w("Jmol", "changing zoom factor=" + zoomFactor);

      return true;
    }
  }

  @Override
  public void onWindowFocusChanged(boolean hasFocus) {
    Log.w("Jmol", "onWindowFocusChanged " + hasFocus);
    if (!hasFocus)
      return;
    updateListener.setScreenDimension();
  };

  private final static float gyroThreshold = 0.7f;
  private final static float spinFactor = 4.5f;

  protected void updateOrientation(float x, float y, float z, long when) {
    float dxyz2 = x * x + y * y;
    float speed = (float) (Math.sqrt(dxyz2) * (panLock ? 1 : 1) * spinFactor);
    if (panLock) {
      viewer.syncScript("Mouse: rotateXYBy " + (int) (-y * speed) + " " + (int) (-x * speed), "=",
          0);
      return;
    }
    if (dxyz2 < gyroThreshold)
      return;
    viewer.syncScript("Mouse: spinXYBy " + (int) -y + " " + (int) -x + " "
        + speed, "=", 0);
    // landscape mode, so x --> y and y --> -x
  }

  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    MenuItem styleMenu = menu.findItem(R.id.style);

    if (styleMenu != null)
      styleMenu.setEnabled(viewer.getAtomCount() > 0);

    return true;
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.main_menu, menu);
    return true;
  }

  private static final int REQUEST_OPEN = 1;

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle item selection
    switch (item.getItemId()) {
    case R.id.mol:
      prompt(/*GT._*/("Enter a molecule name:"), lastMolecule, 1);
      break;
    case R.id.command:
      prompt(/*GT._*/("Enter a script command:"), lastCommand, 0);
      break;
    case R.id.pdb:
      prompt(/*GT._*/("Enter a PDB ID or text:"), lastPDB, 2);
      break;
    case R.id.open:
      File path = Downloader.getAppDir(this);

      Intent intent = new Intent(getBaseContext(), FileDialog.class);
      intent.putExtra(FileDialog.START_PATH, path.getAbsolutePath());
      startActivityForResult(intent, REQUEST_OPEN);
      break;
    case R.id.cpkspacefill:
      script("spacefill only;color cpk");
      break;
    case R.id.ballandstick:
      script("wireframe -0.15;spacefill 23%;color cpk");
      break;
    case R.id.sticks:
      script("wireframe -0.3;color cpk");
      break;
    case R.id.wireframe:
      script("wireframe only;color cpk");
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

  private ProgressDialog pd;

  public synchronized void onActivityResult(final int requestCode,
                                            int resultCode, final Intent data) {
    if (resultCode == Activity.RESULT_OK) {

      if (requestCode == REQUEST_OPEN) {

        setDialog("Opening file...");

        //updateListener.manageDialog(ProgressDialog.show(this, "",
        //   "Opening file...", true), (byte) 2);
        // this is the best way to go -- allows for scripts, surfaces, and models
        viewer.openFileAsync(data
            .getStringExtra(FileDialog.RESULT_PATH));
        opening = true;
      }
    }
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

  protected int myType;

  private void prompt(String title, String text, int type) {

    myType = type;
    AlertDialog.Builder alert = new AlertDialog.Builder(this);

    alert.setTitle(title);

    // Set an EditText view to get user input 
    final EditText input = new EditText(this);
    input.setText(text);
    input.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
    input.setSelectAllOnFocus(true);
    alert.setView(input);

    alert.setPositiveButton(/*GT._*/("OK"),
        new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int whichButton) {
            processUserInput(input.getText().toString());
          }
        });

    alert.setNegativeButton(/*GT._*/("Cancel"),
        new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int whichButton) {
            // ignore
          }
        });

    alert.show();
  }

  private String lastCommand = "wireframe only";
  private String lastMolecule = "acetaminophen";
  private String lastPDB = "1crn";

  protected void processUserInput(String cmd) {
    switch (myType) {
    case 1: // mol
      script("load \"$" + (lastMolecule = cmd) + "\"");
      break;
    case 0: // script
      script(lastCommand = cmd);
      break;
    case 2:
      lastPDB = cmd;
      Intent dnIntent = new Intent(getBaseContext(), PDBSearchActivity.class);
      dnIntent.setAction("jmol::" + cmd);
      startActivityForResult(dnIntent, REQUEST_OPEN);
      break;
    }
  }

  private void script(final String script) {
    viewer.script(script);
  }

  protected void setDialog(String text) {
    //    dismissPDialog();
    pd = ProgressDialog.show(this, "", text, true);
  }

  protected void dismissPDialog() {
    // TODO Auto-generated method stub
    if (pd == null || !resumeComplete)
      return;
    try {
      pd.dismiss();
    } catch (Exception e) {
      // ignore
    }
    pd = null;
  }

  public void repaint() {
    //    if (drawTrigger)
    //      return;
    //    drawTrigger = true;
    Log.w("Jmol", "JmolActivity repaint " + imageView);
    dismissPDialog();
    if (paused || updating || viewer == null)
      return;
    if (imageView != null)
      //updateCanvas(); // alternative was not nec. after system.exit(0) was removed.
      imageView.postInvalidate();
  }

  private boolean updating;
  private boolean paused;

  /*
  private void updateCanvas() {
    Log.w("Jmol","updateCanvas paused/updating " + paused + " " + updating);
    long start = System.currentTimeMillis();

    synchronized (imageView) {
      updating = true;
      Canvas canvas = null;
      try {
        canvas = imageView.getHolder().lockCanvas();
        canvas.getHeight(); // simple test for canvas not null
      } catch (Exception e) {
        Log.w("Jmol", "Unable to lock the canvas\n");
        //e.printStackTrace();
      }
      if (canvas != null) {
        // at least for now we want to see errors traced to their Jmol methods, not trapped here
        viewer.renderScreenImage(canvas, null, canvas.getWidth(), canvas
            .getHeight());
        imageView.getHolder().unlockCanvasAndPost(canvas);
        Log.d("Jmol", "Image updated in " + (System.currentTimeMillis() - start)
            + " ms");
        Log.d("Jmol", "Zoom % " + viewer.getZoomPercentFloat());
      }
      updating = false;
    }
  }
  */
  protected void setPaused(boolean TF) {
    paused = TF;
    if (paused && viewer != null) {
      viewer.syncScript("Mouse: spinXYBy 0 0 0", "+", 0);
    }
    Log.w("Jmol", "setPaused " + paused);
  }

}