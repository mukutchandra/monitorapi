package org.mukut;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class FileServer {

	private static final int PORT = 50101;
	private static final int TIMEOUT_MINUTES = 1;
	private static DataInputStream dataInputStream = null;

	public static void main(final String[] args) {
		final ExecutorService executorService = Executors.newSingleThreadExecutor();

		try (ServerSocket serverSocket = new ServerSocket(PORT)) {
			System.out.println("Server started. Listening on port " + PORT);

			while (true) {
				final Socket clientSocket = serverSocket.accept();
				System.out.println("New client connected: " + clientSocket);

				// Set the timeout for the client socket
				// clientSocket.setSoTimeout(TIMEOUT_MINUTES * 60 * 1000);

				dataInputStream = new DataInputStream(clientSocket.getInputStream());
				// Your logic for handling the client's input/output goes here
				// read from socket to ObjectInputStream object
				final byte[] buffer = new byte[14];
				dataInputStream.read(buffer, 0, 14);
				final String messageReceived = new String(buffer, StandardCharsets.US_ASCII);
				System.out.println("Message Received: " + messageReceived);
				// Create a custom object to hold the Socket and request message
				final ClientRequest clientRequest = new ClientRequest(clientSocket, messageReceived);

				// Submit a new task to the executor for each client connection
				final Future<?> future = executorService.submit(() -> handleClient(clientRequest));

				// Start the monitoring thread to check for task timeout and kill the hanging
				// client
				startMonitorThread(clientRequest, future, TIMEOUT_MINUTES);
			}
		} catch (final IOException e) {
			e.printStackTrace();
		} finally {
			executorService.shutdown();
		}
	}

	private static void startMonitorThread(final ClientRequest clientRequest, final Future<?> future,
			final int timeoutMinutes) {
		final Thread monitorThread = new Thread(() -> {
			try {
				// Wait for the specified time, plus a grace period of 1 second
				future.get(timeoutMinutes + 1, TimeUnit.MINUTES);
			} catch (TimeoutException | InterruptedException | ExecutionException e) {
				// Task took too long or got interrupted or threw an exception.
				// In any case, we need to cancel the task to kill it.
				future.cancel(true);
				System.err.println("Client timed out. Closing connection: " + clientRequest.getClientSocket());
				try {
					// Send the timeout response back to the client
					sendTimeoutResponse(clientRequest);
				} catch (final IOException e1) {
					e1.printStackTrace();
				}
			}
		});

		monitorThread.start();
	}

	private static void handleClient(final ClientRequest clientRequest) {
		try {
			// Your logic for handling the client's input/output goes here
			final String request = clientRequest.getRequestMessage();
			System.out.println("Inside handle Client, request message => " + request);
			// Hang indefinitely without sending any message
			//Thread.sleep(Long.MAX_VALUE);
			// For demonstration purposes, we'll just sleep for 10 seconds before closing
			// the connection
			 Thread.sleep(10000);
			// Sending normal response after receiving a request from the client
			final OutputStream outputStream = clientRequest.getClientSocket().getOutputStream();
			final String normalResponse = "Request has been processed. Closing connection. Original request: "
					+ request;
			outputStream.write(normalResponse.getBytes());
			outputStream.flush();
			clientRequest.getClientSocket().close();
			System.out.println("Client disconnected: " + clientRequest.getClientSocket());
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	private static void sendTimeoutResponse(final ClientRequest clientRequest) throws IOException {
		final Socket clientSocket = clientRequest.getClientSocket();
		final String request = clientRequest.getRequestMessage();

		final OutputStream outputStream = clientSocket.getOutputStream();
		final String timeoutResponse = "Request timed out. Closing connection. Original request: " + request;
		outputStream.write(timeoutResponse.getBytes());
		outputStream.flush();

		clientSocket.close();
	}

	// Custom class to hold the Socket and request message together
	private static class ClientRequest {
		private final Socket clientSocket;
		private final String requestMessage;

		public ClientRequest(final Socket clientSocket, final String requestMessage) {
			this.clientSocket = clientSocket;
			this.requestMessage = requestMessage;
		}

		public Socket getClientSocket() {
			return clientSocket;
		}

		public String getRequestMessage() {
			return requestMessage;
		}
	}
}
