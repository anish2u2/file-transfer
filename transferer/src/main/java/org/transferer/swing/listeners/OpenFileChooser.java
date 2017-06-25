package org.transferer.swing.listeners;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JFrame;

import org.transferer.swing.components.AppButton;
import org.transferer.swing.components.AppFileChooser;

public class OpenFileChooser implements ActionListener {

	private Component component;
	private Component buComponent;

	public void setContaierOfThisContent(Component component) {
		this.component = component;
	}

	public void setComponent(Component component) {
		buComponent = component;
	}

	public void actionPerformed(ActionEvent e) {
		AppFileChooser fileChooser = new AppFileChooser();
		fileChooser.setMultiSelectionEnabled(true);
		String actionCommand = e.getActionCommand();
		if ("Choose File".equals(actionCommand)) {
			int odInt = fileChooser.showOpenDialog(((JFrame) component).getContentPane());
			File[] filesToBeSend = fileChooser.getSelectedFiles();
			if ((odInt == JFileChooser.APPROVE_OPTION) && filesToBeSend != null) {
				List<String> listOfFileName = new ArrayList<String>();
				for (File file : filesToBeSend) {
					listOfFileName.add(file.getAbsolutePath());
				}
				((AppButton) buComponent).addData("filesList", listOfFileName);
				System.out.println("selected Files..." + listOfFileName);

			}

		}
		synchronized (buComponent) {
			buComponent.notify();
		}
		((JFrame) component).remove(fileChooser);

	}

}
