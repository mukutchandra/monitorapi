package org.mukut;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class RunShellScriptFromJava {
	public static void main(final String[] args) {
		try {
			// Replace "script.sh" with the actual path to your shell script
			final ProcessBuilder processBuilder = new ProcessBuilder("path/to/your/script.sh");

			// Set the working directory if the script relies on specific file paths
			// processBuilder.directory(new File("path/to/working/directory"));

			// Redirect the script's standard output and error streams to Java's standard
			// streams
			processBuilder.redirectErrorStream(true);

			final Process process = processBuilder.start();

			// Create an array to hold the operationCompleted flag
			final boolean[] operationCompleted = { false };

			// Start a separate thread to read and print the output of the script
			final Thread outputThread = new Thread(() -> {
				try {
					System.out.println(new String(process.getInputStream().readAllBytes()));
				} catch (final IOException e) {
					e.printStackTrace();
				}
			});
			outputThread.start();

			// Set a timeout of 2 minutes (120,000 milliseconds)
			final int timeout = 120000;

			// Start a separate thread to monitor the timeout
			final Thread timeoutThread = new Thread(() -> {
				try {
					process.waitFor(timeout, TimeUnit.MILLISECONDS);

					// If the operation is not yet completed, terminate the process
					if (!operationCompleted[0] && process.isAlive()) {
						process.destroy();
						System.err.println("Script execution timed out. Terminating process.");
					}
				} catch (final InterruptedException e) {
					e.printStackTrace();
				}
			});
			timeoutThread.start();

			// Wait for the script to finish
			final int exitCode = process.waitFor();

			// Set the flag to indicate the operation is completed
			operationCompleted[0] = true;

			// Interrupt the timeout thread to stop waiting
			timeoutThread.interrupt();

			if (exitCode == 0) {
				System.out.println("Script executed successfully.");
			} else {
				System.err.println("Error executing the script. Exit code: " + exitCode);
			}
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}
}
