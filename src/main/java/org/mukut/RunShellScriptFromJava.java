package org.mukut;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

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

			// Start a separate thread to read and print the output of the script
			final Thread outputThread = new Thread(() -> {
				try {
					final InputStream inputStream = process.getInputStream();
					final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
					String line;
					while ((line = reader.readLine()) != null) {
						System.out.println(line);
					}
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
					Thread.sleep(timeout);
					if (process.isAlive()) {
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
