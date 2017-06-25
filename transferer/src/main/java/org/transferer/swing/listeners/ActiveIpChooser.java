package org.transferer.swing.listeners;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

public class ActiveIpChooser implements ActionListener {

	private Map<String, String> buttonDetail = new HashMap<String, String>();

	private String selectedIpAddress;

	public void addButtonDetails(String buttonName, String buttonData) {
		buttonDetail.put(buttonName, buttonData);
	}

	public void actionPerformed(ActionEvent actionEvent) {
		String commandName = actionEvent.getActionCommand();
		System.out.println("triggered action Command Name:" + commandName);
		selectedIpAddress = buttonDetail.get(commandName);
		synchronized (this) {
			this.notify();
		}
	}

	public String getSelectedIpAddress() {
		return selectedIpAddress;
	}
}
