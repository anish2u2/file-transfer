package org.transferer.swing.components;

import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;

public class AppButton extends JButton {

	/**
	 * @author Anish Singh
	 */
	private static final long serialVersionUID = 1765565656L;

	private Map<String, Object> dataMap;

	public AppButton(String buttonLabel) {
		super(buttonLabel);
	}

	public void addData(String key, Object data) {
		if (dataMap == null)
			dataMap = new HashMap<String, Object>();
		dataMap.put(key, data);
	}

	public Object getData(String key) {
		if (dataMap == null)
			return null;
		System.out.println("Returning key value contains:" + dataMap.containsKey(key));
		return dataMap.get(key);
	}

}
