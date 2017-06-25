package org.transferer.swing.fileSystem;

import java.io.File;

public class FileSystemUtility {

	public static File getUserHomeDirectory() {
		try {
			String systemHomeDirectory = System.getProperty("user.home");
			File homeDirectory = new File(systemHomeDirectory + File.separator + "transfrer");
			if (!homeDirectory.exists()) {
				homeDirectory.mkdir();
			}
			return homeDirectory;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

}
