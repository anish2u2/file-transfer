package org.transferer.swing.components;

import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.net.InetAddress;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.server.client.contract.Client;
import org.server.client.contract.IpAddressDetail;
import org.server.client.contract.Reader;
import org.server.client.contract.Server;
import org.server.client.contract.Wifi;
import org.server.client.contract.Work;
import org.server.client.contract.Writer;
import org.server.client.factory.imple.WifiFactory;
import org.server.client.thread.ThreadUtilityFactory;
import org.server.client.thread.WorkerThread;
import org.transferer.swing.communication.StablishFileTransferCommunication;
import org.transferer.swing.contracts.WindowFrame;
import org.transferer.swing.listeners.ActiveIpChooser;
import org.transferer.swing.listeners.OpenFileChooser;

public class AppFrame extends JFrame implements WindowFrame {

	/**
	 * @author Anish Singh
	 */
	private static final long serialVersionUID = 134543534L;

	private Container container = this.getContentPane();

	public void setSize(int xAxis, int yAxis, int width, int height) {
		this.setBounds(xAxis, yAxis, width, height);
	}

	public void setLayoutFlow() {
		container.setLayout(new FlowLayout(FlowLayout.CENTER));
	}

	public void addComponent(Component component) {
		container.add(component);
	}

	public void startServer() {
		repaintTheScreen();
		Wifi wifi = WifiFactory.getInstance();
		final Server server = wifi.getServer();
		server._init(null, 80, 0);
		final JFrame frame = this;
		while (true) {
			WorkerThread.getWorker().startWorking(new Work() {

				@SuppressWarnings("unchecked")
				public void doWork() {
					try {
						System.out.println("Statring work of main Thread..");
						List<String> fileAbsolutePath = null;
						Reader reader = server.getReader();
						Writer writer = server.getWriter();
						if (onRequest(reader.getRequestAddress())) {

							synchronized (ThreadUtilityFactory.getInstance().get("button")) {
								ThreadUtilityFactory.getInstance().get("button").wait();
							}
							System.out.println("Removing the button..");
							frame.remove((JButton) ThreadUtilityFactory.getInstance().get("button"));
							fileAbsolutePath = (List<String>) ((AppButton) ThreadUtilityFactory.getInstance()
									.get("button")).getData("filesList");

							StablishFileTransferCommunication communication = new StablishFileTransferCommunication();
							communication.setReader(reader);
							communication.setWriter(writer);
							for (String filePath : fileAbsolutePath) {
								communication.startConversationAsServer(filePath);
								System.out.println("File sent..");
							}
							writer.flushAndClose();
						}
						ThreadUtilityFactory.getInstance().removeAll();
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			});
			try {
				synchronized (server) {
					server.wait();
				}
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	@SuppressWarnings("deprecation")
	public void startClient(final String fileDirectory) {
		System.out.println("File Directory:" + fileDirectory);
		repaintTheScreen();
		Wifi wifi = WifiFactory.getInstance();
		final Client client = wifi.getClient();
		List<IpAddressDetail> activeIps = client.getActiveAddress();
		ActiveIpChooser ipChooser = new ActiveIpChooser();
		List<String> thisSystemIp = new LinkedList<String>();
		try {
			InetAddress[] inetAddresses = InetAddress.getAllByName(InetAddress.getLocalHost().getHostName());
			for (InetAddress inetAddress : inetAddresses) {
				thisSystemIp.add(inetAddress.getHostAddress());
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		boolean isCurrentSysIp = false;
		for (IpAddressDetail ip : activeIps) {

			for (String currentSysIp : thisSystemIp) {
				if (currentSysIp.equals(ip.getIpAddress())) {
					isCurrentSysIp = false;
				}
			}
			if (!isCurrentSysIp) {
				try {
					System.out.println("ip:" + ip.getIpAddress());
					AppButton button = new AppButton(ip.getName());
					button.addActionListener(ipChooser);
					System.out.println("button label:" + button.getLabel() + " ip address:" + ip.toString());
					ipChooser.addButtonDetails(ip.getName(), ip.getIpAddress());
					this.add(button);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
		System.out.println("Going to wait state for listening ip choose.");
		synchronized (ipChooser) {
			try {
				ipChooser.wait();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		String serverIp = ipChooser.getSelectedIpAddress();
		System.out.println("Connecting to Ip:" + serverIp);
		client._init(serverIp, 80, 0);
		System.out.println("Now client is connected to server success fully..");
		WorkerThread.getWorker().startWorking(new Work() {

			public void doWork() {
				try {
					System.out.println("Starting Reading data in client..");
					Reader reader = client.getReader();
					Writer writer = client.getWriter();
					System.out.println("Got writer and reader..");
					StablishFileTransferCommunication communication = new StablishFileTransferCommunication();
					communication.setReader(reader);
					communication.setWriter(writer);
					System.out.println("set to the communication...");
					communication.startConversationAsClient(fileDirectory);
					System.out.println("Release All Threads resources..");
					ThreadUtilityFactory.getInstance().removeAll();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});
	}

	public boolean onRequest(String clientAddress) {
		JOptionPane optionPane = new JOptionPane();
		this.add(optionPane);
		@SuppressWarnings("static-access")
		int selectedOption = optionPane.showConfirmDialog(container,
				"Do you want to serve request for :" + clientAddress, "Request", JOptionPane.YES_NO_OPTION,
				JOptionPane.INFORMATION_MESSAGE);
		if (selectedOption == JOptionPane.YES_OPTION) {
			this.remove(optionPane);
			AppButton button = new AppButton("Choose File");
			ThreadUtilityFactory.getInstance().add("button", button);
			OpenFileChooser fileChooser = new OpenFileChooser();
			fileChooser.setContaierOfThisContent(this);
			fileChooser.setComponent(button);
			button.addActionListener(fileChooser);
			container.add(button);
			return true;
		} else {
			this.remove(optionPane);
			return false;
		}

	}

	private void repaintTheScreen() {
		final JFrame frame = this;
		WorkerThread.getWorker().startWorking(new Work() {

			public void doWork() {
				while (true) {
					try {
						frame.repaint();
						Thread.sleep(200);
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}

			}
		});
	}

}
