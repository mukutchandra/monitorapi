package org.mukut;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

public class FileClient {

	private static final String SERVER_ADDRESS = "localhost";
	private static final int SERVER_PORT = 50101;

	public static void main(final String[] args) {
		try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT)) {
			System.out.println("Connected to server.");

			// Sending a message to the server
			final String message = "Hello, Server!";
			final OutputStream outputStream = socket.getOutputStream();
			outputStream.write(message.getBytes());
			outputStream.flush();

			// Receiving a response from the server
			final InputStream inputStream = socket.getInputStream();
			final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
			final String response = reader.readLine();
			System.out.println("Server response: " + response);

		} catch (final IOException e) {
			e.printStackTrace();
		}
	}
}
