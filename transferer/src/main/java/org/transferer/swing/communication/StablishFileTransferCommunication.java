package org.transferer.swing.communication;

import org.server.client.contract.Reader;
import org.server.client.contract.Writer;

public class StablishFileTransferCommunication {

	private Writer writer;

	private Reader reader;

	public void startConversationAsServer(String fileName) {
		writer.writeFile(fileName);
	}

	public void startConversationAsClient(String directoryPath) {
		/*
		 * System.out.println("Client is communicating.."); String response =
		 * (String) reader.read(RESPONSE_TYPE.STRING); while
		 * (!"quit".equals(response)) { System.out.println(
		 * "Response from server:" + response); if
		 * (response.contains("file-name")) return response.split(":")[1];
		 * System.out.println("reading for response.."); response = (String)
		 * reader.read(RESPONSE_TYPE.STRING); }
		 */
		reader.readFile(directoryPath);

	}

	public Writer getWriter() {
		return writer;
	}

	public void setWriter(Writer writer) {
		this.writer = writer;
	}

	public Reader getReader() {
		return reader;
	}

	public void setReader(Reader reader) {
		this.reader = reader;
	}

}
