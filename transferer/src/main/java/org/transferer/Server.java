package org.transferer;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

	public static void main(String args[]) {
		try {
			ServerSocket server = new ServerSocket(80);
			Socket socket = server.accept();
			DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
			BufferedReader inputStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
			outputStream.writeUTF("Do you wish to continue :");
			outputStream.writeByte(13);
			outputStream.writeInt(10);
			outputStream.flush();
			String line = inputStream.readLine();
			System.out.println("Response:" + line);
			String userInput = reader.readLine();
			while (!"quite".equals(line)) {
				for (int counter = 0; counter < userInput.length(); counter++) {
					outputStream.write((byte) userInput.charAt(counter));
				}
				outputStream.write(13);
				outputStream.write(10);
				outputStream.flush();
				line = inputStream.readLine();
				System.out.println("Client:" + line);
				System.out.println("please input text to send to client");
				userInput = reader.readLine();
			}
			reader.close();
			outputStream.close();
			inputStream.close();
			socket.close();
			server.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
