package org.transferer.swing.components;

import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.server.client.contract.Client;
import org.server.client.contract.Reader;
import org.server.client.contract.Reader.RESPONSE_TYPE;
import org.server.client.contract.Server;
import org.server.client.contract.Wifi;
import org.server.client.contract.Work;
import org.server.client.contract.Writer;
import org.server.client.factory.imple.WifiFactory;
import org.server.client.thread.ThreadUtilityFactory;
import org.server.client.thread.WorkerThread;
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
						if (onRequest(reader.getRequestAddress())) {
							System.out.println("Response from vernetwork client:" + reader.read(RESPONSE_TYPE.STRING));
							Writer writer = server.getWriter();
							while (true) {
								String responseFromClient = (String) reader.read(RESPONSE_TYPE.STRING);

								writer.write("You are requesting for file Do you want to proceed:\n a)yes \n b)no"
										.getBytes());
								responseFromClient = (String) reader.read(RESPONSE_TYPE.STRING);
								if ("a".equalsIgnoreCase(responseFromClient)
										|| "yes".equalsIgnoreCase(responseFromClient)) {
									synchronized (ThreadUtilityFactory.getInstance().get("button")) {
										ThreadUtilityFactory.getInstance().get("button").wait();
									}
									frame.remove((JButton) ThreadUtilityFactory.getInstance().get("button"));
									fileAbsolutePath = (String) ((AppButton) ThreadUtilityFactory.getInstance()
											.get("button")).getData("filesList");
									file = new File(fileAbsolutePath);
									writer.write(("file-name:" + file.getName()).getBytes());
									writer.flush();

								} else if ("ok".equalsIgnoreCase(responseFromClient)) {
									break;
								} else if ("b".equalsIgnoreCase(responseFromClient)
										|| "no".equalsIgnoreCase(responseFromClient)) {
									writer.flushAndClose();
									return;
								} else {
									writer.write("If you wish to close the connection then type: b or no".getBytes());
								}
							}
							// writer.write("Hi this server checking multiple
							// clients....".getBytes());
							// writer.flushAndClose();

							FileInputStream inputStream = new FileInputStream(file);
							byte[] buffer = new byte[(file.length() / 1000000 > 5) ? 1000000 : 4096];
							System.out.println("File size.." + (file.length() / 1000000));
							while ((inputStream.read(buffer)) != -1) {

								writer.write(buffer);
								if (file.length() / 1000000 > 5) {
									Thread.sleep(200);
								}
								// System.out.println("Writing buffer:"
								// +
								// new String(buffer, "UTF-8"));
								writer.flush();
							}
							inputStream.close();
							// System.out.println("Got the writer now waiting
							// ofr the reader..:" + filesList);

							// writer.write("hi this is anish
							// verifying..".getBytes());
							System.out.println("response send.");
							writer.flushAndClose();
						}
						ThreadUtilityFactory.getInstance().removeAll();
						// Thread.sleep(300);
						// }
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
		client._init(getHostAddress(), 0, 0);
		WorkerThread.getWorker().startWorking(new Work() {

			public void doWork() {
				try {
					Reader reader = client.getReader();
					String response = (String) reader.read(RESPONSE_TYPE.STRING);
					System.out.println("Please response to Q.");
					System.out.println(response);
					BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
					String command = null;
					boolean readFile = false;
					String fileName = "";
					Writer writer = client.getWriter();
					while (true) {
						command = bufferedReader.readLine();
						if (fileName != null && "ok".equalsIgnoreCase(command)) {
							readFile = true;
						}
						writer.write(command.getBytes());
						writer.flush();
						if (readFile) {
							FileOutputStream outputStream = new FileOutputStream(new File(fileName));
							outputStream.write((byte[]) reader.read(RESPONSE_TYPE.BYTE));
							outputStream.flush();
							outputStream.close();
							break;
						}
						response = (String) reader.read(RESPONSE_TYPE.STRING);
						if (response.contains("file-name:"))
							fileName = response.split("file-name:")[1];
					}

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
						return address.getHostAddress() + ":8987";
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
