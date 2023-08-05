package org.mukut;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;

public class HangingClient {

    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 50101;

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT)) {
            System.out.println("Connected to server.");

            // Start a separate thread to receive the server response
            Thread responseThread = new Thread(() -> receiveResponse(socket));
            responseThread.start();
            
            // Send a request message to the server
            DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
            String requestMessage = "Hello, Server!";
            outputStream.write(requestMessage.getBytes());
            outputStream.flush();

            // Hang indefinitely without sending any message
            Thread.sleep(Long.MAX_VALUE);
            

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    private static void receiveResponse(Socket socket) {
        try {
            InputStream inputStream = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String response = reader.readLine();
            System.out.println("Received response from server for hanging client: " + response);
        } catch (IOException e) {
            System.err.println("Connection closed by the server.");
        }
    }
}

