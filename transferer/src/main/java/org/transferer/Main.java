package org.transferer;

import org.server.client.contract.Work;
import org.server.client.thread.WorkerThread;
import org.transferer.swing.components.AppFrame;

public class Main {

	public static void main(String[] args) {
		AppFrame frame = new AppFrame();
		frame.setLayoutFlow();
		frame.setSize(400, 400, 400, 400);
		frame.setVisible(true);
		frame.startClient();
		WorkerThread.getWorker().startWorking(new Work() {

			public void doWork() {
				while (true) {
					Runtime runtime = Runtime.getRuntime();
					System.out.println("Total Memory:" + (runtime.totalMemory() / 1000000) + " M.B." + " free Memory:"
							+ (runtime.freeMemory() / 1000000) + " M.B. " + " memory consumed by app:"
							+ ((runtime.totalMemory() - runtime.freeMemory()) / 1000000) + " M.B.");
					try {
						Thread.sleep(10000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

			}
		});

	}

}
