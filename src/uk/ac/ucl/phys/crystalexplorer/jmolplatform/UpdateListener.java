package uk.ac.ucl.phys.crystalexplorer.jmolplatform;

import org.jmol.api.JmolViewer;

public interface UpdateListener {

	public void setViewer(JmolViewer viewer);

	public void getScreenDimensions(int[] widthHeight);

	public void setScreenDimension();

	public void repaint();

	public void mouseEvent(int id, int x, int y, int modifiers, long when);
}
