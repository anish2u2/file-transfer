package org.transferer.swing.contracts;

import java.awt.Component;

public interface WindowFrame {

	public void setSize(int xAxis, int yAxis, int width, int height);

	public void addComponent(Component component);

	public void show();

}
