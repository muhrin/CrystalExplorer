package uk.ac.ucl.phys.crystalexplorer.jmolplatform;

import java.io.File;

import org.jmol.api.JmolFileInterface;

/**
 * a subclass of File allowing extension to JavaScript
 * 
 * private to org.jmol.awt
 * 
 */

class JmolFile extends File implements JmolFileInterface {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5873250290119404782L;

	public JmolFile(String name) {
		super(name);
	}

	public JmolFileInterface getParentAsFile() {
		File file = getParentFile();
		return (file == null ? null : new JmolFile(file.getAbsolutePath()));
	}

}
