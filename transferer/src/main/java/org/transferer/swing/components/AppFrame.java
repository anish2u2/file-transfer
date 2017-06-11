package org.transferer.swing.components;

import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.io.File;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.server.client.contract.Client;
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

				public void doWork() {
					try {
						System.out.println("Statring work of main Thread..");
						// while (true) {
						String fileAbsolutePath = null;
						File file = null;
						Reader reader = server.getReader();
						Writer writer = server.getWriter();
						if (onRequest(reader.getRequestAddress())) {

							synchronized (ThreadUtilityFactory.getInstance().get("button")) {
								ThreadUtilityFactory.getInstance().get("button").wait();
							}
							System.out.println("Removing the button..");
							frame.remove((JButton) ThreadUtilityFactory.getInstance().get("button"));
							fileAbsolutePath = (String) ((AppButton) ThreadUtilityFactory.getInstance().get("button"))
									.getData("filesList");
							file = new File(fileAbsolutePath);

							StablishFileTransferCommunication communication = new StablishFileTransferCommunication();
							communication.setReader(reader);
							communication.setWriter(writer);
							communication.startConversationAsServer(fileAbsolutePath);
							/*FileInputStream inputStream = new FileInputStream(file);
							byte[] buffer = new byte[4096];//(file.length() / 1000000 > 5) ? 1000000 : 
							System.out.println("File size.." + (file.length() / 1000000));
							System.out.println("Writing file to client");
							while ((inputStream.read(buffer)) != -1) {
								System.out.println(new String(buffer, "UTF-8"));
								writer.write(buffer);
								if (file.length() / 1000000 > 5) {
									Thread.sleep(100);
								}

								writer.flush();
							}
							writer.write(buffer);
							writer.flush();
							inputStream.close();

							System.out.println("response send.");
							if (!writer.isClosed())
								writer.flushAndClose();*/
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

	public void startClient() {
		Wifi wifi = WifiFactory.getInstance();
		final Client client = wifi.getClient();
		client._init(getHostAddress(), 80, 0);
		System.out.println("Now client is connected to server success fully..");
		Map<Object, Object> threadLocalMap = ThreadUtilityFactory.getInstance().getMap();
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
					communication.startConversationAsClient("D:/");
					/*String fileName = communication.startConversationAsClient();
					fileName = "D:/" + fileName;
					System.out.println("Writing file:" + fileName);
					FileOutputStream outputStream = new FileOutputStream(new File(fileName));
					InputStream inputStream = reader.getInputStream();
					byte[] buffer = new byte[4096];
					while (inputStream.read(buffer) != -1) {
						outputStream.write(buffer);
					}
					System.out.println("File Successfully written..");
					outputStream.flush();
					outputStream.close();
					reader.close();
					if (!writer.isClosed())
						writer.flushAndClose();*/
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});
	}

	public String getHostAddress() {
		try {
			for (Enumeration networkInterface = NetworkInterface.getNetworkInterfaces(); networkInterface
					.hasMoreElements();) {
				NetworkInterface network = (NetworkInterface) networkInterface.nextElement();
				for (Enumeration inetAddress = network.getInetAddresses(); inetAddress.hasMoreElements();) {
					InetAddress address = (InetAddress) inetAddress.nextElement();
					System.out.println("---------------------------------");
					System.out.println("Host address:" + address.getHostAddress());
					System.out.println("Is loop back address:" + address.isLoopbackAddress());
					System.out.println("Is Site Local back address:" + address.isSiteLocalAddress());
					System.out.println("Is Link Local back address:" + address.isLinkLocalAddress());
					System.out.println("Is Any Local back address:" + address.isAnyLocalAddress());
					System.out.println("---------------------------------");
					if (address.isSiteLocalAddress())
						return address.getHostAddress();
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
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
