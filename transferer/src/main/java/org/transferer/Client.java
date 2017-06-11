package org.transferer;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;

public class Client {

	public static void main(String[] args) {
		try {

			Socket socket = new Socket("localhost", 80);
			DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
			BufferedReader inputStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

			String userInput ="hello Server..";
			String line = inputStream.readLine();
			System.out.println("Response:" + line);
			while (!"quite".equals(line)) {
				
				for (int counter = 0; counter < userInput.length(); counter++) {
					outputStream.write((byte) userInput.charAt(counter));
				}
				outputStream.write(13);
				outputStream.write(10);
				outputStream.flush();
				line = inputStream.readLine();
				System.out.println("Server:" + line);
				userInput = "ok";
			}
			reader.close();
			outputStream.close();
			inputStream.close();
			socket.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
