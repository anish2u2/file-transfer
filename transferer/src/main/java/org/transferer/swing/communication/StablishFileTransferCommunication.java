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
		reader.readFile(directoryPath);
		reader.close();
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

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		writer = null;
		reader = null;
	}
}
